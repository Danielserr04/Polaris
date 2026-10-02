import { useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  type LogroDeModulo,
  type ModuloLogros,
  NOMBRE_NIVEL,
  type NivelLogro,
  SECCIONES,
  useLogrosGlobales,
} from '../../api/logros';
import { PageHeader } from '../../components/PageHeader';
import { Alert, Button, Card, Icon, RingChart, SegmentedControl } from '../../design-system';
import { deIso, diasEntre, num } from '../../lib/fechas';
import { iconoOr } from '../../lib/iconos';
import './logros.css';

type Estado = 'todos' | 'conseguidos' | 'pendientes';

const NIVELES: NivelLogro[] = ['BRONCE', 'PLATA', 'ORO', 'PLATINO'];

/** "14 sep 2026" */
function fechaCorta(iso: string): string {
  return deIso(iso).toLocaleDateString('es-ES', { day: 'numeric', month: 'short', year: 'numeric' }).replace('.', '');
}

/** "Hoy", "Ayer", "Hace 5 días" o la fecha corta si es de hace mas de un mes. */
function hace(iso: string): string {
  const dias = diasEntre(deIso(iso), new Date());
  if (dias <= 0) return 'Hoy';
  if (dias === 1) return 'Ayer';
  if (dias < 31) return `Hace ${dias} días`;
  return fechaCorta(iso);
}

const pct = (l: LogroDeModulo) => Math.min(1, l.objetivo ? l.progreso / l.objetivo : 0);

/** La medalla: el color dice el nivel, el icono lo que mide. Sin conseguir, en gris con candado. */
function Medalla({ logro, size = 54 }: { logro: LogroDeModulo; size?: number }) {
  return (
    <span
      className={'lg-medalla' + (logro.conseguido ? '' : ' lg-medalla--no')}
      data-nivel={logro.nivel}
      style={{ width: size, height: size }}
      aria-hidden
    >
      <Icon name={iconoOr(logro.icono, 'trophy')} size={Math.round(size * 0.42)} />
      {!logro.conseguido && (
        <span className="lg-medalla__candado">
          <Icon name="lock" size={10} />
        </span>
      )}
    </span>
  );
}

function TarjetaLogro({ logro, i }: { logro: LogroDeModulo; i: number }) {
  return (
    <div
      className={'lg-logro pl-rise' + (logro.conseguido ? ' lg-logro--si' : '')}
      data-module={logro.modulo}
      data-nivel={logro.nivel}
      style={{ animationDelay: `${Math.min(i, 16) * 25}ms` }}
    >
      <Medalla logro={logro} />
      <div className="lg-logro__txt">
        <div className="lg-logro__cab">
          <b>{logro.nombre}</b>
          <span className="lg-nivel" data-nivel={logro.nivel}>
            {NOMBRE_NIVEL[logro.nivel]}
          </span>
        </div>
        <span className="lg-logro__desc">{logro.descripcion}</span>
        {logro.conseguido ? (
          <span className="lg-logro__fecha">
            <Icon name="circle-check" size={13} />
            {logro.fechaConseguido ? `Conseguido el ${fechaCorta(logro.fechaConseguido)}` : 'Conseguido'}
          </span>
        ) : (
          <div className="lg-logro__prog">
            <span className="lg-barra">
              <span style={{ width: `${pct(logro) * 100}%` }} />
            </span>
            <span className="lg-logro__num">
              {num(logro.progreso)} / {num(logro.objetivo)} {logro.unidad}
            </span>
          </div>
        )}
      </div>
    </div>
  );
}

