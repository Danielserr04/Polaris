import { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUsuario } from '../../api/auth';
import {
  useComidas,
  useEntradas,
  useMovimientos,
  usePesos,
  useResumenDia,
  useResumenMes,
  useSesiones,
  type EntradaResumen,
  type EstadoEntrada,
  type MomentoComida,
} from '../../api/inicio';
import { PageHeader } from '../../components/PageHeader';
import {
  BarChart,
  Button,
  Card,
  Icon,
  LineChart,
  ProgressBar,
  RingChart,
  StateGlyph,
  Stat,
  TypeTag,
} from '../../design-system';
import { deIso, diaLargo, diasEntre, eur, iso, lunesDe, nombreMes, num, periodo, relativa, sumarDias } from '../../lib/fechas';
import { Band, EstadoConsulta } from './Band';
import './inicio.css';

const ESTADOS: [EstadoEntrada, string][] = [
  ['EN_CURSO', 'En curso'],
  ['PENDIENTE', 'Pendiente'],
  ['TERMINADO', 'Terminado'],
  ['ABANDONADO', 'Abandonado'],
];

const MOMENTOS: Record<MomentoComida, string> = {
  DESAYUNO: 'Desayuno',
  COMIDA: 'Comida',
  CENA: 'Cena',
  SNACK: 'Snack',
};

const DIAS_SEMANA = ['L', 'M', 'X', 'J', 'V', 'S', 'D'];

function progresoTexto(e: EntradaResumen): string {
  if (e.progreso === undefined || e.progreso === null) return '—';
  if (e.tituloTipo === 'LIBRO') return `pág. ${e.progreso}`;
  if (e.tituloTipo === 'SERIE') return `ep. ${e.progreso}`;
  return `${e.progreso}`;
}

interface ActividadItem {
  fecha: string;
  modulo: string;
  icon: string;
  txt: string;
  det: string;
  ruta: string;
}

