import type { GastoCategoria } from '../../api/kuiper';
import { pctTexto } from '../../api/kuiperAlertas';
import { Alert, Button } from '../../design-system';

interface Props {
  filas: GastoCategoria[];
  onIrACategorias: () => void;
}

function plural(n: number, uno: string, varios: string) {
  return `${n} ${n === 1 ? uno : varios}`;
}

/** Banner con las categorias que se han pasado o rozan su presupuesto mensual. Nada si todo va bien. */
export function AvisoPresupuestos({ filas, onIrACategorias }: Props) {
  const excedidas = filas.filter((f) => f.estado === 'EXCEDIDO');
  const enAviso = filas.filter((f) => f.estado === 'AVISO');
  if (excedidas.length === 0 && enAviso.length === 0) return null;

  const partes = [
    excedidas.length > 0 ? plural(excedidas.length, 'categoría excedida', 'categorías excedidas') : null,
    enAviso.length > 0 ? plural(enAviso.length, 'en aviso', 'en aviso') : null,
  ].filter(Boolean);

  return (
    <Alert
      tone={excedidas.length > 0 ? 'danger' : 'warning'}
      title={partes.join(' · ')}
      action={
        <Button size="sm" variant="secondary" onClick={onIrACategorias}>
          Ajustar
        </Button>
      }
    >
      <ul className="kui-avisos">
        {[...excedidas, ...enAviso].map((f) => (
          <li key={f.categoriaId} className={`kui-aviso kui-aviso--${f.estado.toLowerCase()}`}>
            <b>{f.categoriaNombre}</b> {pctTexto(f.porcentaje)}
          </li>
        ))}
      </ul>
    </Alert>
  );
}
