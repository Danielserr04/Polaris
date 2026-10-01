import { useState, type FormEvent } from 'react';
import {
  mensajeError,
  pedirRutina,
  useActualizarSesion,
  useBorrarSesion,
  useCrearEjercicio,
  useCrearSesion,
  useEjercicios,
  useRutinas,
  useSesion,
  type Ejercicio,
  type SesionCompleta,
} from '../../api/atlas';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Button, Dialog, IconButton, Input, Select } from '../../design-system';
import { iso, num } from '../../lib/fechas';

interface Props {
  /** Sin id: alta. Con id: edicion (y borrado) de esa sesion. */
  sesionId?: number;
  onClose: () => void;
}

interface SerieForm {
  reps: string;
  peso: string;
  rpe: string;
}

interface Bloque {
  ejercicioId: number;
  nombre: string;
  grupo: string | null;
  series: SerieForm[];
}

const REPS = /^\d{1,3}$/;
const PESO = /^\d{1,4}([.,]\d{1,2})?$/;
const RPE = /^(10|[1-9])([.,][05])?$/;

const decimal = (s: string) => Number(s.trim().replace(',', '.'));
const texto = (n: number) => String(n).replace('.', ',');
const serieVacia = (): SerieForm => ({ reps: '', peso: '', rpe: '' });

const errReps = (s: string) => (REPS.test(s.trim()) && Number(s) >= 1 ? null : '1 a 999');
const errPeso = (s: string) => (PESO.test(s.trim()) && decimal(s) <= 1000 ? null : '0 a 1.000');
const errRpe = (s: string) => (s.trim() === '' || (RPE.test(s.trim()) && decimal(s) >= 1 && decimal(s) <= 10) ? null : '1 a 10, de 0,5 en 0,5');

export function FormularioSesion({ sesionId, onClose }: Props) {
  useRestaurarFoco();
  const editando = sesionId !== undefined;
  const ficha = useSesion(sesionId);

  if (editando && (ficha.isPending || ficha.isError)) {
    return (
      <Dialog open onClose={onClose} title="Editar sesión" footer={<Button variant="ghost" onClick={onClose}>Cerrar</Button>}>
        {ficha.isPending ? <p className="muted" style={{ margin: 0 }}>Cargando…</p> : <Alert tone="danger">{mensajeError(ficha.error)}</Alert>}
      </Dialog>
    );
  }
  return <Cuerpo sesion={ficha.data} onClose={onClose} />;
}

/** Las series planas de la API, agrupadas en un bloque por ejercicio (vienen ya agrupadas). */
function agrupar(sesion: SesionCompleta | undefined): Bloque[] {
  const bloques: Bloque[] = [];
  for (const s of sesion?.series ?? []) {
    let b = bloques.find((x) => x.ejercicioId === s.ejercicioId);
    if (!b) {
      b = { ejercicioId: s.ejercicioId, nombre: s.ejercicioNombre, grupo: s.ejercicioGrupoMuscular ?? null, series: [] };
      bloques.push(b);
    }
    b.series.push({ reps: String(s.reps), peso: texto(s.pesoKg), rpe: s.rpe != null ? texto(s.rpe) : '' });
  }
  return bloques;
}

