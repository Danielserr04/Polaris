import { useEffect } from 'react';
import { Toast } from '../design-system';
import { cerrarAviso, useAviso } from '../lib/avisos';

/** Pinta el aviso de exito actual y lo cierra solo a los pocos segundos. */
export function Avisos() {
  const aviso = useAviso();

  useEffect(() => {
    if (!aviso) return;
    const t = setTimeout(cerrarAviso, 3200);
    return () => clearTimeout(t);
  }, [aviso]);

  if (!aviso) return null;
  return (
    <Toast key={aviso.id} fixed tone="success" onClose={cerrarAviso}>
      {aviso.texto}
    </Toast>
  );
}
