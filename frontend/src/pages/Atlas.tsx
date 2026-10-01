import { useMemo, useState } from 'react';
import { mensajeError, useProgresion, usePesos, useRecords, useSesiones, useSesionesCompletas, type SesionCompleta } from '../api/atlas';
import { PageHeader } from '../components/PageHeader';
import { Alert, Badge, BarChart, Button, Card, LineChart, Select, Stat } from '../design-system';
import { FormularioSesion } from './atlas/FormularioSesion';
import './atlas/atlas.css';
import { diasEntre, deIso, iso, lunesDe, nombreMes, num, relativa, semanaIso, sumarDias } from '../lib/fechas';

const SEMANAS = 10;
const MESES_CORTOS = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];

const volumenDe = (s: SesionCompleta) => s.series.reduce((a, x) => a + x.reps * x.pesoKg, 0);

function delta(n: number, dec: number, unidad = ''): string {
  const signo = n > 0 ? '+' : n < 0 ? '−' : '';
  return `${signo}${num(Math.abs(n), dec)}${unidad}`;
}

const tono = (n: number) => (n > 0 ? 'up' : n < 0 ? 'down' : 'flat') as 'up' | 'down' | 'flat';

// Una sesion abierta: nueva (sin id) o una existente.
type Abierta = { id?: number } | null;

