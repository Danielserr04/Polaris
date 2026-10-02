import { useMemo, useState } from 'react';
import { ETIQUETA_MOMENTO, mensajeError, MOMENTOS, useComidasDia, useComidasRango, useResumenDia, type Alimento, type ComidaCompleta, type MacroResumen, type MomentoComida } from '../api/fusion';
import { PageHeader } from '../components/PageHeader';
import { Alert, Button, Card, IconButton, LineChart, RingChart, Stat, Tabs } from '../design-system';
import { diaLargo, diasEntre, iso, num, sumarDias, deIso } from '../lib/fechas';
import { AlimentosTab } from './fusion/AlimentosTab';
import { FormularioAlimento } from './fusion/FormularioAlimento';
import { FormularioComida } from './fusion/FormularioComida';
import { ImportarOff } from './fusion/ImportarOff';
import { FormularioObjetivo } from './fusion/FormularioObjetivo';
import { ApuntarPlan } from './fusion/ApuntarPlan';
import { FormularioPlan } from './fusion/FormularioPlan';
import { FormularioReceta } from './fusion/FormularioReceta';
import { PlanesTab } from './fusion/PlanesTab';
import { RecetasTab } from './fusion/RecetasTab';
import './fusion/fusion.css';

// Una comida abierta: nueva (con el momento sugerido) o una existente.
type Abierta = { id?: number; momento?: MomentoComida } | null;

// Un alimento abierto: nuevo (sin alimento) o editando uno del catalogo.
type AlimentoAbierto = { alimento?: Alimento } | null;

type Pestana = 'hoy' | 'ali' | 'rec' | 'plan';

// Una receta o un plan abierto: nuevo (sin id) o editando ese id.
type Abierto = { id?: number } | null;

const TITULO: Record<Pestana, string> = { hoy: '', ali: 'Alimentos', rec: 'Recetas', plan: 'Planes de comidas' };

const MACROS: { clave: 'proteinas' | 'carbohidratos' | 'grasas'; nombre: string; color: string }[] = [
  { clave: 'proteinas', nombre: 'Proteínas', color: 'var(--accent)' },
  { clave: 'carbohidratos', nombre: 'Carbohidratos', color: 'var(--mod-kuiper)' },
  { clave: 'grasas', nombre: 'Grasas', color: 'var(--mod-nucleo)' },
];

function tituloDia(fecha: string, hoy: Date): string {
  const dif = diasEntre(deIso(fecha), hoy);
  if (dif === 0) return 'Hoy';
  if (dif === 1) return 'Ayer';
  return diaLargo(fecha).split(' ')[0];
}

function fechaLarga(fecha: string): string {
  return deIso(fecha).toLocaleDateString('es-ES', { weekday: 'long', day: 'numeric', month: 'long' });
}

