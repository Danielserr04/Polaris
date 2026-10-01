export type Estado = 'PENDIENTE' | 'EN_CURSO' | 'TERMINADO' | 'ABANDONADO';
export interface StatusBadgeProps {
  /** Odisea entry state, exactly as returned by the API */
  estado?: Estado;
  /** badge = boxed mono label · dot = glyph + plain text for dense lists */
  variant?: 'badge' | 'dot';
}
export declare function StatusBadge(props: StatusBadgeProps): JSX.Element;
export interface StateGlyphProps { estado?: Estado; size?: number; }
export declare function StateGlyph(props: StateGlyphProps): JSX.Element;
