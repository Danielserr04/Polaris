import { Button, Dialog } from '../design-system';
import { cerrarErrorServidor, useErrorServidor } from '../lib/errorServidor';

/** Ventana centrada con el aviso de error del servidor. Se monta una vez, en App. */
export function ErrorServidor() {
  const abierto = useErrorServidor();
  return (
    <Dialog
      open={abierto}
      title="Error del servidor"
      onClose={cerrarErrorServidor}
      width={400}
      footer={
        <Button type="button" autoFocus onClick={cerrarErrorServidor}>
          Entendido
        </Button>
      }
    >
      <p>Es cosa nuestra y lo solucionaremos en breve. Inténtalo más tarde.</p>
    </Dialog>
  );
}
