import { useState, type FormEvent } from 'react';
import { mensajeError, useActualizarRutina, useBorrarRutina, useCrearEjercicio, useCrearRutina, useEjercicios, useRutina, type Ejercicio, type RutinaCompleta } from '../../api/atlas';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Button, Dialog, IconButton, Input, Select, Switch } from '../../design-system';

interface Props {
  /** Sin id: alta. Con id: edicion (y borrado) de esa rutina. */
  rutinaId?: number;
  onClose: () => void;
}

interface Linea {
  ejercicioId: number;
  nombre: string;
  grupo: string | null;
  series: string;
  reps: string;
}

export function FormularioRutina({ rutinaId, onClose }: Props) {
  useRestaurarFoco();
  const editando = rutinaId !== undefined;
  const ficha = useRutina(rutinaId);

  if (editando && (ficha.isPending || ficha.isError)) {
    return (
      <Dialog open onClose={onClose} title="Editar rutina" footer={<Button variant="ghost" onClick={onClose}>Cerrar</Button>}>
        {ficha.isPending ? <p className="muted" style={{ margin: 0 }}>Cargando…</p> : <Alert tone="danger">{mensajeError(ficha.error)}</Alert>}
      </Dialog>
    );
  }
  return <Cuerpo rutina={ficha.data} onClose={onClose} />;
}

