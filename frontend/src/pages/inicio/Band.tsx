import type { ReactNode } from 'react';
import { Icon } from '../../design-system';

interface Props {
  modulo: string;
  icon: string;
  name: string;
  sub: string;
  onClick?: () => void;
  aside?: ReactNode;
  children: ReactNode;
}

// Una fila del panel de modulos del Inicio. El data-module re-tine el acento de la fila.
export function Band({ modulo, icon, name, sub, onClick, aside, children }: Props) {
  return (
    <div
      className="band"
      data-module={modulo}
      onClick={onClick}
      onKeyDown={onClick ? (e) => (e.key === 'Enter' || e.key === ' ') && onClick() : undefined}
      role={onClick ? 'link' : undefined}
      tabIndex={onClick ? 0 : undefined}
      style={{ cursor: onClick ? 'pointer' : 'default' }}
    >
      <div className="band__id">
        <span className="band__ic">
          <Icon name={icon} size={18} />
        </span>
        <div className="stack-4" style={{ minWidth: 0 }}>
          <b className="band__name">
            {name}
            {onClick && <Icon name="arrow-up-right" size={14} className="band__go" />}
          </b>
          <span className="pl-eyebrow">{sub}</span>
        </div>
      </div>
      <div className="band__main">{children}</div>
      <div className="band__aside">{aside}</div>
    </div>
  );
}

interface Estado {
  cargando: boolean;
  error: boolean;
}

/** Texto discreto mientras carga o si la consulta falla; null si ya hay datos. */
export function EstadoConsulta({ cargando, error }: Estado) {
  if (cargando) return <span className="muted">Cargando…</span>;
  if (error) return <span className="muted">No se han podido cargar los datos.</span>;
  return null;
}