function Cuerpo({ sesion, onClose }: { sesion: SesionCompleta | undefined; onClose: () => void }) {
  const editando = sesion !== undefined;
  const hoy = iso(new Date());
  const crear = useCrearSesion();
  const actualizar = useActualizarSesion(sesion?.id ?? 0);
  const borrar = useBorrarSesion(onClose);
  const ejercicios = useEjercicios();
  const rutinas = useRutinas();
  const crearEjercicio = useCrearEjercicio();

  const [fecha, setFecha] = useState(sesion?.fecha ?? hoy);
  const [rutinaId, setRutinaId] = useState<number | null>(sesion?.rutinaId ?? null);
  const [duracion, setDuracion] = useState(sesion?.duracionMin != null ? String(sesion.duracionMin) : '');
  const [notas, setNotas] = useState(sesion?.notas ?? '');
  const [bloques, setBloques] = useState<Bloque[]>(() => agrupar(sesion));
  const [intentado, setIntentado] = useState(false);
  const [confirmando, setConfirmando] = useState(false);
  const [errorEnvio, setErrorEnvio] = useState<string | null>(null);
  const [nuevo, setNuevo] = useState<{ nombre: string; grupo: string } | null>(null);
  const [errorNuevo, setErrorNuevo] = useState<string | null>(null);

  const errFecha = !fecha ? 'Elige una fecha.' : fecha > hoy ? 'No puede ser una fecha futura.' : null;
  const errDuracion = duracion.trim() === '' || (/^\d{1,4}$/.test(duracion.trim()) && Number(duracion) >= 1 && Number(duracion) <= 1440) ? null : '1 a 1.440 min';
  const errBloques = bloques.length === 0 ? 'Añade al menos un ejercicio.' : bloques.some((b) => b.series.length === 0) ? 'Cada ejercicio necesita al menos una serie.' : null;
  const totalSeries = bloques.reduce((a, b) => a + b.series.length, 0);
  const errTope = totalSeries > 200 ? 'Como mucho 200 series.' : null;
  const serieInvalida = bloques.some((b) => b.series.some((s) => errReps(s.reps) || errPeso(s.peso) || errRpe(s.rpe)));
  const ocupado = crear.isPending || actualizar.isPending || borrar.isPending;
  const ver = (e: string | null) => (intentado ? e : null);
  const volumen = bloques.reduce((a, b) => a + b.series.reduce((x, s) => x + (!errReps(s.reps) && !errPeso(s.peso) ? Number(s.reps) * decimal(s.peso) : 0), 0), 0);

  const disponibles = (ejercicios.data ?? []).filter((e) => !bloques.some((b) => b.ejercicioId === e.id));

  const anadirEjercicio = (e: Ejercicio) =>
    setBloques((bs) => [...bs, { ejercicioId: e.id, nombre: e.nombre, grupo: e.grupoMuscular, series: [serieVacia()] }]);

  const elegirRutina = async (valor: string) => {
    const id = valor === '' ? null : Number(valor);
    setRutinaId(id);
    // En una sesion nueva y todavia vacia, la rutina precarga sus ejercicios con las series objetivo.
    if (id === null || editando || bloques.length > 0) return;
    try {
      const r = await pedirRutina(id);
      setBloques(
        [...r.lineas]
          .sort((a, b) => a.orden - b.orden)
          .map((l) => ({
            ejercicioId: l.ejercicioId,
            nombre: l.ejercicioNombre,
            grupo: l.ejercicioGrupoMuscular ?? null,
            series: Array.from({ length: l.seriesObjetivo }, serieVacia),
          })),
      );
    } catch {
      // Sin precarga: el usuario anade los ejercicios a mano.
    }
  };

  const cambiarSerie = (b: number, s: number, campo: keyof SerieForm, valor: string) =>
    setBloques((bs) => bs.map((x, i) => (i === b ? { ...x, series: x.series.map((y, j) => (j === s ? { ...y, [campo]: valor } : y)) } : x)));

  // Una serie nueva parte de la anterior: lo normal es repetir peso y repeticiones.
  const anadirSerie = (b: number) =>
    setBloques((bs) => bs.map((x, i) => (i === b ? { ...x, series: [...x.series, { ...(x.series[x.series.length - 1] ?? serieVacia()), rpe: '' }] } : x)));

  const quitarSerie = (b: number, s: number) =>
    setBloques((bs) => bs.map((x, i) => (i === b ? { ...x, series: x.series.filter((_, j) => j !== s) } : x)));

  const crearNuevo = () => {
    if (!nuevo) return;
    const nombre = nuevo.nombre.trim();
    const grupo = nuevo.grupo.trim();
    if (!nombre || !grupo) {
      setErrorNuevo('Pon el nombre y el grupo muscular.');
      return;
    }
    setErrorNuevo(null);
    crearEjercicio.mutate(
      { nombre, grupoMuscular: grupo },
      {
        onSuccess: (e) => {
          anadirEjercicio(e);
          setNuevo(null);
        },
        onError: (e) => setErrorNuevo(mensajeError(e)),
      },
    );
  };

  const guardar = (ev: FormEvent) => {
    ev.preventDefault();
    setIntentado(true);
    setErrorEnvio(null);
    if (errFecha || errDuracion || errBloques || errTope || serieInvalida) return;
    const cuerpo = {
      rutinaId,
      fecha,
      duracionMin: duracion.trim() === '' ? null : Number(duracion),
      notas: notas.trim() === '' ? null : notas.trim(),
      series: bloques.flatMap((b) =>
        b.series.map((s, i) => ({
          ejercicioId: b.ejercicioId,
          numeroSerie: i + 1,
          reps: Number(s.reps),
          pesoKg: decimal(s.peso),
          rpe: s.rpe.trim() === '' ? null : decimal(s.rpe),
        })),
      ),
    };
    const opciones = { onSuccess: onClose, onError: (e: unknown) => setErrorEnvio(mensajeError(e)) };
    if (editando) actualizar.mutate(cuerpo, opciones);
    else crear.mutate(cuerpo, opciones);
  };

  const borrarSesion = () => {
    if (!sesion) return;
    setErrorEnvio(null);
    borrar.mutate(sesion.id, {
      onError: (e) => {
        setConfirmando(false);
        setErrorEnvio(mensajeError(e));
      },
    });
  };

  return (
    <Dialog
      open
      onClose={onClose}
      width={640}
      title={editando ? 'Editar sesión' : 'Registrar sesión'}
      footer={
        confirmando ? (
          <>
            <span className="muted" style={{ marginRight: 'auto' }}>¿Borrar esta sesión?</span>
            <Button variant="ghost" type="button" autoFocus onClick={() => setConfirmando(false)}>
              No
            </Button>
            <Button variant="danger" type="button" loading={borrar.isPending} onClick={borrarSesion}>
              Sí, borrar
            </Button>
          </>
        ) : (
          <>
            {editando && (
              <Button variant="ghost" type="button" style={{ marginRight: 'auto' }} disabled={ocupado} onClick={() => setConfirmando(true)}>
                Borrar
              </Button>
            )}
            <Button variant="ghost" type="button" onClick={onClose}>
              Cancelar
            </Button>
            <Button type="submit" form="atl-form-sesion" loading={ocupado}>
              {editando ? 'Guardar' : 'Registrar'}
            </Button>
          </>
        )
      }
    >
      <form id="atl-form-sesion" className="atl-form" onSubmit={guardar} noValidate>
        {errorEnvio && <Alert tone="danger">{errorEnvio}</Alert>}
        <div className="atl-form__row">
          <Input label="Fecha" type="date" max={hoy} value={fecha} onChange={(e) => setFecha(e.target.value)} error={ver(errFecha)} />
          <Select
            id="atl-rutina"
            label="Rutina"
            value={rutinaId !== null ? String(rutinaId) : ''}
            onChange={(e) => void elegirRutina(e.target.value)}
            options={[
              { value: '', label: 'Sin rutina' },
              ...(rutinas.data ?? [])
                // Una rutina inactiva sigue valiendo si ya era la de esta sesion.
                .filter((r) => r.activa || r.id === sesion?.rutinaId)
                .map((r) => ({ value: String(r.id), label: r.activa ? r.nombre : `${r.nombre} (inactiva)` })),
            ]}
          />
          <Input
            label="Duración"
            inputMode="numeric"
            trailing={<span className="muted" style={{ fontSize: 12 }}>min</span>}
            value={duracion}
            onChange={(e) => setDuracion(e.target.value)}
            error={ver(errDuracion)}
          />
        </div>

        {bloques.map((b, i) => (
          <div key={b.ejercicioId} className="atl-ej">
            <div className="atl-ej__h">
              <b>{b.nombre}</b>
              {b.grupo && <span>{b.grupo}</span>}
              <IconButton icon="x" label={`Quitar ${b.nombre}`} variant="ghost" size="sm" type="button" onClick={() => setBloques((bs) => bs.filter((_, j) => j !== i))} />
            </div>
            <div className="atl-serie atl-serie--cab">
              <span>#</span>
              <span>Reps</span>
              <span>Peso kg</span>
              <span>RPE</span>
              <span />
            </div>
            {b.series.map((s, j) => (
              <div key={j} className="atl-serie">
                <span className="atl-serie__n">{j + 1}</span>
                <Input aria-label={`Repeticiones, serie ${j + 1} de ${b.nombre}`} size="sm" inputMode="numeric" value={s.reps} onChange={(e) => cambiarSerie(i, j, 'reps', e.target.value)} error={ver(errReps(s.reps))} />
                <Input aria-label={`Peso, serie ${j + 1} de ${b.nombre}`} size="sm" inputMode="decimal" value={s.peso} onChange={(e) => cambiarSerie(i, j, 'peso', e.target.value)} error={ver(errPeso(s.peso))} />
                <Input aria-label={`RPE, serie ${j + 1} de ${b.nombre}`} size="sm" inputMode="decimal" placeholder="—" value={s.rpe} onChange={(e) => cambiarSerie(i, j, 'rpe', e.target.value)} error={ver(errRpe(s.rpe))} />
                <IconButton icon="x" label={`Quitar serie ${j + 1} de ${b.nombre}`} variant="ghost" size="sm" type="button" onClick={() => quitarSerie(i, j)} />
              </div>
            ))}
            <div>
              <Button size="sm" variant="ghost" icon="plus" type="button" onClick={() => anadirSerie(i)}>
                Serie
              </Button>
            </div>
          </div>
        ))}

        {nuevo ? (
          <div>
            <div className="atl-nuevo">
              <Input label="Ejercicio nuevo" autoFocus value={nuevo.nombre} onChange={(e) => setNuevo({ ...nuevo, nombre: e.target.value })} />
              <Input label="Grupo muscular" value={nuevo.grupo} onChange={(e) => setNuevo({ ...nuevo, grupo: e.target.value })} />
              <span style={{ display: 'flex', gap: 6 }}>
                <Button type="button" loading={crearEjercicio.isPending} onClick={crearNuevo}>
                  Crear
                </Button>
                <Button type="button" variant="ghost" onClick={() => { setNuevo(null); setErrorNuevo(null); }}>
                  Cancelar
                </Button>
              </span>
            </div>
            {errorNuevo && <p style={{ margin: '6px 0 0', fontSize: 12.5, color: 'var(--danger)' }}>{errorNuevo}</p>}
          </div>
        ) : (
          <div style={{ display: 'flex', gap: 10, alignItems: 'end' }}>
            <div style={{ flex: 1 }}>
              <Select
                id="atl-anadir"
                label="Añadir ejercicio"
                value=""
                onChange={(e) => {
                  const e2 = disponibles.find((x) => x.id === Number(e.target.value));
                  if (e2) anadirEjercicio(e2);
                }}
                options={[{ value: '', label: disponibles.length ? 'Elige un ejercicio…' : 'No hay más ejercicios' }, ...disponibles.map((e) => ({ value: String(e.id), label: e.nombre }))]}
              />
            </div>
            <Button type="button" variant="secondary" icon="plus" onClick={() => setNuevo({ nombre: '', grupo: '' })}>
              Nuevo
            </Button>
          </div>
        )}
        {ver(errBloques ?? errTope) && <p style={{ margin: 0, fontSize: 12.5, color: 'var(--danger)' }}>{ver(errBloques ?? errTope)}</p>}

        {bloques.length > 0 && (
          <div className="atl-total">
            <span>{totalSeries} {totalSeries === 1 ? 'serie' : 'series'}</span>
            <b>{num(volumen)} kg de volumen</b>
          </div>
        )}

        <div className="pl-field">
          <label className="pl-field__label" htmlFor="atl-notas">
            Notas
          </label>
          <textarea id="atl-notas" className="atl-textarea" rows={2} maxLength={2000} placeholder="Cómo ha ido, molestias, lo que quieras recordar" value={notas} onChange={(e) => setNotas(e.target.value)} />
        </div>
      </form>
    </Dialog>
  );
}
