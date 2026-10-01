import React from 'react';
import { Icon } from './Icon.jsx';
const T = { PELICULA: ['Película', 'clapperboard'], SERIE: ['Serie', 'tv'], JUEGO: ['Juego', 'gamepad-2'], LIBRO: ['Libro', 'book-open'] };
export function TypeTag({ tipo = 'PELICULA', showLabel = true, size = 14 }) {
  const [label, icon] = T[tipo];
  return <span className="pl-type" title={label}><Icon name={icon} size={size} />{showLabel && label}</span>;
}
