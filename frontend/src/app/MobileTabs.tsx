import { Icon } from '../design-system';
import { NAV } from './modulos';

interface Props {
  activo: string | null;
  onChange: (id: string) => void;
}

// Barra de modulos inferior del movil (design/ui_kits/polaris-mobile). En escritorio no se ve:
// el CSS la oculta por encima de 760 px y alli manda la isla de arriba.
export function MobileTabs({ activo, onChange }: Props) {
  const i = NAV.findIndex((n) => n.id === activo);
  return (
    <nav className="m-tabs" aria-label="Módulos">
      <div className="m-tabs__in">
        {i >= 0 && <span className="m-tabs__ind" data-module={NAV[i].modulo} style={{ left: `calc(${i} * (100% - 8px) / 5 + ${i * 2}px)` }} />}
        {NAV.map((n) => (
          <button key={n.id} type="button" data-module={n.modulo} className={'m-tab' + (n.id === activo ? ' on' : '')} aria-current={n.id === activo ? 'page' : undefined} onClick={() => onChange(n.id)}>
            <Icon name={n.icon} size={19} />
            <span>{n.label}</span>
          </button>
        ))}
      </div>
    </nav>
  );
}
