import { useEffect, useMemo, useState } from 'react';
import {
  ETIQUETA_ESTADO,
  mensajeError,
  useEntradas,
  type EntradaForm,
  type EstadoEntrada,
  type TipoContenido,
} from '../api/odisea';
import { PageHeader } from '../components/PageHeader';
import { Alert, Button, IconButton, Input, SegmentedControl, Toast } from '../design-system';
import { AltaDialog } from './odisea/AltaDialog';
import { Ficha } from './odisea/Ficha';
import { ListaEntradas } from './odisea/ListaEntradas';
import './odisea/odisea.css';

const TIPOS_BARRA: [TipoContenido, string, string][] = [
  ['PELICULA', 'clapperboard', 'Películas'],
  ['SERIE', 'tv', 'Series'],
  ['JUEGO', 'gamepad-2', 'Juegos'],
  ['LIBRO', 'book-open', 'Libros'],
];

// El orden de la barra: lo que tienes entre manos primero.
const ORDEN_ESTADOS: EstadoEntrada[] = ['EN_CURSO', 'PENDIENTE', 'TERMINADO', 'ABANDONADO'];

type FiltroEstado = 'ALL' | EstadoEntrada;

/** Normaliza para filtrar sin distinguir mayúsculas ni tildes. */
const plano = (s: string) => s.normalize('NFD').replace(/\p{Diacritic}/gu, '').toLowerCase();

export function Odisea() {
  const entradas = useEntradas();

  const [estado, setEstado] = useState<FiltroEstado>('ALL');
  const [tipo, setTipo] = useState<TipoContenido | null>(null);
  const [q, setQ] = useState('');
  const [sel, setSel] = useState<number | null>(null);
  const [alta, setAlta] = useState(false);
  const [aviso, setAviso] = useState<string | null>(null);

  useEffect(() => {
    if (!aviso) return;
    const t = setTimeout(() => setAviso(null), 3200);
    return () => clearTimeout(t);
  }, [aviso]);

  // En pantallas estrechas el detalle queda debajo de la lista: al elegir una entrada se baja hasta el.
  const elegir = (id: number) => {
    setSel(id);
    if (window.matchMedia('(max-width:1100px)').matches) {
      setTimeout(() => document.querySelector('.detail')?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 80);
    }
  };

  const todas = useMemo(() => entradas.data ?? [], [entradas.data]);
  const enLista = useMemo(() => new Set(todas.map((e) => e.tituloId)), [todas]);

  const lista = useMemo(() => {
    const texto = plano(q.trim());
    return todas.filter(
      (e) => (estado === 'ALL' || e.estado === estado) && (!tipo || e.tituloTipo === tipo) && plano(e.tituloTitulo).includes(texto),
    );
  }, [todas, estado, tipo, q]);

  // Si lo seleccionado ya no se ve (filtro, borrado), la ficha pasa a la primera fila visible.
  const seleccionada = lista.find((e) => e.id === sel) ?? lista[0];

  const cargada = entradas.isSuccess;
  const cuenta = (s: EstadoEntrada) => todas.filter((e) => e.estado === s).length;
  const coord = entradas.data ? `${todas.length} ${todas.length === 1 ? 'TÍTULO' : 'TÍTULOS'}` : undefined;

  const alImportar = (e: EntradaForm) => {
    // La nueva entrada es PENDIENTE: se quitan los filtros para que se vea.
    setEstado('ALL');
    setTipo(null);
    setQ('');
    setSel(e.id);
    setAlta(false);
    setAviso(`${e.tituloTitulo} añadido a Pendientes`);
  };

  const vacio = todas.length ? 'Nada con estos filtros.' : 'Tu lista está vacía. Añade tu primer título.';

  return (
    <div>
      <PageHeader
        eyebrow="Odisea"
        coord={coord}
        title="Tu lista"
        actions={
          <Button icon="plus" type="button" onClick={() => setAlta(true)}>
            Añadir título
          </Button>
        }
      />

      <div className="toolbar pl-rise" style={{ animationDelay: '140ms' }}>
        <SegmentedControl
          value={estado}
          onChange={(v) => setEstado(v as FiltroEstado)}
          options={[
            { value: 'ALL', label: 'Todo', count: cargada ? todas.length : undefined },
            ...ORDEN_ESTADOS.map((s) => ({
              value: s,
              label: ETIQUETA_ESTADO[s],
              count: cargada ? cuenta(s) : undefined,
            })),
          ]}
        />
        <div className="toolbar__types" role="group" aria-label="Tipo de contenido">
          {TIPOS_BARRA.map(([t, icono, etiqueta]) => (
            <IconButton
              key={t}
              icon={icono}
              label={etiqueta}
              pressed={tipo === t}
              variant={tipo === t ? 'outline' : 'ghost'}
              aria-pressed={tipo === t}
              onClick={() => setTipo(tipo === t ? null : t)}
            />
          ))}
        </div>
        <Input
          size="sm"
          icon="search"
          type="search"
          aria-label="Filtrar por título"
          placeholder="Filtrar por título"
          value={q}
          onChange={(ev) => setQ(ev.target.value)}
          style={{ width: 240 }}
        />
      </div>

      {entradas.isError ? (
        <Alert
          tone="danger"
          title="No se ha podido cargar tu lista"
          action={
            <Button size="sm" variant="secondary" type="button" onClick={() => void entradas.refetch()}>
              Reintentar
            </Button>
          }
        >
          {mensajeError(entradas.error)}
        </Alert>
      ) : (
        <div className="odisea">
          {entradas.isPending ? (
            <ListaEsqueleto />
          ) : (
            <ListaEntradas entradas={lista} seleccionada={seleccionada?.id} onSeleccionar={elegir} vacio={vacio} />
          )}
          {seleccionada && (
            <Ficha
              key={seleccionada.id}
              entradaId={seleccionada.id}
              tituloId={seleccionada.tituloId}
              onBorrada={(titulo) => setAviso(`${titulo} borrado de tu lista`)}
            />
          )}
        </div>
      )}

      {alta && <AltaDialog tituloIdsEnLista={enLista} tipoInicial={tipo} onClose={() => setAlta(false)} onImportada={alImportar} />}
      {aviso && (
        <Toast fixed tone="success" onClose={() => setAviso(null)}>
          {aviso}
        </Toast>
      )}
    </div>
  );
}

function ListaEsqueleto() {
  return (
    <div className="listbox" aria-busy="true" aria-label="Cargando tu lista">
      {Array.from({ length: 8 }, (_, i) => (
        <div key={i} className="odisea-sk-row">
          <span className="sk sk--line" style={{ width: `${40 + ((i * 17) % 40)}%` }} />
        </div>
      ))}
    </div>
  );
}
