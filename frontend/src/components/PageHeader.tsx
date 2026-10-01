import type { ReactNode } from 'react';
import { Eyebrow } from '../design-system';

interface Props {
  eyebrow: string;
  coord?: string;
  title: ReactNode;
  badge?: ReactNode;
  actions?: ReactNode;
}

export function PageHeader({ eyebrow, coord, title, badge, actions }: Props) {
  return (
    <header className="ph">
      <div className="ph__t">
        <div className="pl-rise" style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
          <Eyebrow star coord={coord}>
            {eyebrow}
          </Eyebrow>
          {badge}
        </div>
        <h1 className="ph__h pl-rise" style={{ animationDelay: '60ms' }}>
          {title}
        </h1>
      </div>
      {actions && (
        <div className="ph__a pl-rise" style={{ animationDelay: '120ms' }}>
          {actions}
        </div>
      )}
    </header>
  );
}
