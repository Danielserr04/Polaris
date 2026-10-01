import { useEffect } from 'react';

/**
 * El Dialog del design system no devuelve el foco al cerrarse. Se llama desde el componente que lo
 * monta: al montarse guarda quién tenía el foco y al desmontarse se lo devuelve.
 */
export function useRestaurarFoco(): void {
  useEffect(() => {
    const previo = document.activeElement;
    return () => {
      if (previo instanceof HTMLElement && previo.isConnected) previo.focus();
    };
  }, []);
}
