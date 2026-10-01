import React from 'react';
export function Tooltip({ label, children, placement = 'top', wrap }) {
  return (
    <span className={'pl-tip' + (placement === 'bottom' ? ' pl-tip--bottom' : '')}>
      {children}
      <span role="tooltip" className={'pl-tip__bubble' + (wrap ? ' pl-tip__bubble--wrap' : '')}>{label}</span>
    </span>
  );
}
