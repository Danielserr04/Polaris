import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { MutationCache, QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import { App } from './app/App';
import { getToken, suscribir } from './auth/sesion';
import { avisar } from './lib/avisos';
import './styles/index.css';

declare module '@tanstack/react-query' {
  interface Register {
    /** `aviso`: texto del aviso de exito que se muestra al terminar bien la mutacion. */
    mutationMeta: { aviso?: string };
  }
}

const queryClient = new QueryClient({
  mutationCache: new MutationCache({
    onSuccess: (_datos, _variables, _contexto, mutacion) => {
      if (mutacion.meta?.aviso) avisar(mutacion.meta.aviso);
    },
  }),
  defaultOptions: { queries: { retry: 1, refetchOnWindowFocus: false } },
});

// Al cerrarse la sesion (logout o 401) se tira la cache: nada de un usuario sobrevive al siguiente.
suscribir(() => {
  if (!getToken()) queryClient.clear();
});

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
        <App />
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>,
);
