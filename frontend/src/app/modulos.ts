export type ModuloId = 'polaris' | 'odisea' | 'kuiper' | 'fusion' | 'atlas' | 'nucleo';

export interface ModuloNav {
  id: string;
  ruta: string;
  label: string;
  icon: string;
  color: string;
  modulo: ModuloId;
}

// Orden y colores tal cual el design system (design/readme.md). La ruta de Inicio es "/".
export const NAV: ModuloNav[] = [
  { id: 'inicio', ruta: '/', label: 'Inicio', icon: 'compass', color: 'var(--mod-polaris)', modulo: 'polaris' },
  { id: 'odisea', ruta: '/odisea', label: 'Odisea', icon: 'clapperboard', color: 'var(--mod-odisea)', modulo: 'odisea' },
  { id: 'kuiper', ruta: '/kuiper', label: 'Kuiper', icon: 'wallet', color: 'var(--mod-kuiper)', modulo: 'kuiper' },
  { id: 'fusion', ruta: '/fusion', label: 'Fusión', icon: 'flame', color: 'var(--mod-fusion)', modulo: 'fusion' },
  { id: 'atlas', ruta: '/atlas', label: 'Atlas', icon: 'dumbbell', color: 'var(--mod-atlas)', modulo: 'atlas' },
];
