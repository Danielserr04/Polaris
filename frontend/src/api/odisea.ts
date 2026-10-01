import { useMutation, useQueries, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from './client';

// Contratos de Odisea. Reflejan los DTO de com.polaris.odisea.infrastructure.persistence.dto;
// si cambian alli, cambian aqui.

export type TipoContenido = 'PELICULA' | 'SERIE' | 'JUEGO' | 'LIBRO';
export type EstadoEntrada = 'PENDIENTE' | 'EN_CURSO' | 'TERMINADO' | 'ABANDONADO';
export type FuenteExterna = 'TMDB' | 'IGDB' | 'OPEN_LIBRARY' | 'MANUAL';

export const TIPOS: TipoContenido[] = ['PELICULA', 'SERIE', 'JUEGO', 'LIBRO'];
export const ESTADOS: EstadoEntrada[] = ['PENDIENTE', 'EN_CURSO', 'TERMINADO', 'ABANDONADO'];

export const ETIQUETA_ESTADO: Record<EstadoEntrada, string> = {
  PENDIENTE: 'Pendiente',
  EN_CURSO: 'En curso',
  TERMINADO: 'Terminado',
  ABANDONADO: 'Abandonado',
};

export const ETIQUETA_TIPO: Record<TipoContenido, string> = {
  PELICULA: 'Película',
  SERIE: 'Serie',
  JUEGO: 'Juego',
  LIBRO: 'Libro',
};

/** GET /api/odisea/entrada: la version ligera para el listado, sin notas ni fechas. */
export interface EntradaList {
  id: number;
  tituloId: number;
  tituloTitulo: string;
  tituloImagenUrl: string | null;
  tituloTipo: TipoContenido;
  estado: EstadoEntrada;
  valoracion: number | null;
  favorito: boolean;
  progreso: number | null;
}

/** GET /api/odisea/entrada/{id}: la entrada completa. Fechas como yyyy-MM-dd. */
export interface EntradaForm {
  id: number;
  tituloId: number;
  tituloTitulo: string;
  tituloImagenUrl: string | null;
  tituloTipo: TipoContenido;
  estado: EstadoEntrada;
  valoracion: number | null;
  notas: string | null;
  fechaInicio: string | null;
  fechaFin: string | null;
  favorito: boolean;
  progreso: number | null;
}

/** Cuerpo de POST/PUT /api/odisea/entrada. El PUT reemplaza todos los campos. */
export interface EntradaRequest {
  tituloId: number;
  estado: EstadoEntrada;
  valoracion: number | null;
  notas: string | null;
  fechaInicio: string | null;
  fechaFin: string | null;
  favorito: boolean;
  progreso: number | null;
}

/** GET /api/odisea/titulo: version ligera del catalogo compartido. */
export interface TituloList {
  id: number;
  tipo: TipoContenido;
  titulo: string;
  anio: number | null;
  imagenUrl: string | null;
}

/** GET /api/odisea/titulo/{id}: la ficha completa. */
export interface TituloForm {
  id: number;
  tipo: TipoContenido;
  titulo: string;
  tituloOriginal: string | null;
  anio: number | null;
  sinopsis: string | null;
  imagenUrl: string | null;
  generos: string | null;
  /** Minutos en una pelicula, paginas en un libro, null en juegos */
  duracionMin: number | null;
  fuenteExterna: FuenteExterna;
  idExterno: string | null;
}

/** GET /api/odisea/catalogo/buscar: un resultado de la fuente externa, todavia sin guardar. */
export interface ResultadoCatalogo {
  fuenteExterna: FuenteExterna;
  idExterno: string;
  tipo: TipoContenido;
  titulo: string;
  tituloOriginal: string | null;
  anio: number | null;
  sinopsis: string | null;
  imagenUrl: string | null;
  /** Id del titulo en el catalogo de Polaris si ya esta importado */
  tituloId: number | null;
}

const BASE = '/api/odisea';

export const claves = {
  entradas: ['odisea', 'entradas'] as const,
  entrada: (id: number) => ['odisea', 'entrada', id] as const,
  titulos: ['odisea', 'titulos'] as const,
  titulo: (id: number) => ['odisea', 'titulo', id] as const,
  catalogo: (q: string, tipo: TipoContenido) => ['odisea', 'catalogo', tipo, q] as const,
};

// ---------- Lecturas ----------

/** Toda tu lista. Los filtros de la pantalla se aplican en cliente para tener los contadores. */
export function useEntradas() {
  return useQuery({
    queryKey: claves.entradas,
    queryFn: () => api<EntradaList[]>(`${BASE}/entrada`),
  });
}

/** El catalogo compartido; solo se usa para el año de cada fila, que el listado de entradas no trae. */
export function useTitulos() {
  return useQuery({
    queryKey: claves.titulos,
    queryFn: () => api<TituloList[]>(`${BASE}/titulo`),
    staleTime: 60_000,
  });
}

export function useEntrada(id: number | undefined) {
  return useQuery({
    queryKey: claves.entrada(id ?? 0),
    queryFn: () => api<EntradaForm>(`${BASE}/entrada/${id}`),
    enabled: id !== undefined,
  });
}

export function useTitulo(id: number | undefined) {
  return useQuery({
    queryKey: claves.titulo(id ?? 0),
    queryFn: () => api<TituloForm>(`${BASE}/titulo/${id}`),
    enabled: id !== undefined,
    staleTime: 60_000,
  });
}

/** Busca en la fuente externa que corresponde a cada tipo (una peticion por tipo). No guarda nada. */
export function useBuscarCatalogo(q: string, tipos: TipoContenido[]) {
  return useQueries({
    queries: tipos.map((tipo) => ({
      queryKey: claves.catalogo(q, tipo),
      queryFn: () => api<ResultadoCatalogo[]>(`${BASE}/catalogo/buscar`, { query: { q, tipo } }),
      enabled: q.length > 0,
      // Un 4xx (fuente sin configurar, consulta que no vale) no se arregla reintentando.
      retry: (n: number, e: Error) => !(e instanceof ApiError && e.status < 500) && n < 1,
      staleTime: 5 * 60_000,
    })),
  });
}

// ---------- Escrituras ----------

/** Pasa una entrada completa al cuerpo del PUT, con los cambios encima. */
export function aRequest(e: EntradaForm, cambios: Partial<EntradaRequest> = {}): EntradaRequest {
  return {
    tituloId: e.tituloId,
    estado: e.estado,
    valoracion: e.valoracion,
    notas: e.notas,
    fechaInicio: e.fechaInicio,
    fechaFin: e.fechaFin,
    favorito: e.favorito,
    progreso: e.progreso,
    ...cambios,
  };
}

export function useActualizarEntrada(id: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (cuerpo: EntradaRequest) =>
      api<EntradaForm>(`${BASE}/entrada/${id}`, { metodo: 'PUT', cuerpo }),
    onSuccess: (e) => qc.setQueryData(claves.entrada(id), e),
    onSettled: () => qc.invalidateQueries({ queryKey: claves.entradas }),
  });
}

