import { useEffect, useState } from 'react';
import { ApiError } from '../../api/client';
import {
  mensajeError,
  TIPOS,
  useBuscarCatalogo,
  useImportar,
  type EntradaForm,
  type ResultadoCatalogo,
  type TipoContenido,
} from '../../api/odisea';
import { Alert, Badge, Button, Dialog, Input, SegmentedControl, TypeTag } from '../../design-system';
import { useRestaurarFoco } from './useRestaurarFoco';

interface Props {
  /** Ids de título que ya tienes en tu lista, para no ofrecer añadirlos otra vez */
  tituloIdsEnLista: ReadonlySet<number>;
  tipoInicial: TipoContenido | null;
  onClose: () => void;
  onImportada: (entrada: EntradaForm) => void;
}

type Filtro = 'ALL' | TipoContenido;

const OPCIONES = [
  { value: 'ALL', label: 'Todo' },
  { value: 'PELICULA', label: 'Pelis', icon: 'clapperboard' },
  { value: 'SERIE', label: 'Series', icon: 'tv' },
  { value: 'JUEGO', label: 'Juegos', icon: 'gamepad-2' },
  { value: 'LIBRO', label: 'Libros', icon: 'book-open' },
];

const PLURAL: Record<TipoContenido, string> = {
  PELICULA: 'Películas',
  SERIE: 'Series',
  JUEGO: 'Juegos',
  LIBRO: 'Libros',
};

const MIN_LETRAS = 2;

function useRetardo<T>(valor: T, ms: number): T {
  const [retardado, setRetardado] = useState(valor);
  useEffect(() => {
    const t = setTimeout(() => setRetardado(valor), ms);
    return () => clearTimeout(t);
  }, [valor, ms]);
  return retardado;
}

/** "Películas y series: Esta fuente no está configurada." */
function unir(etiquetas: string[]): string {
  const [primera, ...resto] = etiquetas;
  const minusculas = resto.map((e) => e.toLowerCase());
  return minusculas.length ? `${[primera, ...minusculas.slice(0, -1)].join(', ')} y ${minusculas[minusculas.length - 1]}` : primera;
}

/** Alta de un título: busca en TMDB, IGDB u OpenLibrary según el tipo y lo importa a tu lista como pendiente. */
export function AltaDialog({ tituloIdsEnLista, tipoInicial, onClose, onImportada }: Props) {
  useRestaurarFoco();
  const [tipo, setTipo] = useState<Filtro>(tipoInicial ?? 'ALL');
  const [q, setQ] = useState('');
  const consulta = useRetardo(q.trim(), 350);
  const buscar = consulta.length >= MIN_LETRAS;
  const tipos = tipo === 'ALL' ? TIPOS : [tipo];

  const busquedas = useBuscarCatalogo(buscar ? consulta : '', tipos);
  const importar = useImportar();
  const [importando, setImportando] = useState<string | null>(null);

  const esperando = q.trim() !== consulta;
  const cargando = buscar && busquedas.some((b) => b.isFetching);
  const resultados = busquedas.flatMap((b) => b.data ?? []);

  // Los fallos se agrupan por mensaje: películas y series comparten fuente y fallan juntas.
  // Reintentar solo tiene sentido si el fallo puede ser pasajero: un 4xx (fuente sin configurar,
  // consulta que la fuente no admite) saldría igual.
  const fallos = new Map<string, { tipos: TipoContenido[]; reintentable: boolean }>();
  busquedas.forEach((b, i) => {
    if (!b.isError) return;
    const m = mensajeError(b.error);
    const previo = fallos.get(m);
    fallos.set(m, {
      tipos: [...(previo?.tipos ?? []), tipos[i]],
      reintentable: !(b.error instanceof ApiError && b.error.status < 500),
    });
  });
  const hayDatos = busquedas.some((b) => b.isSuccess);
  const reintentar = () => busquedas.forEach((b) => b.isError && void b.refetch());

  const añadir = (r: ResultadoCatalogo) => {
    setImportando(r.fuenteExterna + r.idExterno);
    importar.mutate(r, { onSuccess: onImportada, onSettled: () => setImportando(null) });
  };

  return (
    <Dialog open onClose={onClose} title="Añadir a Odisea" width={720}>
      <div className="stack-12">
        <Input
          icon="search"
          autoFocus
          aria-label="Buscar título"
          placeholder="Busca en TMDB, IGDB y OpenLibrary"
          value={q}
          onChange={(ev) => setQ(ev.target.value)}
        />
        <SegmentedControl value={tipo} onChange={(v) => setTipo(v as Filtro)} options={OPCIONES} />

        {importar.isError && <Alert tone="danger">{mensajeError(importar.error)}</Alert>}

        {[...fallos].map(([mensaje, { tipos: afectados, reintentable }]) => (
          <Alert
            key={mensaje}
            tone={hayDatos ? 'warning' : 'danger'}
            action={
              !reintentable ? undefined : (
                <Button size="sm" variant="secondary" type="button" onClick={reintentar}>
                  Reintentar
                </Button>
              )
            }
          >
            {tipos.length > 1 ? `${unir(afectados.map((t) => PLURAL[t]))}: ` : ''}
            {mensaje}
          </Alert>
        ))}

        <div className="results" role="list" aria-label="Resultados" aria-busy={cargando}>
          {resultados.map((r) => {
            const clave = r.fuenteExterna + r.idExterno;
            const enLista = r.tituloId !== null && tituloIdsEnLista.has(r.tituloId);
            const secundario = r.tipo === 'LIBRO' ? r.sinopsis : r.tituloOriginal && r.tituloOriginal !== r.titulo ? r.tituloOriginal : null;
            return (
              <div key={r.tipo + clave} className="result" role="listitem">
                <TypeTag tipo={r.tipo} showLabel={false} size={15} />
                <span className="result__t">
                  <b>{r.titulo}</b>
                  {secundario && <span className="muted"> · {secundario}</span>}
                </span>
                <span className="pl-row__num">{r.anio ?? '—'}</span>
                <Badge variant="outline">{r.fuenteExterna.replace('_', ' ')}</Badge>
                {enLista ? (
                  <Badge tone="success" variant="soft">
                    En tu lista
                  </Badge>
                ) : (
                  <Button
                    size="sm"
                    variant="secondary"
                    icon="plus"
                    type="button"
                    loading={importando === clave}
                    disabled={importando !== null}
                    aria-label={`Añadir ${r.titulo}`}
                    onClick={() => añadir(r)}
                  >
                    Añadir
                  </Button>
                )}
              </div>
            );
          })}
        </div>

        {!buscar && !esperando && <p className="cmd__empty">Escribe al menos {MIN_LETRAS} letras para buscar.</p>}
        {buscar && !esperando && cargando && !resultados.length && <p className="cmd__empty">Buscando…</p>}
        {buscar && !esperando && !cargando && !resultados.length && fallos.size === 0 && (
          <p className="cmd__empty">Sin resultados para «{consulta}».</p>
        )}
      </div>
    </Dialog>
  );
}
