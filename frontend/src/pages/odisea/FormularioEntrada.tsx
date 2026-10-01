import { useState, type FormEvent } from 'react';
import {
  aRequest,
  ESTADOS,
  ETIQUETA_ESTADO,
  mensajeError,
  tieneProgreso,
  useActualizarEntrada,
  type EntradaForm,
  type EstadoEntrada,
  type TipoContenido,
} from '../../api/odisea';
import { Alert, Button, Dialog, IconButton, Input, Rating, Select, Switch } from '../../design-system';
import { useRestaurarFoco } from './useRestaurarFoco';

interface Props {
  entrada: EntradaForm;
  tipo: TipoContenido;
  onClose: () => void;
}

const OPCIONES_ESTADO = ESTADOS.map((e) => ({ value: e, label: ETIQUETA_ESTADO[e] }));

const ETIQUETA_PROGRESO: Partial<Record<TipoContenido, string>> = {
  SERIE: 'Episodio por el que vas',
  LIBRO: 'Página por la que vas',
};

/** Edición completa de la entrada. El PUT reemplaza todos los campos, así que se manda la entrada entera. */
export function FormularioEntrada({ entrada, tipo, onClose }: Props) {
  useRestaurarFoco();
  const actualizar = useActualizarEntrada(entrada.id);
  const [estado, setEstado] = useState<EstadoEntrada>(entrada.estado);
  const [progreso, setProgreso] = useState(entrada.progreso != null ? String(entrada.progreso) : '');
  const [valoracion, setValoracion] = useState<number | null>(entrada.valoracion);
  const [fechaInicio, setFechaInicio] = useState(entrada.fechaInicio ?? '');
  const [fechaFin, setFechaFin] = useState(entrada.fechaFin ?? '');
  const [favorito, setFavorito] = useState(entrada.favorito);
  const [notas, setNotas] = useState(entrada.notas ?? '');

  const progresoNum = progreso.trim() === '' ? null : Number(progreso);
  const errorProgreso =
    tieneProgreso(tipo) && progresoNum !== null && (!Number.isInteger(progresoNum) || progresoNum < 0) ? 'Escribe un número entero, 0 o más.' : null;
  const errorFechas = fechaInicio && fechaFin && fechaFin < fechaInicio ? 'El fin no puede ser anterior al inicio.' : null;

  const guardar = (ev: FormEvent) => {
    ev.preventDefault();
    if (errorProgreso || errorFechas) return;
    actualizar.mutate(
      aRequest(entrada, {
        estado,
        // El progreso solo tiene sentido en series (episodio) y libros (página): en el resto se deja como está.
        progreso: tieneProgreso(tipo) ? progresoNum : entrada.progreso,
        valoracion,
        fechaInicio: fechaInicio || null,
        fechaFin: fechaFin || null,
        favorito,
        notas: notas.trim() === '' ? null : notas,
      }),
      { onSuccess: onClose },
    );
  };

  return (
    <Dialog
      open
      onClose={onClose}
      title="Editar entrada"
      width={520}
      footer={
        <>
          <Button variant="ghost" type="button" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" form="odisea-form-entrada" loading={actualizar.isPending} disabled={!!errorProgreso || !!errorFechas}>
            Guardar
          </Button>
        </>
      }
    >
      <form id="odisea-form-entrada" className="stack-16 odisea-form" onSubmit={guardar} noValidate>
        {actualizar.isError && <Alert tone="danger">{mensajeError(actualizar.error)}</Alert>}

        <div className="odisea-form__row">
          <Select
            id="of-estado"
            autoFocus
            label="Estado"
            value={estado}
            onChange={(ev) => setEstado(ev.target.value as EstadoEntrada)}
            options={OPCIONES_ESTADO}
          />
          {tieneProgreso(tipo) && (
            <Input
              id="of-progreso"
              label={ETIQUETA_PROGRESO[tipo]}
              type="number"
              inputMode="numeric"
              min={0}
              step={1}
              placeholder="—"
              value={progreso}
              onChange={(ev) => setProgreso(ev.target.value)}
              error={errorProgreso}
            />
          )}
        </div>

        <div className="odisea-form__row">
          <Input id="of-inicio" label="Inicio" type="date" value={fechaInicio} onChange={(ev) => setFechaInicio(ev.target.value)} />
          <Input id="of-fin" label="Fin" type="date" value={fechaFin} onChange={(ev) => setFechaFin(ev.target.value)} error={errorFechas} />
        </div>

        <div className="odisea-form__row">
          <div className="pl-field">
            <span className="pl-field__label">Valoración</span>
            <span className="odisea-form__rating">
              <Rating value={valoracion} size={20} onChange={setValoracion} />
              <IconButton icon="x" label="Quitar valoración" size="sm" type="button" disabled={valoracion == null} onClick={() => setValoracion(null)} />
            </span>
          </div>
          <div className="pl-field">
            <span className="pl-field__label">Favorito</span>
            <Switch checked={favorito} onChange={setFavorito} label={favorito ? 'Sí' : 'No'} />
          </div>
        </div>

        <div className="pl-field">
          <label className="pl-field__label" htmlFor="of-notas">
            Notas
          </label>
          <textarea
            id="of-notas"
            className="odisea-textarea"
            rows={4}
            placeholder="Qué te ha parecido, dónde lo viste, lo que quieras recordar"
            value={notas}
            onChange={(ev) => setNotas(ev.target.value)}
          />
        </div>
      </form>
    </Dialog>
  );
}
