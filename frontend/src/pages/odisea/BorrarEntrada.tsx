import { mensajeError, useBorrarEntrada, type EntradaForm } from '../../api/odisea';
import { Alert, Button, Dialog } from '../../design-system';
import { useRestaurarFoco } from './useRestaurarFoco';

interface Props {
  entrada: EntradaForm;
  onClose: () => void;
  onBorrada: () => void;
}

/** Confirmación de borrado. Solo se borra tu entrada: la ficha sigue en el catálogo compartido. */
export function BorrarEntrada({ entrada, onClose, onBorrada }: Props) {
  useRestaurarFoco();
  const borrar = useBorrarEntrada(onBorrada);

  return (
    <Dialog
      open
      onClose={onClose}
      title="Borrar de tu lista"
      footer={
        <>
          <Button variant="ghost" type="button" autoFocus onClick={onClose}>
            Cancelar
          </Button>
          <Button
            variant="danger"
            type="button"
            loading={borrar.isPending}
            onClick={() => borrar.mutate(entrada.id)}
          >
            Borrar
          </Button>
        </>
      }
    >
      <div className="stack-12">
        {borrar.isError && <Alert tone="danger">{mensajeError(borrar.error)}</Alert>}
        <p style={{ margin: 0 }}>
          Se quitará «{entrada.tituloTitulo}» de tu lista, con su estado, valoración y notas. La ficha del título se queda en el catálogo.
        </p>
      </div>
    </Dialog>
  );
}
