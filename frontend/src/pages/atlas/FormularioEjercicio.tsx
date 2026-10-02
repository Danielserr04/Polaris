import { useState, type FormEvent } from 'react';
import { mensajeError, useActualizarEjercicio, useBorrarEjercicio, useCrearEjercicio, type Ejercicio } from '../../api/atlas';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Button, Dialog, Input } from '../../design-system';

interface Props {
  /** Sin ejercicio: alta. Con ejercicio propio: edicion (y borrado). */
  ejercicio?: Ejercicio;
  onClose: () => void;
}

export function FormularioEjercicio({ ejercicio, onClose }: Props) {
  useRestaurarFoco();
  const editando = ejercicio !== undefined;
  const crear = useCrearEjercicio();
  const actualizar = useActualizarEjercicio(ejercicio?.id ?? 0);
  const borrar = useBorrarEjercicio(onClose);

  const [nombre, setNombre] = useState(ejercicio?.nombre ?? '');
  const [grupo, setGrupo] = useState(ejercicio?.grupoMuscular ?? '');
  const [equipamiento, setEquipamiento] = useState(ejercicio?.equipamiento ?? '');
  const [confirmando, setConfirmando] = useState(false);
  const [errorEnvio, setErrorEnvio] = useState<string | null>(null);

  const errNombre = !nombre.trim() ? 'Pon un nombre.' : nombre.trim().length > 150 ? 'Como mucho 150 caracteres.' : null;
  const errGrupo = !grupo.trim() ? 'Pon el grupo muscular.' : grupo.trim().length > 50 ? 'Como mucho 50 caracteres.' : null;
  const errEquip = equipamiento.trim().length > 100 ? 'Como mucho 100 caracteres.' : null;
  const ocupado = crear.isPending || actualizar.isPending || borrar.isPending;

  const guardar = (ev: FormEvent) => {
    ev.preventDefault();
    setErrorEnvio(null);
    if (errNombre || errGrupo || errEquip) return;
    const cuerpo = { nombre: nombre.trim(), grupoMuscular: grupo.trim(), equipamiento: equipamiento.trim() || null };
    const opciones = { onSuccess: onClose, onError: (e: unknown) => setErrorEnvio(mensajeError(e)) };
    if (editando) actualizar.mutate(cuerpo, opciones);
    else crear.mutate(cuerpo, opciones);
  };

  const borrarEjercicio = () => {
    if (!ejercicio) return;
    setErrorEnvio(null);
    borrar.mutate(ejercicio.id, {
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
      width={460}
      title={editando ? 'Editar ejercicio' : 'Nuevo ejercicio'}
      footer={
        confirmando ? (
          <>
            <span className="muted" style={{ marginRight: 'auto' }}>¿Borrar este ejercicio?</span>
            <Button variant="ghost" type="button" autoFocus onClick={() => setConfirmando(false)}>
              No
            </Button>
            <Button variant="danger" type="button" loading={borrar.isPending} onClick={borrarEjercicio}>
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
            <Button type="submit" form="atl-form-ejercicio" loading={ocupado}>
              {editando ? 'Guardar' : 'Crear'}
            </Button>
          </>
        )
      }
    >
      <form id="atl-form-ejercicio" className="atl-form" onSubmit={guardar} noValidate>
        {errorEnvio && <Alert tone="danger">{errorEnvio}</Alert>}
        <Input label="Nombre" autoFocus value={nombre} onChange={(e) => setNombre(e.target.value)} error={errNombre} validarAlSalir />
        <Input label="Grupo muscular" placeholder="pecho, espalda, pierna…" value={grupo} onChange={(e) => setGrupo(e.target.value)} error={errGrupo} validarAlSalir />
        <Input label="Equipamiento" hint="Opcional" placeholder="barra, mancuernas, máquina…" value={equipamiento} onChange={(e) => setEquipamiento(e.target.value)} error={errEquip} validarAlSalir />
      </form>
    </Dialog>
  );
}
