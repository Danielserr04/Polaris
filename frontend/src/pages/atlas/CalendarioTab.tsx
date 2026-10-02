import { useMemo, useState } from 'react';
import { mensajeError, type Sesion, useSesiones, useTrabajoMuscular } from '../../api/atlas';
import { Alert, Button, Card, IconButton, Stat } from '../../design-system';
import { deIso, diasEntre, iso, lunesDe, num, sumarDias } from '../../lib/fechas';
import { MapaMuscular } from './MapaMuscular';

const DIAS = ['L', 'M', 'X', 'J', 'V', 'S', 'D'];
const MES = new Intl.DateTimeFormat('es-ES', { month: 'long', year: 'numeric' });
const LARGA = new Intl.DateTimeFormat('es-ES', { weekday: 'long', day: 'numeric', month: 'long' });

interface Props {
  /** Abre una sesion existente, o una nueva en ese dia. */
  onAbrir: (sesion: { id?: number; fecha?: string }) => void;
}

/** Semanas seguidas (hasta la actual o la anterior) con al menos una sesion. */
function rachaSemanas(fechas: string[], hoy: Date): number {
  const semanas = new Set(fechas.map((f) => iso(lunesDe(deIso(f)))));
  let lunes = lunesDe(hoy);
  // La semana en curso no rompe la racha si aun no se ha entrenado.
  if (!semanas.has(iso(lunes))) lunes = sumarDias(lunes, -7);
  let racha = 0;
  while (semanas.has(iso(lunes))) {
    racha++;
    lunes = sumarDias(lunes, -7);
  }
  return racha;
}

/**
 * Calendario de entrenos (traido de FitCore): un mes con los dias entrenados
 * marcados y, al lado, el mapa muscular de ese mes.
 */
export function CalendarioTab({ onAbrir }: Props) {
  const hoy = useMemo(() => new Date(), []);
  const [mes, setMes] = useState(() => new Date(hoy.getFullYear(), hoy.getMonth(), 1));
  const primero = mes;
  const ultimo = new Date(mes.getFullYear(), mes.getMonth() + 1, 0);
  const inicioRejilla = lunesDe(primero);
  const dias = Array.from({ length: Math.ceil((diasEntre(inicioRejilla, ultimo) + 1) / 7) * 7 }, (_, i) => sumarDias(inicioRejilla, i));

  const sesiones = useSesiones(iso(primero), iso(ultimo));
  const trabajo = useTrabajoMuscular(iso(primero), iso(ultimo));
  // Para la racha hacen falta semanas anteriores al mes que se mira: el ultimo anio.
  const historico = useSesiones(iso(sumarDias(hoy, -365)), iso(hoy));

  const porDia = useMemo(() => {
    const m = new Map<string, Sesion[]>();
    for (const s of sesiones.data ?? []) m.set(s.fecha, [...(m.get(s.fecha) ?? []), s]);
    return m;
  }, [sesiones.data]);

  const lista = sesiones.data ?? [];
  const volumen = lista.reduce((a, s) => a + s.volumen, 0);
  const racha = rachaSemanas((historico.data ?? []).map((s) => s.fecha), hoy);
  const esMesActual = mes.getFullYear() === hoy.getFullYear() && mes.getMonth() === hoy.getMonth();
  const hoyIso = iso(hoy);

  if (sesiones.isError) {
    return (
      <Alert
        tone="danger"
        title="No se han podido cargar las sesiones"
        action={
          <Button size="sm" variant="secondary" onClick={() => void sesiones.refetch()}>
            Reintentar
          </Button>
        }
      >
        {mensajeError(sesiones.error)}
      </Alert>
    );
  }

  return (
    <div className="grid">
      <div className="span-12 stats pl-rise">
        <Stat label="Sesiones del mes" value={lista.length} size={34} />
        <Stat label="Días entrenados" value={porDia.size} size={34} caption={`de ${ultimo.getDate()}`} />
        <Stat label="Volumen del mes" value={volumen / 1000} decimals={1} unit="t" size={34} />
        <Stat label="Racha" value={racha} unit={racha === 1 ? 'semana' : 'semanas'} size={34} caption="seguidas con algún entreno" />
      </div>

      <div className="span-7">
        <Card
          delay={100}
          eyebrow="Calendario"
          title={MES.format(mes).replace(/^./, (c) => c.toUpperCase())}
          action={
            <div style={{ display: 'flex', gap: 4 }}>
              <IconButton type="button" size="sm" icon="chevron-left" label="Mes anterior" onClick={() => setMes((m) => new Date(m.getFullYear(), m.getMonth() - 1, 1))} />
              <IconButton type="button" size="sm" icon="chevron-right" label="Mes siguiente" disabled={esMesActual} onClick={() => setMes((m) => new Date(m.getFullYear(), m.getMonth() + 1, 1))} />
            </div>
          }
        >
          <div className="atl-cal" role="grid" aria-label="Días del mes">
            {DIAS.map((d) => (
              <span key={d} className="atl-cal__dow" aria-hidden="true">{d}</span>
            ))}
            {dias.map((d) => {
              const f = iso(d);
              const delDia = porDia.get(f) ?? [];
              const fuera = d.getMonth() !== mes.getMonth();
              const futuro = f > hoyIso;
              const vol = delDia.reduce((a, s) => a + s.volumen, 0);
              const etiqueta = delDia.length
                ? `${LARGA.format(d)}: ${delDia.map((s) => s.rutinaNombre ?? 'improvisado').join(', ')}, ${num(vol)} kg`
                : `${LARGA.format(d)}: sin entreno`;
              return (
                <button
                  key={f}
                  type="button"
                  role="gridcell"
                  disabled={futuro}
                  aria-label={etiqueta}
                  aria-current={f === hoyIso ? 'date' : undefined}
                  title={delDia.length ? etiqueta : undefined}
                  className={['atl-cal__dia', fuera && 'atl-cal__dia--fuera', f === hoyIso && 'atl-cal__dia--hoy', delDia.length > 0 && 'atl-cal__dia--entreno'].filter(Boolean).join(' ')}
                  onClick={() => onAbrir(delDia.length ? { id: delDia[0].id } : { fecha: f })}
                >
                  <span>{d.getDate()}</span>
                  {delDia.length > 0 && <small>{delDia[0].rutinaNombre ?? 'Libre'}</small>}
                </button>
              );
            })}
          </div>
          <p className="muted" style={{ margin: '10px 0 0', fontSize: 12 }}>Pulsa un día entrenado para ver la sesión, o uno libre para registrar una.</p>
        </Card>
      </div>

      <div className="span-5">
        <Card delay={160} eyebrow="Este mes" title="Mapa muscular">
          {trabajo.isError ? (
            <p className="muted" style={{ margin: 0 }}>No se ha podido cargar el trabajo muscular.</p>
          ) : trabajo.isPending ? (
            <p className="muted" style={{ margin: 0 }}>Cargando…</p>
          ) : trabajo.data.length === 0 ? (
            <p className="muted" style={{ margin: 0 }}>Sin series este mes.</p>
          ) : (
            <>
              <MapaMuscular trabajo={trabajo.data} />
              <div className="atl-grupos">
                {trabajo.data.map((t) => (
                  <div key={t.grupoMuscular} className="atl-grupos__f">
                    <b>{t.grupoMuscular}</b>
                    <span className="muted">{t.numeroSeries} series · {t.numeroSesiones} {t.numeroSesiones === 1 ? 'sesión' : 'sesiones'}</span>
                  </div>
                ))}
              </div>
            </>
          )}
        </Card>
      </div>
    </div>
  );
}
