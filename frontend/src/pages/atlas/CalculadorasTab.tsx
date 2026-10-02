import { useState } from 'react';
import { Card, Input, SegmentedControl } from '../../design-system';
import { num } from '../../lib/fechas';

const DEC = /^\d{1,4}([.,]\d{1,2})?$/;
const dec = (s: string) => Number(s.trim().replace(',', '.'));
const valido = (s: string) => DEC.test(s.trim()) && dec(s) > 0;

const DISCOS = [25, 20, 15, 10, 5, 2.5, 1.25];
const PORCENTAJES = [100, 95, 90, 85, 80, 75, 70, 65, 60];

/**
 * 1RM estimado: media de Epley y Brzycki, que se parecen hasta unas 10 reps.
 * Con mas de 12 reps la estimacion ya no es fiable y no se da.
 */
export function unaRm(peso: number, reps: number): number | null {
  if (reps < 1 || reps > 12) return null;
  if (reps === 1) return peso;
  const epley = peso * (1 + reps / 30);
  const brzycki = (peso * 36) / (37 - reps);
  return (epley + brzycki) / 2;
}

/** Discos por lado para llegar al total con esa barra; `resto` es lo que no se puede cargar. */
export function discosPorLado(total: number, barra: number): { discos: number[]; resto: number } {
  let lado = (total - barra) / 2;
  const discos: number[] = [];
  for (const d of DISCOS) {
    while (lado >= d - 1e-9) {
      discos.push(d);
      lado -= d;
    }
  }
  return { discos, resto: Math.max(0, Math.round(lado * 2 * 100) / 100) };
}

function UnaRm() {
  const [peso, setPeso] = useState('');
  const [reps, setReps] = useState('');
  const repsN = Number(reps);
  const errPeso = peso.trim() === '' || valido(peso) ? null : 'Un peso en kg.';
  const errReps = reps.trim() === '' || (/^\d{1,2}$/.test(reps.trim()) && repsN >= 1 && repsN <= 12) ? null : 'De 1 a 12 reps.';
  const rm = !errPeso && !errReps && peso && reps ? unaRm(dec(peso), repsN) : null;

  return (
    <Card delay={60} eyebrow="Fuerza" title="1RM estimado">
      <div className="atl-form">
        <div className="atl-form__row2">
          <Input label="Peso levantado" inputMode="decimal" trailing={<span className="muted" style={{ fontSize: 12 }}>kg</span>} value={peso} onChange={(e) => setPeso(e.target.value)} error={errPeso} />
          <Input label="Repeticiones" inputMode="numeric" value={reps} onChange={(e) => setReps(e.target.value)} error={errReps} />
        </div>
        {rm === null ? (
          <p className="muted" style={{ margin: 0 }}>Pon el peso y las repeticiones de una serie hecha al fallo o casi.</p>
        ) : (
          <>
            <div className="atl-calc__res">
              <span className="muted">Tu 1RM rondará</span>
              <b>{num(rm, 1)} kg</b>
            </div>
            <div className="atl-calc__tabla">
              {PORCENTAJES.map((p) => (
                <div key={p} className="atl-calc__f">
                  <span className="pl-row__num">{p} %</span>
                  <span className="money">{num(Math.round((rm * p) / 100 / 1.25) * 1.25, 2)} kg</span>
                </div>
              ))}
            </div>
            <p className="muted" style={{ margin: 0, fontSize: 12 }}>Media de las fórmulas de Epley y Brzycki, redondeada a 1,25 kg.</p>
          </>
        )}
      </div>
    </Card>
  );
}

function Discos() {
  const [total, setTotal] = useState('');
  const [barra, setBarra] = useState('20');
  const barraN = Number(barra);
  const errTotal = total.trim() === '' ? null : !valido(total) ? 'Un peso en kg.' : dec(total) < barraN ? `Como mínimo la barra, ${barraN} kg.` : null;
  const calculo = total.trim() !== '' && !errTotal ? discosPorLado(dec(total), barraN) : null;

  return (
    <Card delay={120} eyebrow="Carga" title="Discos por lado">
      <div className="atl-form">
        <div className="atl-form__row2">
          <Input label="Peso total" inputMode="decimal" trailing={<span className="muted" style={{ fontSize: 12 }}>kg</span>} value={total} onChange={(e) => setTotal(e.target.value)} error={errTotal} />
          <div className="pl-field">
            <span className="pl-field__label">Barra</span>
            <SegmentedControl
              value={barra}
              onChange={(v) => setBarra(String(v))}
              options={[
                { value: '20', label: '20 kg' },
                { value: '15', label: '15 kg' },
                { value: '10', label: '10 kg' },
              ]}
            />
          </div>
        </div>
        {calculo === null ? (
          <p className="muted" style={{ margin: 0 }}>Discos de 25, 20, 15, 10, 5, 2,5 y 1,25 kg.</p>
        ) : calculo.discos.length === 0 ? (
          <p className="muted" style={{ margin: 0 }}>Solo la barra.</p>
        ) : (
          <>
            <div className="atl-discos" aria-label={`Por lado: ${calculo.discos.map((d) => num(d, d % 1 ? 2 : 0)).join(', ')} kg`}>
              {calculo.discos.map((d, i) => (
                <span key={i} className="atl-disco" style={{ height: 34 + d * 2.6 }}>
                  {num(d, d % 1 ? 2 : 0)}
                </span>
              ))}
            </div>
            {calculo.resto > 0 && <p className="muted" style={{ margin: 0, fontSize: 12 }}>Sobran {num(calculo.resto, 2)} kg que no se pueden cargar con estos discos.</p>}
          </>
        )}
      </div>
    </Card>
  );
}

/** Calculadoras de FitCore que Atlas no tenia: 1RM y discos por lado. Sin backend. */
export function CalculadorasTab() {
  return (
    <div className="grid">
      <div className="span-6">
        <UnaRm />
      </div>
      <div className="span-6">
        <Discos />
      </div>
    </div>
  );
}