/**
 * `alBorrar` va en las opciones del hook y no en `mutate()`: al refrescar la lista la ficha desaparece,
 * y los callbacks de `mutate()` no se ejecutan si el componente ya no esta montado.
 */
export function useBorrarEntrada(alBorrar?: () => void) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api<void>(`${BASE}/entrada/${id}`, { metodo: 'DELETE' }),
    onSuccess: (_r, id) => {
      alBorrar?.();
      qc.removeQueries({ queryKey: claves.entrada(id) });
      return qc.invalidateQueries({ queryKey: claves.entradas });
    },
  });
}

/** Del catalogo externo a tu lista: crea (o reutiliza) la ficha y una entrada PENDIENTE. */
export function useImportar() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (r: Pick<ResultadoCatalogo, 'idExterno' | 'tipo'>) =>
      api<EntradaForm>(`${BASE}/catalogo/importar`, {
        metodo: 'POST',
        cuerpo: { idExterno: r.idExterno, tipo: r.tipo },
      }),
    onSuccess: (e) => {
      qc.setQueryData(claves.entrada(e.id), e);
      return Promise.all([
        qc.invalidateQueries({ queryKey: claves.entradas }),
        qc.invalidateQueries({ queryKey: claves.titulos }),
        // Los resultados guardados llevan tituloId a null para esta ficha.
        qc.invalidateQueries({ queryKey: ['odisea', 'catalogo'] }),
      ]);
    },
  });
}

// ---------- Presentacion ----------

/** Mensaje legible de un fallo de la API. Las fuentes sin credenciales responden 400 con el motivo. */
export function mensajeError(e: unknown): string {
  if (e instanceof ApiError) {
    if (/no est[aá] configurada/i.test(e.message)) return 'Esta fuente no está configurada.';
    if (e.status === 409) return 'Ya tienes ese título en tu lista.';
    if (e.status === 502) return 'La fuente externa no responde. Prueba otra vez en un rato.';
    return e.message;
  }
  return 'No se ha podido conectar con el servidor.';
}

/** El progreso (episodio o página) solo tiene sentido en series y libros. */
export function tieneProgreso(tipo: TipoContenido): boolean {
  return tipo === 'SERIE' || tipo === 'LIBRO';
}

const FMT_FECHA = new Intl.DateTimeFormat('es-ES', { day: 'numeric', month: 'short', year: 'numeric' });

/** "2026-09-01" -> "1 sept 2026". Se parte a mano: new Date('2026-09-01') es UTC y se puede desplazar un dia. */
export function formatearFecha(d: string | null | undefined): string | null {
  if (!d) return null;
  const [y, m, dia] = d.split('-').map(Number);
  if (!y || !m || !dia) return null;
  return FMT_FECHA.format(new Date(y, m - 1, dia)).replace(/\./g, '');
}

/** Minutos en pelicula y serie, paginas en libro, nada en juegos. */
export function formatearDuracion(tipo: TipoContenido, d: number | null | undefined): string | null {
  if (d == null) return null;
  const n = d.toLocaleString('es-ES');
  return tipo === 'LIBRO' ? `${n} páginas` : `${n} min`;
}
