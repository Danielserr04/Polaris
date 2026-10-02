import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { mensajeError, useCrearObjetivo, type ResumenDiario } from '../../api/fusion';
import { ETIQUETA_ACTIVIDAD, ETIQUETA_TIPO, mensajeErrorCalculo, useCalcularObjetivo, type TipoObjetivo } from '../../api/fusionCalculo';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Button, Dialog, Input, SegmentedControl } from '../../design-system';
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
  const [errorEnvio, setErrorEnvio] = useState<string | null>(null);
  const [tipo, setTipo] = useState<TipoObjetivo>('MANTENIMIENTO');
  const calcular = useCalcularObjetivo();
  const calculo = calcular.data;

  // Rellena los campos con la propuesta; el usuario puede retocarlos antes de guardar.
  const proponer = () =>
    calcular.mutate(tipo, {
      onSuccess: (c) => {
        setKcal(String(c.kcalDiarias));
        setProt(String(c.proteinasObj));
        setCarb(String(c.carbosObj));
        setGras(String(c.grasasObj));
      },
    });

  const entero = (s: string) => (ENTERO.test(s.trim()) ? Number(s.trim()) : NaN);
  const k = entero(kcal);
  const p = entero(prot);
  const c = entero(carb);
  const g = entero(gras);
  const errKcal = !(k >= 500 && k <= 10000) ? 'Entre 500 y 10.000 kcal.' : null;
  const errMacro = (v: number) => (!(v >= 0 && v <= 1000) ? 'Entre 0 y 1.000 g.' : null);
  const errDesde = !desde ? 'Elige una fecha.' : null;
  const sumaMacros = [p, c, g].every((v) => v >= 0) ? Math.round(p * 4 + c * 4 + g * 9) : null;

  const guardar = (ev: FormEvent) => {
    ev.preventDefault();
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
      confirmarDescarte
      width={880}
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
      <form id="fus-form-objetivo" className="pl-form" onSubmit={guardar} noValidate>
        {errorEnvio && <Alert tone="danger">{errorEnvio}</Alert>}
        <div className="pl-form__cols">
          <section className="pl-form__sec">
            <h3 className="pl-form__titulo">Calcular con tu perfil</h3>
            <div className="fus-calculo">
              <div className="fus-calculo__fila">
                <SegmentedControl value={tipo} onChange={(v) => setTipo(v as TipoObjetivo)} options={(Object.keys(ETIQUETA_TIPO) as TipoObjetivo[]).map((t) => ({ value: t, label: ETIQUETA_TIPO[t] }))} />
                <Button type="button" size="sm" variant="secondary" icon="sparkles" loading={calcular.isPending} onClick={proponer}>
                  Calcular
                </Button>
              </div>
              {calcular.isError ? (
                <p className="muted" style={{ margin: 0, fontSize: 12.5 }}>
                  {mensajeErrorCalculo(calcular.error)} <Link to="/perfil">Ir a Perfil</Link>
                </p>
              ) : calculo ? (
                <p className="muted" style={{ margin: 0, fontSize: 12.5 }}>
                  Gasto en reposo {num(calculo.tmb)} kcal; con {ETIQUETA_ACTIVIDAD[calculo.nivelActividad]}, {num(calculo.gastoTotal)} kcal al día
                  {calculo.tipo === 'DEFINICION' ? ', menos 500 para perder grasa' : calculo.tipo === 'VOLUMEN' ? ', más 300 para ganar músculo' : ''}. Con {num(calculo.pesoKg, 1)} kg,
                  {' '}{calculo.edad} años y {calculo.alturaCm} cm. Proteínas a 2 g por kilo y grasas al 25 %.
                </p>
              ) : (
                <p className="muted" style={{ margin: 0, fontSize: 12.5 }}>Usa tu altura, edad, sexo y actividad de Perfil y tu último peso. Rellena los campos de abajo, que puedes retocar.</p>
              )}
            </div>
          </section>
          <section className="pl-form__sec">
            <h3 className="pl-form__titulo">Objetivo</h3>
            <div className="fus-form__row">
              <Input label="Calorías al día" inputMode="numeric" autoFocus value={kcal} onChange={(e) => setKcal(e.target.value)} error={errKcal} validarAlSalir />
              <Input label="Vigente desde" type="date" value={desde} onChange={(e) => setDesde(e.target.value)} error={errDesde} validarAlSalir />
            </div>
            <div className="fus-form__row4" style={{ gridTemplateColumns: 'repeat(3,minmax(0,1fr))' }}>
              <Input label="Proteínas (g)" inputMode="numeric" value={prot} onChange={(e) => setProt(e.target.value)} error={errMacro(p)} validarAlSalir />
              <Input label="Carbohidratos (g)" inputMode="numeric" value={carb} onChange={(e) => setCarb(e.target.value)} error={errMacro(c)} validarAlSalir />
              <Input label="Grasas (g)" inputMode="numeric" value={gras} onChange={(e) => setGras(e.target.value)} error={errMacro(g)} validarAlSalir />
            </div>
            {sumaMacros !== null && (
              <p className="muted" style={{ margin: 0, fontSize: 12.5 }}>
                Esos macros suman unas {num(sumaMacros)} kcal.
              </p>
            )}
            <p className="muted" style={{ margin: 0, fontSize: 12.5 }}>
              Se guarda como un objetivo nuevo desde esa fecha; el histórico no se modifica.
            </p>
          </section>
        </div>
      </form>
    </Dialog>
  );
}
