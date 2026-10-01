import { useState } from 'react';
import {
  aRequest,
  ESTADOS,
  tieneProgreso,
  ETIQUETA_ESTADO,
  formatearDuracion,
  formatearFecha,
  mensajeError,
  useActualizarEntrada,
  useEntrada,
  useTitulo,
  type EntradaForm,
  type EntradaRequest,
  type EstadoEntrada,
  type TituloForm,
} from '../../api/odisea';
import { Alert, Button, Eyebrow, ProgressBar, Rating, SegmentedControl, Switch, TypeTag } from '../../design-system';
import { BorrarEntrada } from './BorrarEntrada';
import { Caratula } from './Caratula';
import { FormularioEntrada } from './FormularioEntrada';

interface Props {
  entradaId: number;
  tituloId: number;
  onBorrada: (titulo: string) => void;
}

const OPCIONES_ESTADO = ESTADOS.map((e) => ({ value: e, label: ETIQUETA_ESTADO[e] }));

/** Ficha de detalle: la entrada (tu relación con el título) y el título (la ficha del catálogo). */
export function Ficha({ entradaId, tituloId, onBorrada }: Props) {
  const entrada = useEntrada(entradaId);
  const titulo = useTitulo(tituloId);

  if (entrada.isPending || titulo.isPending) return <FichaEsqueleto />;

  if (entrada.isError || titulo.isError) {
    return (
      <aside className="detail" aria-label="Detalle">
        <Alert
          tone="danger"
          action={
            <Button
              size="sm"
              variant="secondary"
              type="button"
              onClick={() => {
                void entrada.refetch();
                void titulo.refetch();
              }}
            >
              Reintentar
            </Button>
          }
        >
          {mensajeError(entrada.error ?? titulo.error)}
        </Alert>
      </aside>
    );
  }

  return <FichaCompleta e={entrada.data} t={titulo.data} onBorrada={onBorrada} />;
}

function FichaEsqueleto() {
  return (
    <aside className="detail" aria-label="Detalle" aria-busy="true">
      <div className="detail__top">
        <div className="cover sk" />
        <div className="stack-8">
          <span className="sk sk--line" style={{ width: '40%' }} />
          <span className="sk sk--line sk--h" style={{ width: '90%' }} />
          <span className="sk sk--line" style={{ width: '60%' }} />
        </div>
      </div>
      <span className="sk sk--line" style={{ width: '100%' }} />
      <span className="sk sk--line" style={{ width: '80%' }} />
    </aside>
  );
}

function FichaCompleta({ e, t, onBorrada }: { e: EntradaForm; t: TituloForm; onBorrada: (titulo: string) => void }) {
  const actualizar = useActualizarEntrada(e.id);
  const [editando, setEditando] = useState(false);
  const [borrando, setBorrando] = useState(false);

  const cambiar = (cambios: Partial<EntradaRequest>) => actualizar.mutate(aRequest(e, cambios));

  const duracion = formatearDuracion(t.tipo, t.duracionMin);
  const meta = [t.anio, duracion].filter(Boolean).join(' · ') || 'Sin año ni duración';
  const muestraProgreso = e.estado === 'EN_CURSO' && tieneProgreso(t.tipo);

  // Los diálogos van fuera del aside: su animación de entrada crea un contexto que encerraría al diálogo fijo.
  return (
    <>
      <aside className="detail" aria-label={`Detalle de ${t.titulo}`}>
        <div className="detail__top">
          <Caratula titulo={t.titulo} tipo={t.tipo} anio={t.anio} imagenUrl={t.imagenUrl} big />
          <div className="stack-8" style={{ minWidth: 0 }}>
            <Eyebrow star coord={t.fuenteExterna.replace('_', ' ')}>
              <TypeTag tipo={t.tipo} size={12} />
            </Eyebrow>
            <h2 className="detail__h">{t.titulo}</h2>
            {t.tituloOriginal && t.tituloOriginal !== t.titulo && <span className="muted">{t.tituloOriginal}</span>}
            <span className="detail__meta">{meta}</span>
            <span className="muted" style={{ fontSize: 13 }}>
              {t.generos || '—'}
            </span>
          </div>
        </div>

        {actualizar.isError && (
          <Alert tone="danger" title="No se ha guardado el cambio">
            {mensajeError(actualizar.error)}
          </Alert>
        )}

        {t.sinopsis ? <p className="detail__syn">{t.sinopsis}</p> : <p className="detail__syn detail__syn--empty">Sin sinopsis.</p>}

        <div className="detail__block">
          <span className="pl-eyebrow">Estado</span>
          <SegmentedControl value={e.estado} onChange={(v) => cambiar({ estado: v as EstadoEntrada })} options={OPCIONES_ESTADO} />
        </div>

        {muestraProgreso && (
          <div className="detail__block">
            {t.tipo === 'LIBRO' ? (
              e.progreso != null && t.duracionMin ? (
                <ProgressBar
                  label="Progreso"
                  value={e.progreso}
                  max={t.duracionMin}
                  valueLabel={`pág. ${e.progreso.toLocaleString('es-ES')} / ${t.duracionMin.toLocaleString('es-ES')}`}
                  size="lg"
                />
              ) : (
                <div className="stack-8">
                  <span className="pl-eyebrow">Progreso</span>
                  <b className="big">{e.progreso != null ? `Página ${e.progreso.toLocaleString('es-ES')}` : '—'}</b>
                </div>
              )
            ) : (
              <div className="stack-8">
                <span className="pl-eyebrow">Progreso</span>
                <span style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <b className="big">{e.progreso != null ? `Episodio ${e.progreso}` : '—'}</b>
                  <Button size="sm" variant="secondary" icon="plus" type="button" onClick={() => cambiar({ progreso: (e.progreso ?? 0) + 1 })}>
                    1
                  </Button>
                </span>
              </div>
            )}
          </div>
        )}

        <div className="detail__grid">
          <div className="stack-8">
            <span className="pl-eyebrow">Valoración</span>
            <Rating value={e.valoracion} size={20} onChange={(v) => cambiar({ valoracion: v === e.valoracion ? null : v })} />
          </div>
          <div className="stack-8">
            <span className="pl-eyebrow">Favorito</span>
            <Switch checked={e.favorito} onChange={(v) => cambiar({ favorito: v })} label={e.favorito ? 'Sí' : 'No'} />
          </div>
          <div className="stack-8">
            <span className="pl-eyebrow">Inicio</span>
            <span className="detail__meta">{formatearFecha(e.fechaInicio) ?? '—'}</span>
          </div>
          <div className="stack-8">
            <span className="pl-eyebrow">Fin</span>
            <span className="detail__meta">{formatearFecha(e.fechaFin) ?? '—'}</span>
          </div>
        </div>

        <div className="detail__block">
          <span className="pl-eyebrow">Notas</span>
          {e.notas ? <p className="detail__notes">{e.notas}</p> : <p className="detail__syn--empty" style={{ margin: 0, fontSize: 13 }}>Sin notas todavía.</p>}
        </div>

        <div className="detail__actions">
          <Button size="sm" variant="secondary" type="button" onClick={() => setEditando(true)}>
            Editar
          </Button>
          <Button size="sm" variant="danger" type="button" onClick={() => setBorrando(true)}>
            Borrar
          </Button>
        </div>
      </aside>
      {editando && <FormularioEntrada entrada={e} tipo={t.tipo} onClose={() => setEditando(false)} />}
      {borrando && <BorrarEntrada entrada={e} onClose={() => setBorrando(false)} onBorrada={() => onBorrada(t.titulo)} />}
    </>
  );
}