export function Atlas() {
  const [abierta, setAbierta] = useState<Abierta>(null);
  const hoy = useMemo(() => new Date(), []);
  const hoyIso = iso(hoy);
  const lunes = lunesDe(hoy);
  const lunesIso = iso(lunes);
  const lunesPrevio = iso(sumarDias(lunes, -7));
  const mesActual = hoy.getMonth();
  const inicioMes = new Date(hoy.getFullYear(), mesActual, 1);
  const inicioMesPrevio = new Date(hoy.getFullYear(), mesActual - 1, 1);
  const desde = iso(inicioMesPrevio < sumarDias(lunes, -7 * (SEMANAS - 1)) ? inicioMesPrevio : sumarDias(lunes, -7 * (SEMANAS - 1)));

  const sesiones = useSesiones(desde, hoyIso);
  const lista = sesiones.data;

  // Series y volumen se piden de las sesiones que cuentan para "esta semana", "la anterior"
  // y las 5 ultimas que se listan (el listado ligero no trae series).
  const idsDetalle = useMemo(() => {
    if (!lista) return [];
    const recientes = lista.filter((s) => s.fecha >= lunesPrevio).length;
    return lista.slice(0, Math.min(14, Math.max(5, recientes))).map((s) => s.id);
  }, [lista, lunesPrevio]);
  const detalle = useSesionesCompletas(idsDetalle);

  const records = useRecords();
  const [elegido, setElegido] = useState<number | undefined>();
  const porDefecto = detalle.data?.[0]?.series[0]?.ejercicioId ?? records.data?.[0]?.ejercicioId;
  const ejercicioId = elegido ?? porDefecto;
  const ejercicio = records.data?.find((r) => r.ejercicioId === ejercicioId);

  const desdeProg = iso(sumarDias(hoy, -182));
  const progresion = useProgresion(ejercicioId, desdeProg, hoyIso);
  const pesos = usePesos(iso(sumarDias(hoy, -90)), hoyIso);

  // ---- Derivados ----
  const sesionesMes = lista?.filter((s) => s.fecha >= iso(inicioMes)).length ?? 0;
  const sesionesMesPrevio = lista?.filter((s) => s.fecha >= iso(inicioMesPrevio) && s.fecha < iso(inicioMes)).length ?? 0;

  const volSemana = (detalle.data ?? []).filter((s) => s.fecha >= lunesIso).reduce((a, s) => a + volumenDe(s), 0);
  const volPrevia = (detalle.data ?? []).filter((s) => s.fecha >= lunesPrevio && s.fecha < lunesIso).reduce((a, s) => a + volumenDe(s), 0);

  const seriesSemana = useMemo(() => {
    const sem = Array.from({ length: SEMANAS }, (_, i) => sumarDias(lunes, -7 * (SEMANAS - 1 - i)));
    const total = sem.map(() => 0);
    for (const s of lista ?? []) {
      const i = SEMANAS - 1 - Math.floor(diasEntre(lunesDe(deIso(s.fecha)), lunes) / 7);
      if (i >= 0 && i < SEMANAS) total[i] += s.numeroSeries;
    }
    return sem.map((d, i) => ({ label: `S${semanaIso(d)}`, value: total[i] }));
  }, [lista, lunes]);

  const puntos = progresion.data ?? [];
  // Un ejercicio solo con peso corporal tiene volumen 0 (ADR 026): se pinta por repeticiones.
  const porReps = puntos.length > 0 && puntos.every((p) => p.pesoMaximo === 0);
  const valores = puntos.map((p) => (porReps ? p.repsTotales : p.volumen));
  const ultimo = puntos[puntos.length - 1];
  const anterior = puntos[puntos.length - 2];

  const pesoActual = [...(pesos.data ?? [])].sort((a, b) => a.fecha.localeCompare(b.fecha));
  const peso = pesoActual[pesoActual.length - 1];
  const pesoPrevio = pesoActual[pesoActual.length - 2];

  const ultimas = (detalle.data ?? []).slice(0, 5);
  const recordsOrden = useMemo(
    () => [...(records.data ?? [])].sort((a, b) => b.fechaPesoMaximo.localeCompare(a.fechaPesoMaximo)).slice(0, 6),
    [records.data],
  );

  const error = sesiones.error ?? records.error;
  const sinSesiones = sesiones.isSuccess && records.isSuccess && records.data.length === 0;

  const etiquetasProg = puntos.length > 1 ? [puntos[0], puntos[Math.floor((puntos.length - 1) / 2)], ultimo] : puntos.slice(0, 1);

  return (
    <div>
      <PageHeader
        eyebrow="Atlas"
        coord={`SEMANA ${semanaIso(hoy)}`}
        title="Progresión"
        actions={
          <Button icon="plus" onClick={() => setAbierta({})}>
            Registrar sesión
          </Button>
        }
      />

      {error ? (
        <Alert
          tone="danger"
          title="No se ha podido cargar Atlas"
          action={
            <Button size="sm" variant="secondary" onClick={() => void (sesiones.isError ? sesiones.refetch() : records.refetch())}>
              Reintentar
            </Button>
          }
        >
          {mensajeError(error)}
        </Alert>
      ) : sinSesiones ? (
        <Card eyebrow="Atlas" title="Aún no hay sesiones">
          <p className="muted" style={{ margin: 0 }}>Cuando registres tu primera sesión aparecerán aquí la progresión, los récords y el volumen.</p>
        </Card>
      ) : (
        <div className="grid">
          <div className="span-12 stats pl-rise">
            <Stat
              label={`Sesiones · ${MESES_CORTOS[mesActual]}`}
              value={sesionesMes}
              size={34}
              delta={sesionesMesPrevio || sesionesMes ? delta(sesionesMes - sesionesMesPrevio, 0) : undefined}
              deltaTone={tono(sesionesMes - sesionesMesPrevio)}
              caption={`vs ${nombreMes(inicioMesPrevio).toLowerCase()}`}
            />
            <Stat
              label="Volumen semana"
              value={volSemana / 1000}
              decimals={1}
              unit="t"
              size={34}
              delta={volPrevia > 0 ? delta(((volSemana - volPrevia) / volPrevia) * 100, 0, ' %') : undefined}
              deltaTone={tono(volSemana - volPrevia)}
              caption={volPrevia > 0 ? 'vs semana anterior' : undefined}
            />
            <Stat
              label={porReps ? 'Reps última sesión' : 'Último peso máx.'}
              value={ultimo ? (porReps ? ultimo.repsTotales : ultimo.pesoMaximo) : 0}
              decimals={!porReps && ultimo && ultimo.pesoMaximo % 1 !== 0 ? 1 : 0}
              unit={porReps ? 'reps' : 'kg'}
              size={34}
              delta={ultimo && anterior ? delta(porReps ? ultimo.repsTotales - anterior.repsTotales : ultimo.pesoMaximo - anterior.pesoMaximo, porReps ? 0 : 1, porReps ? '' : ' kg') : undefined}
              deltaTone={ultimo && anterior ? tono(porReps ? ultimo.repsTotales - anterior.repsTotales : ultimo.pesoMaximo - anterior.pesoMaximo) : undefined}
              caption={ejercicio?.ejercicioNombre}
            />
            <Stat
              label="Peso corporal"
              value={peso?.pesoKg ?? 0}
              decimals={1}
              unit="kg"
              size={34}
              delta={peso && pesoPrevio ? delta(peso.pesoKg - pesoPrevio.pesoKg, 1, ' kg') : undefined}
              deltaTone={peso && pesoPrevio ? tono(peso.pesoKg - pesoPrevio.pesoKg) : undefined}
              caption={peso ? 'desde Núcleo' : 'sin registros'}
            />
          </div>

          <div className="span-8">
            <Card
              delay={100}
              eyebrow={porReps ? 'Repeticiones' : 'Volumen por sesión'}
              title={ejercicio?.ejercicioNombre ?? 'Progresión'}
              action={
                records.data && records.data.length > 0 ? (
                  <div style={{ width: 190 }}>
                    <Select
                      size="sm"
                      aria-label="Ejercicio"
                      value={ejercicioId !== undefined ? String(ejercicioId) : ''}
                      onChange={(e) => setElegido(Number(e.target.value))}
                      options={records.data.map((r) => ({ value: String(r.ejercicioId), label: r.ejercicioNombre }))}
                    />
                  </div>
                ) : undefined
              }
            >
              {progresion.isError ? (
                <p className="muted" style={{ margin: 0 }}>No se ha podido cargar la progresión.</p>
              ) : progresion.isSuccess && puntos.length === 0 ? (
                <p className="muted" style={{ margin: 0 }}>Sin series de este ejercicio en los últimos 6 meses.</p>
              ) : progresion.isSuccess ? (
                <LineChart
                  height={220}
                  min={0}
                  labels={etiquetasProg.map((p) => relativa(p.fecha, hoy))}
                  format={(v) => (v <= 0 ? '0' : num(Math.round(v)))}
                  series={[{ name: porReps ? 'Repeticiones' : 'Volumen (kg)', points: valores }]}
                />
              ) : (
                <p className="muted" style={{ margin: 0 }}>Cargando…</p>
              )}
            </Card>
          </div>

          <div className="span-4">
            <Card delay={160} eyebrow="Mejores marcas" title="Récords" padding="4px 0 8px">
              {records.isPending ? (
                <p className="muted" style={{ margin: '14px 18px' }}>Cargando…</p>
              ) : (
                recordsOrden.map((r, i) => (
                  <div key={r.ejercicioId} className="rec pl-rise" style={{ animationDelay: 200 + i * 60 + 'ms' }}>
                    <div className="stack-4">
                      <b>{r.ejercicioNombre}</b>
                      <span className="pl-row__num">{relativa(r.fechaPesoMaximo, hoy)}</span>
                    </div>
                    <span className="money">{r.pesoMaximo > 0 ? `${num(r.pesoMaximo, r.pesoMaximo % 1 ? 1 : 0)} kg × ${r.repsPesoMaximo}` : `${r.repsPesoMaximo} reps`}</span>
                    {diasEntre(deIso(r.fechaPesoMaximo), hoy) <= 7 && (
                      <Badge tone="accent" variant="solid">
                        Nuevo
                      </Badge>
                    )}
                  </div>
                ))
              )}
            </Card>
          </div>

          <div className="span-7">
            <Card delay={220} eyebrow="Historial" title="Últimas sesiones" padding="8px 0 0">
              {detalle.isError ? (
                <p className="muted" style={{ margin: '6px 18px 14px' }}>No se han podido cargar las sesiones.</p>
              ) : detalle.isPending && idsDetalle.length > 0 ? (
                <p className="muted" style={{ margin: '6px 18px 14px' }}>Cargando…</p>
              ) : (
                <div className="table">
                  {ultimas.map((s, i) => (
                    <button
                      key={s.id}
                      type="button"
                      className="table__r table__r--5 atl-ses pl-rise"
                      style={{ animationDelay: 240 + i * 40 + 'ms' }}
                      onClick={() => setAbierta({ id: s.id })}
                      aria-label={`Editar la sesión del ${relativa(s.fecha, hoy)}`}
                    >
                      <span className="pl-row__num">{relativa(s.fecha, hoy)}</span>
                      <b>
                        {s.rutinaNombre ?? 'Improvisado'}
                        {s.rutinaNombre == null && (
                          <Badge variant="outline" style={{ marginLeft: 8 }}>
                            Sin rutina
                          </Badge>
                        )}
                      </b>
                      <span className="muted">{new Set(s.series.map((x) => x.ejercicioId)).size} ejercicios</span>
                      <span className="muted">{s.series.length} series</span>
                      <span className="money">{num(volumenDe(s))} kg</span>
                    </button>
                  ))}
                </div>
              )}
            </Card>
          </div>

          <div className="span-5">
            <Card delay={280} eyebrow="Actividad" title="Series por semana">
              <BarChart height={170} data={seriesSemana} highlight={SEMANAS - 1} format={(v) => `${num(v)} series`} />
            </Card>
          </div>
        </div>
      )}

      {abierta && <FormularioSesion sesionId={abierta.id} onClose={() => setAbierta(null)} />}
    </div>
  );
}