export function Inicio() {
  const navigate = useNavigate();
  const { data: usuario } = useUsuario();
  const hoy = useMemo(() => new Date(), []);
  const hoyIso = iso(hoy);
  const h = hoy.getHours();
  const saludo = h < 13 ? 'Buenos días' : h < 21 ? 'Buenas tardes' : 'Buenas noches';
  const fecha = hoy.toLocaleDateString('es-ES', { weekday: 'long', day: 'numeric', month: 'long' });
  const nombre = usuario?.nombre?.split(' ')[0] ?? usuario?.username ?? '';

  const desde14 = iso(sumarDias(hoy, -13));
  const desde7 = iso(sumarDias(hoy, -6));
  const desde60 = iso(sumarDias(hoy, -59));
  const desde90 = iso(sumarDias(hoy, -89));
  const lunes = lunesDe(hoy);

  const entradas = useEntradas();
  const resumenMes = useResumenMes(periodo(hoy));
  const movimientos = useMovimientos(desde14, hoyIso);
  const resumenDia = useResumenDia(hoyIso);
  const comidas = useComidas(desde7, hoyIso);
  const sesiones = useSesiones(desde90, hoyIso);
  const pesos = usePesos(desde60, hoyIso);

  // --- Odisea
  const enCurso = (entradas.data ?? []).filter((e) => e.estado === 'EN_CURSO');
  const cuenta = (s: EstadoEntrada) => (entradas.data ?? []).filter((e) => e.estado === s).length;

  // --- Kuiper
  const gastosPorDia = useMemo(() => {
    const porDia = new Map<string, number>();
    for (const m of movimientos.data ?? []) {
      if (m.tipo === 'GASTO') porDia.set(m.fecha, (porDia.get(m.fecha) ?? 0) + m.importe);
    }
    return Array.from({ length: 14 }, (_, i) => {
      const f = iso(sumarDias(hoy, i - 13));
      return { label: f, value: porDia.get(f) ?? 0 };
    });
  }, [movimientos.data, hoy]);
  const gastadoHoy = gastosPorDia[13].value;
  const gastoMes = resumenMes.data?.gastos ?? 0;
  const mediaDia = gastoMes / hoy.getDate();
  const conLimite = (resumenMes.data?.gastoPorCategoria ?? []).filter((c) => (c.limiteMensual ?? 0) > 0);
  const totalLimite = conLimite.reduce((s, c) => s + (c.limiteMensual ?? 0), 0);
  const gastadoConLimite = conLimite.reduce((s, c) => s + c.gastado, 0);
  const diasMes = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0).getDate();
  const peor = [...conLimite].sort((a, b) => b.gastado / (b.limiteMensual ?? 1) - a.gastado / (a.limiteMensual ?? 1))[0];

  // --- Fusión
  const dia = resumenDia.data;
  const kcalObjetivo = dia?.kcal.objetivo;

  // --- Atlas
  const sesionesOrdenadas = useMemo(
    () => [...(sesiones.data ?? [])].sort((a, b) => (a.fecha < b.fecha ? 1 : a.fecha > b.fecha ? -1 : b.id - a.id)),
    [sesiones.data],
  );
  const ultima = sesionesOrdenadas[0];
  const diasConSesion = new Set(sesionesOrdenadas.map((s) => s.fecha));
  const semana = DIAS_SEMANA.map((l, i) => {
    const f = iso(sumarDias(lunes, i));
    return { l, on: diasConSesion.has(f), hoy: f === hoyIso };
  });
  const sesionesSemana = semana.filter((d) => d.on).length;

  // --- Núcleo
  const serie = useMemo(
    () => [...(pesos.data ?? [])].sort((a, b) => (a.fecha < b.fecha ? -1 : 1)),
    [pesos.data],
  );
  const ultimoPeso = serie[serie.length - 1];
  const primerPeso = serie[0];
  const deltaPeso = ultimoPeso && primerPeso ? ultimoPeso.pesoKg - primerPeso.pesoKg : 0;
  const diasSerie = ultimoPeso && primerPeso ? diasEntre(deIso(primerPeso.fecha), deIso(ultimoPeso.fecha)) : 0;

  // --- Actividad reciente: lo ultimo que has hecho en cualquier modulo
  const actividad = useMemo<ActividadItem[]>(() => {
    const items: ActividadItem[] = [];
    for (const m of movimientos.data ?? []) {
      items.push({
        fecha: m.fecha,
        modulo: 'kuiper',
        icon: 'shopping-basket',
        txt: m.concepto || m.categoriaNombre,
        det: `${m.tipo === 'GASTO' ? '−' : '+'}${eur(m.importe)}`,
        ruta: '/kuiper',
      });
    }
    for (const c of comidas.data ?? []) {
      items.push({
        fecha: c.fecha,
        modulo: 'fusion',
        icon: 'utensils',
        txt: `${MOMENTOS[c.momento]} registrado`,
        det: `${num(c.kcalTotal)} kcal`,
        ruta: '/fusion',
      });
    }
    for (const s of sesiones.data ?? []) {
      items.push({
        fecha: s.fecha,
        modulo: 'atlas',
        icon: 'dumbbell',
        txt: s.rutinaNombre ? `Sesión de ${s.rutinaNombre}` : 'Entreno libre',
        det: `${s.numeroSeries} series`,
        ruta: '/atlas',
      });
    }
    for (const p of pesos.data ?? []) {
      items.push({ fecha: p.fecha, modulo: 'nucleo', icon: 'scale', txt: 'Peso registrado', det: `${num(p.pesoKg, 1)} kg`, ruta: '/' });
    }
    return items.sort((a, b) => (a.fecha < b.fecha ? 1 : a.fecha > b.fecha ? -1 : 0)).slice(0, 8);
  }, [movimientos.data, comidas.data, sesiones.data, pesos.data]);
  const cargandoActividad = movimientos.isPending || comidas.isPending || sesiones.isPending || pesos.isPending;

  return (
    <div className="dash">
      <PageHeader
        eyebrow="Inicio"
        coord={fecha.toUpperCase()}
        title={
          <>
            {saludo}
            {nombre && (
              <>
                , <span style={{ color: 'var(--accent)' }}>{nombre}</span>
              </>
            )}
          </>
        }
        actions={
          <Button variant="secondary" icon="plus" onClick={() => navigate('/odisea')}>
            Añadir
          </Button>
        }
      />

      <div className="stats stats--today pl-rise" style={{ animationDelay: '60ms', marginBottom: 24 }}>
        <div data-module="kuiper" className="today" onClick={() => navigate('/kuiper')}>
          <span className="today__k">
            <Icon name="wallet" size={14} />
            Gastado hoy
          </span>
          {movimientos.isSuccess ? (
            <Stat value={gastadoHoy} decimals={2} unit="€" size={34} caption={resumenMes.isSuccess ? `media ${eur(mediaDia)}` : undefined} />
          ) : (
            <EstadoConsulta cargando={movimientos.isPending} error={movimientos.isError} />
          )}
        </div>
        <div data-module="fusion" className="today" onClick={() => navigate('/fusion')}>
          <span className="today__k">
            <Icon name="flame" size={14} />
            {kcalObjetivo !== undefined ? 'Te quedan' : 'Hoy llevas'}
          </span>
          {dia ? (
            <Stat
              value={kcalObjetivo !== undefined ? Math.round(dia.kcal.restante ?? 0) : Math.round(dia.kcal.consumido)}
              unit="kcal"
              size={34}
              caption={kcalObjetivo !== undefined ? `${num(dia.kcal.consumido)} de ${num(kcalObjetivo)}` : 'sin objetivo nutricional'}
            />
          ) : (
            <EstadoConsulta cargando={resumenDia.isPending} error={resumenDia.isError} />
          )}
        </div>
        <div data-module="atlas" className="today" onClick={() => navigate('/atlas')}>
          <span className="today__k">
            <Icon name="dumbbell" size={14} />
            Última sesión
          </span>
          {sesiones.isSuccess ? (
            <Stat
              value={ultima ? (ultima.rutinaNombre ?? 'Entreno libre') : '—'}
              size={34}
              caption={ultima ? `${relativa(ultima.fecha, hoy).toLowerCase()} · ${ultima.numeroSeries} series` : 'sin sesiones todavía'}
            />
          ) : (
            <EstadoConsulta cargando={sesiones.isPending} error={sesiones.isError} />
          )}
        </div>
        <div data-module="odisea" className="today" onClick={() => navigate('/odisea')}>
          <span className="today__k">
            <Icon name="clapperboard" size={14} />
            En curso
          </span>
          {entradas.isSuccess ? (
            <Stat value={enCurso.length} size={34} caption={enCurso.map((e) => e.tituloTitulo).join(' · ') || 'nada ahora mismo'} />
          ) : (
            <EstadoConsulta cargando={entradas.isPending} error={entradas.isError} />
          )}
        </div>
      </div>

      <div className="dash__cols">
        <section className="bands pl-rise" style={{ animationDelay: '120ms' }}>
          <Band
            modulo="odisea"
            icon="clapperboard"
            name="Odisea"
            sub="Ocio"
            onClick={() => navigate('/odisea')}
            aside={
              <div className="states">
                {ESTADOS.map(([s, l]) => (
                  <span key={s}>
                    <StateGlyph estado={s} />
                    <b>{cuenta(s)}</b>
                    {l}
                  </span>
                ))}
              </div>
            }
          >
            {entradas.isSuccess ? (
              enCurso.length ? (
                <div className="stack-12">
                  {enCurso.slice(0, 4).map((e) => (
                    <div key={e.id} className="prow prow--sin-barra">
                      <TypeTag tipo={e.tituloTipo} showLabel={false} size={15} />
                      <b>{e.tituloTitulo}</b>
                      <span className="pl-row__num">{progresoTexto(e)}</span>
                    </div>
                  ))}
                </div>
              ) : (
                <span className="muted">Nada en curso.</span>
              )
            ) : (
              <EstadoConsulta cargando={entradas.isPending} error={entradas.isError} />
            )}
          </Band>

          <Band
            modulo="kuiper"
            icon="wallet"
            name="Kuiper"
            sub={nombreMes(hoy)}
            onClick={() => navigate('/kuiper')}
            aside={
              movimientos.isSuccess ? (
                <BarChart height={64} gap={3} data={gastosPorDia} highlight={13} format={(v) => eur(v)} showAxis={false} gridLines={2} />
              ) : undefined
            }
          >
            {resumenMes.isSuccess ? (
              <div className="bmain">
                <Stat value={gastoMes} decimals={2} unit="€" size={36} />
                <div className="stack-8" style={{ flex: 1, minWidth: 200 }}>
                  {totalLimite > 0 ? (
                    <>
                      <ProgressBar
                        value={gastadoConLimite}
                        max={totalLimite}
                        target={(totalLimite * hoy.getDate()) / diasMes}
                        valueLabel={`de ${eur(totalLimite, 0)}`}
                        label={`Quedan ${eur(Math.max(totalLimite - gastadoConLimite, 0))}`}
                      />
                      {peor && (
                        <span className="muted" style={{ fontSize: 12.5 }}>
                          {peor.categoriaNombre}: {eur(peor.gastado, 0)} de {eur(peor.limiteMensual ?? 0, 0)}
                        </span>
                      )}
                    </>
                  ) : (
                    <span className="muted" style={{ fontSize: 12.5 }}>
                      Ingresos {eur(resumenMes.data.ingresos)} · Balance {eur(resumenMes.data.balance)}
                    </span>
                  )}
                </div>
              </div>
            ) : (
              <EstadoConsulta cargando={resumenMes.isPending} error={resumenMes.isError} />
            )}
          </Band>

          <Band
            modulo="fusion"
            icon="flame"
            name="Fusión"
            sub="Hoy"
            onClick={() => navigate('/fusion')}
            aside={
              dia ? (
                <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                  <RingChart
                    value={dia.kcal.consumido}
                    max={kcalObjetivo ?? Math.max(dia.kcal.consumido, 1)}
                    size={84}
                    thickness={8}
                    label={num(dia.kcal.consumido)}
                    sublabel="KCAL"
                  />
                </div>
              ) : undefined
            }
          >
            {dia ? (
              kcalObjetivo !== undefined ? (
                <div className="macros3">
                  {(
                    [
                      ['Proteínas', dia.proteinas],
                      ['Carbohidratos', dia.carbohidratos],
                      ['Grasas', dia.grasas],
                    ] as const
                  ).map(([n, m]) => (
                    <ProgressBar
                      key={n}
                      label={n}
                      value={m.consumido}
                      max={m.objetivo ?? Math.max(m.consumido, 1)}
                      valueLabel={`${num(m.consumido)}/${num(m.objetivo ?? 0)} g`}
                    />
                  ))}
                </div>
              ) : (
                <span className="muted">Sin objetivo nutricional: {num(dia.proteinas.consumido)} g de proteína, {num(dia.carbohidratos.consumido)} g de carbohidratos y {num(dia.grasas.consumido)} g de grasas hoy.</span>
              )
            ) : (
              <EstadoConsulta cargando={resumenDia.isPending} error={resumenDia.isError} />
            )}
          </Band>

          <Band
            modulo="atlas"
            icon="dumbbell"
            name="Atlas"
            sub={`Semana ${numeroSemana(hoy)}`}
            onClick={() => navigate('/atlas')}
            aside={
              <div className="week week--sm">
                {semana.map((d, i) => (
                  <div key={i} className={'week__d' + (d.on ? ' on' : '') + (d.hoy ? ' today' : '')}>
                    <span>{d.l}</span>
                    <i />
                  </div>
                ))}
              </div>
            }
          >
            {sesiones.isSuccess ? (
              ultima ? (
                <div className="bmain">
                  <div className="stack-4">
                    <span className="pl-eyebrow">Última · {diaLargo(ultima.fecha)}</span>
                    <b className="big">{ultima.rutinaNombre ?? 'Entreno libre'}</b>
                    <span className="muted" style={{ fontSize: 12.5 }}>
                      {ultima.numeroSeries} series{ultima.duracionMin ? ` · ${ultima.duracionMin} min` : ''}
                    </span>
                  </div>
                  <div className="stack-4 next">
                    <span className="pl-eyebrow" style={{ color: 'var(--accent)' }}>Esta semana</span>
                    <b className="big">{sesionesSemana} {sesionesSemana === 1 ? 'sesión' : 'sesiones'}</b>
                  </div>
                </div>
              ) : (
                <span className="muted">Sin sesiones registradas.</span>
              )
            ) : (
              <EstadoConsulta cargando={sesiones.isPending} error={sesiones.isError} />
            )}
          </Band>

          <Band
            modulo="nucleo"
            icon="orbit"
            name="Núcleo"
            sub="Peso"
            aside={
              serie.length > 1 ? (
                <LineChart height={56} series={[{ name: 'Peso', points: serie.map((p) => p.pesoKg) }]} showAxis={false} gridLines={2} />
              ) : undefined
            }
          >
            {pesos.isSuccess ? (
              ultimoPeso ? (
                <div className="bmain">
                  <Stat
                    value={ultimoPeso.pesoKg}
                    decimals={1}
                    unit="kg"
                    size={36}
                    caption={
                      serie.length > 1
                        ? `${deltaPeso > 0 ? '+' : deltaPeso < 0 ? '−' : ''}${num(Math.abs(deltaPeso), 1)} kg en ${diasSerie} días`
                        : relativa(ultimoPeso.fecha, hoy).toLowerCase()
                    }
                  />
                </div>
              ) : (
                <span className="muted">Sin registros de peso.</span>
              )
            ) : (
              <EstadoConsulta cargando={pesos.isPending} error={pesos.isError} />
            )}
          </Band>
        </section>

        <aside className="dash__side">
          <Card delay={180} eyebrow="Actividad" title="Lo último" padding="4px 0 6px">
            <div className="feed">
              {actividad.map((a, i) => (
                <div
                  key={i}
                  className="feed__r feed__r--stack pl-rise"
                  data-module={a.modulo}
                  style={{ animationDelay: 220 + i * 50 + 'ms', cursor: 'pointer' }}
                  onClick={() => navigate(a.ruta)}
                >
                  <span className="feed__ic">
                    <Icon name={a.icon} size={14} />
                  </span>
                  <span className="stack-4" style={{ minWidth: 0 }}>
                    <span className="feed__t">{a.txt}</span>
                    <span className="pl-row__num">{a.det}</span>
                  </span>
                  <span className="pl-row__num feed__h">{relativa(a.fecha, hoy)}</span>
                </div>
              ))}
              {!actividad.length && (
                <div className="feed__vacio muted">
                  {cargandoActividad ? 'Cargando…' : 'Todavía no hay actividad.'}
                </div>
              )}
            </div>
          </Card>
        </aside>
      </div>
    </div>
  );
}

/** Numero de semana ISO 8601 (la semana empieza en lunes y la 1 contiene el primer jueves). */
function numeroSemana(d: Date): number {
  const t = new Date(Date.UTC(d.getFullYear(), d.getMonth(), d.getDate()));
  const dia = t.getUTCDay() || 7;
  t.setUTCDate(t.getUTCDate() + 4 - dia);
  const inicio = new Date(Date.UTC(t.getUTCFullYear(), 0, 1));
  return Math.ceil(((t.getTime() - inicio.getTime()) / 86_400_000 + 1) / 7);
}