function Cuerpo({ rutina, onClose }: { rutina: RutinaCompleta | undefined; onClose: () => void }) {
  const editando = rutina !== undefined;
  const crear = useCrearRutina();
  const actualizar = useActualizarRutina(rutina?.id ?? 0);
  const borrar = useBorrarRutina(onClose);
  const ejercicios = useEjercicios();
  const crearEjercicio = useCrearEjercicio();

  const [nombre, setNombre] = useState(rutina?.nombre ?? '');
  const [descripcion, setDescripcion] = useState(rutina?.descripcion ?? '');
  const [activa, setActiva] = useState(rutina?.activa ?? true);
  const [lineas, setLineas] = useState<Linea[]>(() =>
    [...(rutina?.lineas ?? [])]
      .sort((a, b) => a.orden - b.orden)
      .map((l) => ({ ejercicioId: l.ejercicioId, nombre: l.ejercicioNombre, grupo: l.ejercicioGrupoMuscular ?? null, series: String(l.seriesObjetivo), reps: l.repsObjetivo ?? '' })),
  );
  const [intentado, setIntentado] = useState(false);
  const [confirmando, setConfirmando] = useState(false);
  const [errorEnvio, setErrorEnvio] = useState<string | null>(null);
  const [nuevo, setNuevo] = useState<{ nombre: string; grupo: string } | null>(null);
  const [errorNuevo, setErrorNuevo] = useState<string | null>(null);

  const errNombre = !nombre.trim() ? 'Pon un nombre.' : nombre.trim().length > 100 ? 'Como mucho 100 caracteres.' : null;
  const errSeries = (s: string) => (/^\d{1,2}$/.test(s.trim()) && Number(s) >= 1 && Number(s) <= 99 ? null : '1 a 99');
  const errReps = (s: string) => (!s.trim() ? 'Ej. 6-8' : s.trim().length > 20 ? 'Máx. 20' : null);
  const errLineas = lineas.length === 0 ? 'Añade al menos un ejercicio.' : lineas.length > 50 ? 'Como mucho 50 ejercicios.' : null;
  const ocupado = crear.isPending || actualizar.isPending || borrar.isPending;
  const ver = (e: string | null) => (intentado ? e : null);
  const disponibles = (ejercicios.data ?? []).filter((e) => !lineas.some((l) => l.ejercicioId === e.id));

  const anadir = (e: Ejercicio) => setLineas((ls) => [...ls, { ejercicioId: e.id, nombre: e.nombre, grupo: e.grupoMuscular, series: '3', reps: '8-12' }]);
  const mover = (i: number, d: -1 | 1) =>
    setLineas((ls) => {
      const j = i + d;
      if (j < 0 || j >= ls.length) return ls;
      const c = [...ls];
      [c[i], c[j]] = [c[j], c[i]];
      return c;
    });

  const crearNuevo = () => {
    if (!nuevo) return;
    const n = nuevo.nombre.trim();
    const g = nuevo.grupo.trim();
    if (!n || !g) {
      setErrorNuevo('Pon el nombre y el grupo muscular.');
      return;
    }
    setErrorNuevo(null);
    crearEjercicio.mutate(
      { nombre: n, grupoMuscular: g },
      {
        onSuccess: (e) => {
          anadir(e);
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
    if (errNombre || errLineas || lineas.some((l) => errSeries(l.series) || errReps(l.reps))) return;
    const cuerpo = {
      nombre: nombre.trim(),
      descripcion: descripcion.trim() || null,
      activa,
      lineas: lineas.map((l, i) => ({ ejercicioId: l.ejercicioId, orden: i + 1, seriesObjetivo: Number(l.series), repsObjetivo: l.reps.trim() })),
    };
    const opciones = { onSuccess: onClose, onError: (e: unknown) => setErrorEnvio(mensajeError(e)) };
    if (editando) actualizar.mutate(cuerpo, opciones);
    else crear.mutate(cuerpo, opciones);
  };

  const borrarRutina = () => {
    if (!rutina) return;
    setErrorEnvio(null);
    borrar.mutate(rutina.id, {
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
      confirmarDescarte
      width={600}
      title={editando ? 'Editar rutina' : 'Nueva rutina'}
      footer={
        confirmando ? (
          <>
            <span className="muted" style={{ marginRight: 'auto' }}>¿Borrar esta rutina?</span>
            <Button variant="ghost" type="button" autoFocus onClick={() => setConfirmando(false)}>
              No
            </Button>
            <Button variant="danger" type="button" loading={borrar.isPending} onClick={borrarRutina}>
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
            <Button type="submit" form="atl-form-rutina" loading={ocupado}>
              {editando ? 'Guardar' : 'Crear'}
            </Button>
          </>
        )
      }
    >
      <form id="atl-form-rutina" className="atl-form" onSubmit={guardar} noValidate>
        {errorEnvio && <Alert tone="danger">{errorEnvio}</Alert>}
        <Input label="Nombre" autoFocus value={nombre} onChange={(e) => setNombre(e.target.value)} error={errNombre} validarAlSalir />
        <div className="pl-field">
          <label className="pl-field__label" htmlFor="atl-rut-desc">
            Descripción
          </label>
          <textarea id="atl-rut-desc" className="atl-textarea" rows={2} maxLength={2000} placeholder="Opcional" value={descripcion} onChange={(e) => setDescripcion(e.target.value)} />
        </div>
        <Switch label={activa ? 'Activa' : 'Inactiva (no sale al registrar sesiones)'} checked={activa} onChange={setActiva} />

        {lineas.length > 0 && (
          <div>
            <div className="atl-linea atl-serie--cab" style={{ borderBottom: 0, paddingBottom: 0 }}>
              <span>Ejercicio</span>
              <span>Series</span>
              <span>Reps</span>
              <span />
            </div>
            {lineas.map((l, i) => (
              <div key={l.ejercicioId} className="atl-linea">
                <div className="atl-linea__n">
                  <b>{l.nombre}</b>
                  {l.grupo && <span>{l.grupo}</span>}
                </div>
                <Input aria-label={`Series de ${l.nombre}`} size="sm" inputMode="numeric" value={l.series} onChange={(e) => setLineas((ls) => ls.map((x, j) => (j === i ? { ...x, series: e.target.value } : x)))} error={errSeries(l.series)} validarAlSalir />
                <Input aria-label={`Repeticiones de ${l.nombre}`} size="sm" placeholder="6-8" value={l.reps} onChange={(e) => setLineas((ls) => ls.map((x, j) => (j === i ? { ...x, reps: e.target.value } : x)))} error={errReps(l.reps)} validarAlSalir />
                <span className="atl-linea__m">
                  <IconButton icon="chevron-up" label={`Subir ${l.nombre}`} variant="ghost" size="sm" type="button" disabled={i === 0} onClick={() => mover(i, -1)} />
                  <IconButton icon="chevron-down" label={`Bajar ${l.nombre}`} variant="ghost" size="sm" type="button" disabled={i === lineas.length - 1} onClick={() => mover(i, 1)} />
                  <IconButton icon="x" label={`Quitar ${l.nombre}`} variant="ghost" size="sm" type="button" onClick={() => setLineas((ls) => ls.filter((_, j) => j !== i))} />
                </span>
              </div>
            ))}
          </div>
        )}

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
                id="atl-rut-anadir"
                label="Añadir ejercicio"
                value=""
                onChange={(e) => {
                  const x = disponibles.find((d) => d.id === Number(e.target.value));
                  if (x) anadir(x);
                }}
                options={[{ value: '', label: disponibles.length ? 'Elige un ejercicio…' : 'No hay más ejercicios' }, ...disponibles.map((d) => ({ value: String(d.id), label: d.nombre }))]}
              />
            </div>
            <Button type="button" variant="secondary" icon="plus" onClick={() => setNuevo({ nombre: '', grupo: '' })}>
              Nuevo
            </Button>
          </div>
        )}
        {ver(errLineas) && <p style={{ margin: 0, fontSize: 12.5, color: 'var(--danger)' }}>{ver(errLineas)}</p>}
      </form>
    </Dialog>
  );
}