export function Fusion() {
  const hoy = useMemo(() => new Date(), []);
  const hoyIso = iso(hoy);
  const [fecha, setFecha] = useState(hoyIso);
  const [abierta, setAbierta] = useState<Abierta>(null);
  const [objetivoAbierto, setObjetivoAbierto] = useState(false);
  const [pestana, setPestana] = useState<Pestana>('hoy');
  const [alimentoAbierto, setAlimentoAbierto] = useState<AlimentoAbierto>(null);
  const [importarAbierto, setImportarAbierto] = useState(false);
  const [recuento, setRecuento] = useState<number | null>(null);
  const [recetaAbierta, setRecetaAbierta] = useState<Abierto>(null);
  const [planAbierto, setPlanAbierto] = useState<Abierto>(null);

  const resumen = useResumenDia(fecha);
  const comidas = useComidasDia(fecha);
  const desde14 = iso(sumarDias(deIso(fecha), -13));
  const rango = useComidasRango(desde14, fecha);

  const esHoy = fecha === hoyIso;
  const mover = (dias: number) => setFecha(iso(sumarDias(deIso(fecha), dias)));

  const tendencia = useMemo(() => {
    const porDia = new Map<string, number>();
    for (const c of rango.data ?? []) porDia.set(c.fecha, (porDia.get(c.fecha) ?? 0) + c.kcalTotal);
    const dias = Array.from({ length: 14 }, (_, i) => iso(sumarDias(deIso(fecha), i - 13)));
    return { dias, kcal: dias.map((d) => Math.round(porDia.get(d) ?? 0)) };
  }, [rango.data, fecha]);

  const porMomento = useMemo(() => {
    const m = new Map<MomentoComida, ComidaCompleta[]>();
    for (const c of comidas.data ?? []) m.set(c.momento, [...(m.get(c.momento) ?? []), c]);
    return m;
  }, [comidas.data]);

  const r = resumen.data;
  const objetivo = r?.kcal.objetivo ?? null;

  return (
    <div>
      <PageHeader
        eyebrow="Fusión"
        coord={
          pestana === 'hoy'
            ? fechaLarga(fecha).toUpperCase()
            : pestana === 'rec'
              ? 'PLATOS REUTILIZABLES'
              : pestana === 'plan'
                ? 'TU SEMANA'
                : recuento !== null
                  ? `${recuento} ${recuento === 1 ? 'ALIMENTO' : 'ALIMENTOS'}`
                  : 'CATÁLOGO'
        }
        title={pestana === 'hoy' ? tituloDia(fecha, hoy) : TITULO[pestana]}
        actions={
          pestana === 'hoy' ? (
            <>
              <span className="fus-dia">
                <IconButton icon="chevron-left" label="Día anterior" variant="ghost" onClick={() => mover(-1)} />
                <IconButton icon="chevron-right" label="Día siguiente" variant="ghost" disabled={esHoy} onClick={() => mover(1)} />
                {!esHoy && (
                  <Button variant="ghost" size="sm" onClick={() => setFecha(hoyIso)}>
                    Hoy
                  </Button>
                )}
              </span>
              <Button icon="plus" onClick={() => setAbierta({})}>
                Registrar comida
              </Button>
            </>
          ) : pestana === 'rec' ? (
            <Button icon="plus" onClick={() => setRecetaAbierta({})}>
              Receta
            </Button>
          ) : pestana === 'plan' ? (
            <Button icon="plus" onClick={() => setPlanAbierto({})}>
              Plan
            </Button>
          ) : (
            <>
              <Button variant="secondary" icon="search" onClick={() => setImportarAbierto(true)}>
                Importar
              </Button>
              <Button icon="plus" onClick={() => setAlimentoAbierto({})}>
                Alimento
              </Button>
            </>
          )
        }
      />
      <Tabs
        value={pestana}
        onChange={(v) => setPestana(v as Pestana)}
        items={[
          { value: 'hoy', label: 'Hoy' },
          { value: 'ali', label: 'Alimentos' },
          { value: 'rec', label: 'Recetas' },
          { value: 'plan', label: 'Planes' },
        ]}
        style={{ marginBottom: 24 }}
      />

      <div key={pestana} className="pl-tabpanel">
        {pestana === 'ali' ? (
          <AlimentosTab onEditar={(a) => setAlimentoAbierto({ alimento: a })} onRecuento={setRecuento} />
        ) : pestana === 'rec' ? (
          <RecetasTab onAbrir={(r) => setRecetaAbierta({ id: r.id })} onNueva={() => setRecetaAbierta({})} />
        ) : pestana === 'plan' ? (
          <PlanesTab onEditar={(id) => setPlanAbierto({ id })} onNuevo={() => setPlanAbierto({})} />
        ) : (
          <>
        {resumen.isError ? (
          <Alert
            tone="danger"
            title="No se ha podido cargar el día"
            action={
              <Button size="sm" variant="secondary" onClick={() => void resumen.refetch()}>
                Reintentar
              </Button>
            }
          >
            {mensajeError(resumen.error)}
          </Alert>
        ) : (
          <div className="grid">
            <div className="span-5">
              <Card delay={60} eyebrow="Energía" title="Calorías del día">
                {r ? (
                  <div className="fus-kcal" style={{ display: 'flex', gap: 28, alignItems: 'center' }}>
                    <RingChart
                      value={r.kcal.consumido}
                      max={objetivo ?? Math.max(r.kcal.consumido, 1)}
                      size={168}
                      thickness={14}
                      label={num(r.kcal.consumido)}
                      sublabel={objetivo !== null ? `DE ${num(objetivo)} KCAL` : 'KCAL'}
                    />
                    <div className="stack-16" style={{ flex: 1 }}>
                      {objetivo !== null && r.kcal.restante != null ? (
                        <Stat label={r.kcal.restante >= 0 ? 'Te quedan' : 'Te has pasado'} value={Math.abs(Math.round(r.kcal.restante))} unit="kcal" size={32} />
                      ) : (
                        <Stat label="Llevas" value={Math.round(r.kcal.consumido)} unit="kcal" size={32} />
                      )}
                      <span className="muted" style={{ fontSize: 13 }}>
                        {r.objetivoVigenteDesde
                          ? `Objetivo vigente desde el ${deIso(r.objetivoVigenteDesde).toLocaleDateString('es-ES', { day: 'numeric', month: 'long' })}.`
                          : 'Sin objetivo nutricional para este día.'}
                      </span>
                      <div>
                        <Button size="sm" variant="ghost" onClick={() => setObjetivoAbierto(true)}>
                          {r.objetivoVigenteDesde ? 'Cambiar objetivo' : 'Fijar objetivo'}
                        </Button>
                      </div>
                    </div>
                  </div>
                ) : (
                  <p className="muted" style={{ margin: 0 }}>Cargando…</p>
                )}
              </Card>
            </div>

            <div className="span-7">
              <Card delay={120} eyebrow="Macros" title={objetivo !== null ? 'Contra objetivo' : 'Del día'}>
                {r ? (
                  <div className="macros">
                    {MACROS.map((m) => {
                      const v: MacroResumen = r[m.clave];
                      return (
                        <div key={m.clave} className="macro">
                          <RingChart
                            value={v.consumido}
                            max={v.objetivo ?? Math.max(v.consumido, 1)}
                            size={104}
                            thickness={8}
                            color={m.color}
                            label={num(v.consumido)}
                            sublabel={v.objetivo != null ? `/ ${num(v.objetivo)} G` : 'G'}
                          />
                          <b>{m.nombre}</b>
                          <span className="pl-row__num">{v.porcentaje != null ? `${Math.round(v.porcentaje)} %` : '—'}</span>
                        </div>
                      );
                    })}
                  </div>
                ) : (
                  <p className="muted" style={{ margin: 0 }}>Cargando…</p>
                )}
              </Card>
            </div>

            <div className="span-6">
              <Card delay={180} eyebrow="Registro" title="Comidas" padding="4px 0 8px">
                {comidas.isError ? (
                  <div className="fus-vacio" style={{ padding: '14px 18px' }}>
                    No se han podido cargar las comidas.{' '}
                    <Button size="sm" variant="ghost" onClick={() => void comidas.refetch()}>
                      Reintentar
                    </Button>
                  </div>
                ) : comidas.isPending ? (
                  <div className="fus-vacio" style={{ padding: '14px 18px' }}>Cargando…</div>
                ) : (
                  <>
                  {comidas.data.length === 0 && <ApuntarPlan fecha={fecha} />}
                  {MOMENTOS.map((momento, i) => {
                    const lista = porMomento.get(momento) ?? [];
                    const kcal = lista.reduce((a, c) => a + c.kcalTotal, 0);
                    if (!lista.length) {
                      return (
                        <div key={momento} className="fus-meal fus-meal--vacia pl-rise" style={{ animationDelay: 200 + i * 60 + 'ms' }}>
                          <div className="fus-meal-h">
                            <b>{ETIQUETA_MOMENTO[momento]}</b>
                            <Button size="sm" variant="ghost" icon="plus" style={{ marginLeft: 'auto' }} onClick={() => setAbierta({ momento })}>
                              Añadir
                            </Button>
                          </div>
                        </div>
                      );
                    }
                    return lista.map((c) => (
                      <button
                        key={c.id}
                        type="button"
                        className="fus-meal pl-rise"
                        style={{ animationDelay: 200 + i * 60 + 'ms' }}
                        onClick={() => setAbierta({ id: c.id })}
                        aria-label={`Editar ${ETIQUETA_MOMENTO[momento].toLowerCase()}, ${num(kcal)} kcal`}
                      >
                        <div className="fus-meal-h">
                          <b>{ETIQUETA_MOMENTO[momento]}</b>
                          <span className="money">{num(c.kcalTotal)} kcal</span>
                        </div>
                        {c.lineas.map((l) => (
                          <div key={l.id} className="meal__l">
                            <span>{l.alimentoNombre}</span>
                            <span className="pl-row__num">{num(l.cantidadG)} g</span>
                            <span className="pl-row__num">{num(l.kcal)} kcal</span>
                          </div>
                        ))}
                      </button>
                    ));
                  })}
                  </>
                )}
              </Card>
            </div>

            <div className="span-6">
              <Card delay={240} eyebrow="Tendencia" title="Últimos 14 días">
                {rango.isSuccess && tendencia.kcal.every((k) => k === 0) ? (
                  <p className="muted" style={{ margin: 0 }}>Sin comidas registradas en estos 14 días.</p>
                ) : rango.isSuccess ? (
                  <LineChart
                    height={220}
                    min={0}
                    showLegend
                    labels={[tendencia.dias[0], tendencia.dias[6], tendencia.dias[13]].map((d) => deIso(d).toLocaleDateString('es-ES', { day: 'numeric', month: 'short' }).replace('.', ''))}
                    // El grafico deja un margen por debajo del minimo: nada de kcal negativas en el eje.
                    format={(v) => (v <= 0 ? '0' : `${Math.round(v / 100) / 10}k`)}
                    series={[
                      { name: 'Kcal', points: tendencia.kcal },
                      ...(objetivo !== null ? [{ name: 'Objetivo', points: tendencia.kcal.map(() => objetivo), dashed: true, color: 'var(--text-3)' }] : []),
                    ]}
                  />
                ) : (
                  <p className="muted" style={{ margin: 0 }}>{rango.isError ? 'No se ha podido cargar.' : 'Cargando…'}</p>
                )}
              </Card>
            </div>
          </div>
        )}

          </>
        )}
      </div>

      {abierta && <FormularioComida comidaId={abierta.id} momentoInicial={abierta.momento} fecha={fecha} onClose={() => setAbierta(null)} />}
      {alimentoAbierto && <FormularioAlimento alimento={alimentoAbierto.alimento} onClose={() => setAlimentoAbierto(null)} />}
      {importarAbierto && <ImportarOff onClose={() => setImportarAbierto(false)} />}
      {recetaAbierta && <FormularioReceta recetaId={recetaAbierta.id} onClose={() => setRecetaAbierta(null)} />}
      {planAbierto && <FormularioPlan planId={planAbierto.id} onClose={() => setPlanAbierto(null)} />}
      {objetivoAbierto && <FormularioObjetivo resumen={r} fecha={fecha} onClose={() => setObjetivoAbierto(false)} />}
    </div>
  );
}
