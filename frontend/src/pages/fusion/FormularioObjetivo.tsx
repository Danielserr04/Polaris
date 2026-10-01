import { useState, type FormEvent } from 'react';
import { mensajeError, useCrearObjetivo, type ResumenDiario } from '../../api/fusion';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Button, Dialog, Input } from '../../design-system';
import { iso, num } from '../../lib/fechas';

interface Props {
  /** El resumen del dia que se ve: de ahi se precarga el objetivo vigente. */
  resumen: ResumenDiario | undefined;
  fecha: string;
  onClose: () => void;
}

const ENTERO = /^\d{1,5}$/;

function texto(n: number | null | undefined): string {
  return n === null || n === undefined ? '' : String(Math.round(n));
}

export function FormularioObjetivo({ resumen, fecha, onClose }: Props) {
  useRestaurarFoco();
  const crear = useCrearObjetivo();
  const hoy = iso(new Date());
  const [kcal, setKcal] = useState(texto(resumen?.kcal.objetivo));
  const [prot, setProt] = useState(texto(resumen?.proteinas.objetivo));
  const [carb, setCarb] = useState(texto(resumen?.carbohidratos.objetivo));
  const [gras, setGras] = useState(texto(resumen?.grasas.objetivo));
  const [desde, setDesde] = useState(fecha > hoy ? hoy : fecha);
  const [intentado, setIntentado] = useState(false);
  const [errorEnvio, setErrorEnvio] = useState<string | null>(null);

  const entero = (s: string) => (ENTERO.test(s.trim()) ? Number(s.trim()) : NaN);
  const k = entero(kcal);
  const p = entero(prot);
  const c = entero(carb);
  const g = entero(gras);
  const errKcal = !(k >= 500 && k <= 10000) ? 'Entre 500 y 10.000 kcal.' : null;
  const errMacro = (v: number) => (!(v >= 0 && v <= 1000) ? 'Entre 0 y 1.000 g.' : null);
  const errDesde = !desde ? 'Elige una fecha.' : null;
  const ver = (e: string | null) => (intentado ? e : null);
  const sumaMacros = [p, c, g].every((v) => v >= 0) ? Math.round(p * 4 + c * 4 + g * 9) : null;

  const guardar = (ev: FormEvent) => {
    ev.preventDefault();
    setIntentado(true);
    setErrorEnvio(null);
    if (errKcal || errMacro(p) || errMacro(c) || errMacro(g) || errDesde) return;
    crear.mutate(
      { kcalDiarias: k, proteinasObj: p, carbosObj: c, grasasObj: g, vigenteDesde: desde },
      { onSuccess: onClose, onError: (e) => setErrorEnvio(mensajeError(e)) },
    );
  };

  return (
    <Dialog
      open
      onClose={onClose}
      title={resumen?.objetivoVigenteDesde ? 'Cambiar objetivo' : 'Fijar objetivo'}
      footer={
        <>
          <Button variant="ghost" type="button" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" form="fus-form-objetivo" loading={crear.isPending}>
            Guardar
          </Button>
        </>
      }
    >
      <form id="fus-form-objetivo" className="fus-form" onSubmit={guardar} noValidate>
        {errorEnvio && <Alert tone="danger">{errorEnvio}</Alert>}
        <div className="fus-form__row">
          <Input label="Calorías al día" inputMode="numeric" autoFocus value={kcal} onChange={(e) => setKcal(e.target.value)} error={ver(errKcal)} />
          <Input label="Vigente desde" type="date" value={desde} onChange={(e) => setDesde(e.target.value)} error={ver(errDesde)} />
        </div>
        <div className="fus-form__row4" style={{ gridTemplateColumns: 'repeat(3,minmax(0,1fr))' }}>
          <Input label="Proteínas (g)" inputMode="numeric" value={prot} onChange={(e) => setProt(e.target.value)} error={ver(errMacro(p))} />
          <Input label="Carbohidratos (g)" inputMode="numeric" value={carb} onChange={(e) => setCarb(e.target.value)} error={ver(errMacro(c))} />
          <Input label="Grasas (g)" inputMode="numeric" value={gras} onChange={(e) => setGras(e.target.value)} error={ver(errMacro(g))} />
        </div>
        {sumaMacros !== null && (
          <p className="muted" style={{ margin: 0, fontSize: 12.5 }}>
            Esos macros suman unas {num(sumaMacros)} kcal.
          </p>
        )}
        <p className="muted" style={{ margin: 0, fontSize: 12.5 }}>
          Se guarda como un objetivo nuevo desde esa fecha; el histórico no se modifica.
        </p>
      </form>
    </Dialog>
  );
}
