import { useEffect } from 'react';
import { mensajeError } from '../../api/kuiper';
import { avisar, cerrarAviso, useAviso, useRestaurarMovimientos } from '../../api/kuiperPapelera';
import { Button, Toast } from '../../design-system';

const DURACION_MS = 7000;

/**
 * El aviso flotante de Kuiper: "Movimiento en la papelera · Deshacer", "Duplicado"... Se monta una
 * vez en la pagina; los avisos llegan por `avisar()` desde cualquier sitio, aunque quien borra ya
 * se haya desmontado. Se cierra solo a los pocos segundos.
 */
export function AvisoKuiper() {
  const aviso = useAviso();
  const restaurar = useRestaurarMovimientos();

  useEffect(() => {
    if (!aviso) return;
    const t = window.setTimeout(cerrarAviso, DURACION_MS);
    return () => window.clearTimeout(t);
  }, [aviso]);

  // Al salir de Kuiper no se queda un aviso pendiente para la proxima visita.
  useEffect(() => cerrarAviso, []);

  if (!aviso) return null;

  const deshacer = () => {
    if (!aviso.restaurar) return;
    restaurar.mutate(aviso.restaurar, {
      onSuccess: (r) => avisar({ texto: r.length === 1 ? 'Movimiento restaurado.' : `${r.length} movimientos restaurados.`, tono: 'success' }),
      onError: (e) => avisar({ texto: mensajeError(e), tono: 'danger' }),
    });
  };

  return (
    <Toast
      fixed
      tone={aviso.tono ?? 'info'}
      icon={aviso.restaurar ? 'trash-2' : undefined}
      onClose={cerrarAviso}
      action={
        aviso.restaurar && (
          <Button size="sm" variant="ghost" icon="rotate-ccw" loading={restaurar.isPending} onClick={deshacer}>
            Deshacer
          </Button>
        )
      }
    >
      {aviso.texto}
    </Toast>
  );
}
