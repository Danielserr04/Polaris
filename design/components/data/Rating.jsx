import React from 'react';
const STAR = 'polygon(50% 0%,61.8% 35.4%,100% 38.2%,69.1% 61.8%,80.9% 100%,50% 76.4%,19.1% 100%,30.9% 61.8%,0% 38.2%,38.2% 35.4%)';
export function Rating({ value, onChange, size = 14, showValue = true }) {
  const [hover, setHover] = React.useState(null);
  const v = hover ?? value ?? 0;
  return (
    <span className={'pl-rating' + (onChange ? ' pl-rating--interactive' : '')} onMouseLeave={() => setHover(null)}>
      <span className="pl-rating__stars">
        {[0, 1, 2, 3, 4].map(i => {
          const fill = Math.max(0, Math.min(1, (v - i * 2) / 2));
          return (
            <span key={i} className="pl-rating__star" style={{ width: size, height: size, clipPath: STAR, background: 'var(--surface-3)' }}
              onMouseMove={onChange ? e => { const r = e.currentTarget.getBoundingClientRect(); setHover(i * 2 + (e.clientX - r.left < r.width / 2 ? 1 : 2)); } : undefined}
              onClick={onChange ? () => onChange(hover) : undefined}>
              <span className="pl-rating__fill" style={{ width: fill * 100 + '%', background: 'var(--accent)' }} />
            </span>
          );
        })}
      </span>
      {showValue && <span className="pl-rating__num">{value != null ? value : '–'}/10</span>}
    </span>
  );
}
