import { useState, type FormEvent } from 'react';
import { mensajeError, useActualizarAlimento, useBorrarAlimento, useCrearAlimento, type Alimento } from '../../api/fusion';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Button, Dialog, Input } from '../../design-system';

interface Props {
  /** Sin alimento: alta. Con alimento: edicion (y borrado). */
  alimento?: Alimento;
  onClose: () => void;
}

const NUMERO = /^\d{1,3}([.,]\d{1,2})?$/;
const texto = (n: number | undefined) => (n === undefined ? '' : String(n).replace('.', ','));

export function FormularioAlimento({ alimento, onClose }: Props) {
  useRestaurarFoco();
  const editando = alimento !== undefined;
  const crear = useCrearAlimento();
  const actualizar = useActualizarAlimento(alimento?.id ?? 0);
  const borrar = useBorrarAlimento(onClose);

  const [nombre, setNombre] = useState(alimento?.nombre ?? '');
  const [marca, setMarca] = useState(alimento?.marca ?? '');
  const [kcal, setKcal] = useState(texto(alimento?.kcal100g));
  const [prot, setProt] = useState(texto(alimento?.proteinas100g));
  const [carb, setCarb] = useState(texto(alimento?.carbohidratos100g));
  const [gras, setGras] = useState(texto(alimento?.grasas100g));
  const [confirmando, setConfirmando] = useState(false);
  const [errorEnvio, setErrorEnvio] = useState<string | null>(null);

  const valor = (s: string, max: number) => (NUMERO.test(s.trim()) && Number(s.trim().replace(',', '.')) <= max ? Number(s.trim().replace(',', '.')) : NaN);
  const k = valor(kcal, 900);
  const p = valor(prot, 100);
  const c = valor(carb, 100);
  const g = valor(gras, 100);
  const errNombre = nombre.trim() === '' ? 'Escribe un nombre.' : null;
  const err = (v: number, max: number) => (Number.isNaN(v) ? `0 a ${max}, con hasta 2 decimales.` : null);
  const ocupado = crear.isPending || actualizar.isPending || borrar.isPending;

  const guardar = (ev: FormEvent) => {
    ev.preventDefault();
    setErrorEnvio(null);
    if (errNombre || [k, p, c, g].some(Number.isNaN)) return;
    const cuerpo = { nombre: nombre.trim(), marca: marca.trim() || null, kcal100g: k, proteinas100g: p, carbohidratos100g: c, grasas100g: g };
    const opciones = { onSuccess: onClose, onError: (e: unknown) => setErrorEnvio(mensajeError(e)) };
    if (editando) actualizar.mutate(cuerpo, opciones);
    else crear.mutate(cuerpo, opciones);
  };

  const borrarAlimento = () => {
    if (!alimento) return;
    setErrorEnvio(null);
    borrar.mutate(alimento.id, {
      onError: (e) => {
        setConfirmando(false);
        setErrorEnvio(mensajeError(e));
      },
    });
  };

  return (
    <Dialog
      open
      onClose={onClose}
      confirmarDescarte
      title={editando ? 'Editar alimento' : 'Nuevo alimento'}
      footer={
        confirmando ? (
          <>
            <span className="muted" style={{ marginRight: 'auto' }}>¿Borrar este alimento?</span>
            <Button variant="ghost" type="button" autoFocus onClick={() => setConfirmando(false)}>
              No
            </Button>
            <Button variant="danger" type="button" loading={borrar.isPending} onClick={borrarAlimento}>
              Sí, borrar
            </Button>
          </>
        ) : (
          <>
            {editando && (
              <Button variant="ghost" type="button" style={{ marginRight: 'auto' }} disabled={ocupado} onClick={() => setConfirmando(true)}>
                Borrar
              </Button>
            )}
            <Button variant="ghost" type="button" onClick={onClose}>
              Cancelar
            </Button>
            <Button type="submit" form="fus-form-alimento" loading={ocupado}>
              {editando ? 'Guardar' : 'Añadir'}
            </Button>
          </>
        )
      }
    >
      <form id="fus-form-alimento" className="fus-form" onSubmit={guardar} noValidate>
        {errorEnvio && <Alert tone="danger">{errorEnvio}</Alert>}
        <div className="fus-form__row">
          <Input label="Nombre" autoFocus maxLength={150} value={nombre} onChange={(e) => setNombre(e.target.value)} error={errNombre} validarAlSalir />
          <Input label="Marca" placeholder="Opcional" maxLength={100} value={marca} onChange={(e) => setMarca(e.target.value)} />
        </div>
        <span className="pl-eyebrow">Valores por 100 g</span>
        <div className="fus-form__row4">
          <Input label="Kcal" inputMode="decimal" value={kcal} onChange={(e) => setKcal(e.target.value)} error={err(k, 900)} validarAlSalir />
          <Input label="Prot. (g)" inputMode="decimal" value={prot} onChange={(e) => setProt(e.target.value)} error={err(p, 100)} validarAlSalir />
          <Input label="Carb. (g)" inputMode="decimal" value={carb} onChange={(e) => setCarb(e.target.value)} error={err(c, 100)} validarAlSalir />
          <Input label="Grasas (g)" inputMode="decimal" value={gras} onChange={(e) => setGras(e.target.value)} error={err(g, 100)} validarAlSalir />
        </div>
        {editando && (
          <p className="muted" style={{ margin: 0, fontSize: 12.5 }}>
            Si cambias los valores, se recalculan las kcal y macros de las comidas que ya usan este alimento.
          </p>
        )}
      </form>
    </Dialog>
  );
}
