import { useEffect } from 'react';
import { Button, Toast } from '../design-system';
import { cerrarAviso, useAviso } from '../lib/avisos';

/** Pinta el aviso actual y lo cierra solo a los pocos segundos (mas si trae una accion como "Deshacer"). */
export function Avisos() {
  const aviso = useAviso();

  useEffect(() => {
    if (!aviso) return;
    const t = setTimeout(cerrarAviso, aviso.accion ? 7000 : 3200);
    return () => clearTimeout(t);
  }, [aviso]);

  if (!aviso) return null;
  const { accion } = aviso;
  return (
    <Toast
      key={aviso.id}
      fixed
      tone={aviso.tono ?? 'success'}
      onClose={cerrarAviso}
      action={
        accion && (
          <Button
            size="sm"
            variant="ghost"
            icon={accion.icono}
            onClick={() => {
              cerrarAviso();
              accion.alPulsar();
            }}
          >
            {accion.texto}
          </Button>
        )
      }
    >
      {aviso.texto}
    </Toast>
  );
}
