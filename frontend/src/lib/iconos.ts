// Iconos Lucide disponibles en public/icons. Un icono que no existe aqui se pinta como un
// hueco (la mascara CSS no carga), asi que los nombres que vienen de la API se validan.
const DISPONIBLES = new Set([
  'arrow-down-right', 'arrow-left', 'arrow-right', 'arrow-up-right', 'book-open', 'calendar', 'check',
  'chevron-down', 'chevron-left', 'chevron-right', 'chevrons-up-down', 'circle-alert', 'circle-check',
  'clapperboard', 'compass', 'dumbbell', 'flame', 'gamepad-2', 'house', 'image-up', 'info', 'key-round',
  'link-2-off', 'link-2', 'lock', 'log-out', 'mail-warning', 'mail', 'minus', 'orbit', 'play', 'plus',
  'repeat', 'scale', 'search', 'send', 'shopping-basket', 'sparkles', 'star', 'ticket', 'train-front',
  'trophy', 'tv', 'unlink', 'user-round', 'utensils', 'wallet', 'x',
]);

/** El icono pedido si existe en local; si no, el de reserva. */
export function iconoOr(nombre: string | null | undefined, reserva = 'wallet'): string {
  return nombre && DISPONIBLES.has(nombre) ? nombre : reserva;
}