/** Todos los logros de Polaris juntos: los de cada modulo y los del cuerpo. */
export function Logros() {
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();
  const [estado, setEstado] = useState<Estado>('todos');
  const { logros, cargando, fallidos, reintentar } = useLogrosGlobales();

  const filtroModulo = (SECCIONES.find((s) => s.id === params.get('modulo'))?.id ?? 'todos') as ModuloLogros | 'todos';
  const elegirModulo = (v: string) => setParams(v === 'todos' ? {} : { modulo: v }, { replace: true });

  const conseguidos = logros.filter((l) => l.conseguido);
  const porNivel = NIVELES.map((n) => ({
    nivel: n,
    tengo: conseguidos.filter((l) => l.nivel === n).length,
    total: logros.filter((l) => l.nivel === n).length,
  }));

  const ultimos = useMemo(
    () =>
      logros
        .filter((l) => l.conseguido && l.fechaConseguido)
        .sort((a, b) => (b.fechaConseguido! < a.fechaConseguido! ? -1 : b.fechaConseguido! > a.fechaConseguido! ? 1 : 0))
        .slice(0, 3),
    [logros],
  );
  // Los pendientes a los que menos les falta, en proporcion; con algo de progreso.
  const casi = useMemo(
    () =>
      logros
        .filter((l) => !l.conseguido && l.progreso > 0)
        .sort((a, b) => pct(b) - pct(a))
        .slice(0, 3),
    [logros],
  );

  const visibles = logros.filter(
    (l) =>
      (filtroModulo === 'todos' || l.modulo === filtroModulo) &&
      (estado === 'todos' || (estado === 'conseguidos') === l.conseguido),
  );
  const secciones = SECCIONES.filter((s) => filtroModulo === 'todos' || s.id === filtroModulo)
    .map((s) => ({
      ...s,
      logros: visibles.filter((l) => l.modulo === s.id),
      tengo: conseguidos.filter((l) => l.modulo === s.id).length,
      total: logros.filter((l) => l.modulo === s.id).length,
    }))
    .filter((s) => s.total > 0);

  const seccionDe = (m: ModuloLogros) => SECCIONES.find((s) => s.id === m)!;

  return (
    <div className="lg">
      <PageHeader eyebrow="Polaris" coord="Tu colección" title="Logros" />

      {fallidos.length > 0 && (
        <Alert
          tone="warning"
          title={`No se han podido cargar los logros de ${fallidos.map((f) => f.nombre).join(', ')}`}
          action={
            <Button size="sm" variant="secondary" onClick={reintentar}>
              Reintentar
            </Button>
          }
        >
          El resto se ven con normalidad.
        </Alert>
      )}

      {cargando && logros.length === 0 ? (
        <p className="muted">Cargando…</p>
      ) : (
        <>
          <div className="grid lg-resumen">
            <div className="span-4">
              <Card eyebrow="Colección" delay={0} padding="10px 16px 14px">
                <div className="lg-coleccion">
                  <RingChart value={conseguidos.length} max={Math.max(1, logros.length)} size={92} thickness={8} color="var(--mod-polaris)" />
                  <div className="lg-coleccion__dato">
                    <span className="lg-coleccion__num">
                      {conseguidos.length}
                      <span className="muted"> / {logros.length}</span>
                    </span>
                    <span className="muted">logros conseguidos</span>
                    <ul className="lg-niveles">
                      {porNivel.map((n) => (
                        <li key={n.nivel} title={NOMBRE_NIVEL[n.nivel]}>
                          <span className="lg-punto" data-nivel={n.nivel} />
                          <span className="lg-niveles__nombre">{NOMBRE_NIVEL[n.nivel]}</span>
                          <b>
                            {n.tengo}
                            <span className="muted">/{n.total}</span>
                          </b>
                        </li>
                      ))}
                    </ul>
                  </div>
                </div>
              </Card>
            </div>

            <div className="span-4">
              <Card eyebrow="Últimos conseguidos" delay={60} padding="10px 16px 14px">
                {ultimos.length === 0 ? (
                  <p className="muted lg-vacio">Aún no tienes ninguno con fecha. El primero está más cerca de lo que crees.</p>
                ) : (
                  <ul className="lg-lista">
                    {ultimos.map((l) => (
                      <li key={l.modulo + l.codigo} data-module={l.modulo}>
                        <Medalla logro={l} size={30} />
                        <div>
                          <b>{l.nombre}</b>
                          <span className="muted">{seccionDe(l.modulo).nombre}</span>
                        </div>
                        <span className="lg-lista__dato">{hace(l.fechaConseguido!)}</span>
                      </li>
                    ))}
                  </ul>
                )}
              </Card>
            </div>

            <div className="span-4">
              <Card eyebrow="Casi lo tienes" delay={120} padding="10px 16px 14px">
                {casi.length === 0 ? (
                  <p className="muted lg-vacio">Empieza a apuntar en cualquier módulo y aquí verás lo que tienes a tiro.</p>
                ) : (
                  <ul className="lg-lista">
                    {casi.map((l) => (
                      <li key={l.modulo + l.codigo} data-module={l.modulo}>
                        <Medalla logro={l} size={30} />
                        <div>
                          <span className="lg-lista__linea">
                            <b>{l.nombre}</b>
                            <span className="lg-lista__dato">
                              {num(l.progreso)}/{num(l.objetivo)} {l.unidad}
                            </span>
                          </span>
                          <span className="lg-barra">
                            <span style={{ width: `${pct(l) * 100}%` }} />
                          </span>
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
              </Card>
            </div>
          </div>

          <div className="lg-filtros pl-rise">
            <SegmentedControl
              value={filtroModulo}
              onChange={elegirModulo}
              options={[
                { value: 'todos', label: 'Todos' },
                ...SECCIONES.map((s) => ({ value: s.id, label: s.nombre, icon: s.icono })),
              ]}
            />
            <SegmentedControl
              value={estado}
              onChange={(v) => setEstado(v as Estado)}
              options={[
                { value: 'todos', label: 'Todos' },
                { value: 'conseguidos', label: 'Conseguidos', count: conseguidos.length },
                { value: 'pendientes', label: 'Pendientes', count: logros.length - conseguidos.length },
              ]}
            />
          </div>

          {secciones.map((s) => (
            <section key={s.id} className="lg-seccion" data-module={s.id}>
              <header className="lg-seccion__cab">
                <span className="lg-seccion__icono">
                  <Icon name={s.icono} size={16} />
                </span>
                <h2>{s.nombre}</h2>
                <span className="lg-seccion__cuenta">
                  {s.tengo} de {s.total}
                </span>
                <span className="lg-barra lg-seccion__barra">
                  <span style={{ width: `${(s.tengo / s.total) * 100}%` }} />
                </span>
                {s.id !== 'nucleo' && (
                  <Button size="sm" variant="ghost" onClick={() => navigate(`/${s.id}`)}>
                    Ir a {s.nombre}
                  </Button>
                )}
              </header>
              {s.logros.length === 0 ? (
                <p className="muted lg-vacio">{estado === 'conseguidos' ? 'Aún ninguno conseguido aquí.' : '¡Los tienes todos!'}</p>
              ) : (
                <div className="lg-rejilla">
                  {s.logros.map((l, i) => (
                    <TarjetaLogro key={l.codigo} logro={l} i={i} />
                  ))}
                </div>
              )}
            </section>
          ))}
        </>
      )}
    </div>
  );
}
