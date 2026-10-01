import { useState, type FormEvent } from 'react';
import { mensajeError, useApuntarPeso } from '../../api/atlas';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Button, Dialog, Input } from '../../design-system';
import { iso } from '../../lib/fechas';

const PESO = /^\d{1,3}([.,]\d{1,2})?$/;
const GRASA = /^\d{1,3}([.,]\d)?$/;
const dec = (s: string) => Number(s.trim().replace(',', '.'));

export function FormularioPeso({ onClose }: { onClose: () => void }) {
  useRestaurarFoco();
  const hoy = iso(new Date());
  const apuntar = useApuntarPeso();
  const [fecha, setFecha] = useState(hoy);
  const [peso, setPeso] = useState('');
  const [grasa, setGrasa] = useState('');
  const [intentado, setIntentado] = useState(false);
  const [errorEnvio, setErrorEnvio] = useState<string | null>(null);

  const errFecha = !fecha ? 'Elige una fecha.' : fecha > hoy ? 'No puede ser una fecha futura.' : null;
  const errPeso = !PESO.test(peso.trim()) || dec(peso) <= 0 ? 'Entre 0,01 y 999,99 kg.' : null;
  const errGrasa = grasa.trim() === '' || (GRASA.test(grasa.trim()) && dec(grasa) <= 100) ? null : 'De 0 a 100, con un decimal.';
  const ver = (e: string | null) => (intentado ? e : null);

  const guardar = (ev: FormEvent) => {
    ev.preventDefault();
    setIntentado(true);
    setErrorEnvio(null);
    if (errFecha || errPeso || errGrasa) return;
    apuntar.mutate(
      { fecha, pesoKg: dec(peso), grasaPct: grasa.trim() === '' ? null : dec(grasa), notas: null },
      { onSuccess: onClose, onError: (e) => setErrorEnvio(mensajeError(e)) },
    );
  };

  return (
    <Dialog
      open
      onClose={onClose}
      width={420}
      title="Apuntar peso"
      footer={
        <>
          <Button variant="ghost" type="button" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" form="atl-form-peso" loading={apuntar.isPending}>
            Guardar
          </Button>
        </>
      }
    >
      <form id="atl-form-peso" className="atl-form" onSubmit={guardar} noValidate>
        {errorEnvio && <Alert tone="danger">{errorEnvio}</Alert>}
        <Input label="Fecha" type="date" max={hoy} value={fecha} onChange={(e) => setFecha(e.target.value)} error={ver(errFecha)} hint="Un peso por día: si ya hay uno, se reemplaza." />
        <div className="atl-form__row2">
          <Input label="Peso" autoFocus inputMode="decimal" trailing={<span className="muted" style={{ fontSize: 12 }}>kg</span>} value={peso} onChange={(e) => setPeso(e.target.value)} error={ver(errPeso)} />
          <Input label="Grasa" hint="Opcional" inputMode="decimal" trailing={<span className="muted" style={{ fontSize: 12 }}>%</span>} value={grasa} onChange={(e) => setGrasa(e.target.value)} error={ver(errGrasa)} />
        </div>
      </form>
    </Dialog>
  );
}
