// Dibujo del mapa muscular (frente y espalda) en un espacio de 200 x 440, dibujo propio.
// Cada musculo es una lista de puntos de la mitad derecha de la imagen (x >= 100):
// el componente la refleja para pintar el otro lado y suaviza los puntos en curvas. Solo datos: el dibujo y el color estan en MapaMuscular.tsx.

export type Musculo =
  | 'pectoral' | 'deltoides' | 'trapecio' | 'biceps' | 'triceps' | 'antebrazo'
  | 'abdominales' | 'oblicuos' | 'serrato' | 'dorsal' | 'redondo' | 'lumbar'
  | 'gluteo' | 'gluteoMedio' | 'cuadriceps' | 'aductores' | 'isquios' | 'gemelos' | 'soleo' | 'tibial';

export interface FormaMusculo {
  musculo: Musculo;
  /** Pares x,y seguidos, en el sentido de las agujas del reloj. */
  puntos: number[];
}

export const NOMBRE_MUSCULO: Record<Musculo, string> = {
  pectoral: 'Pectoral', deltoides: 'Deltoides', trapecio: 'Trapecio', biceps: 'Bíceps', triceps: 'Tríceps',
  antebrazo: 'Antebrazo', abdominales: 'Abdominales', oblicuos: 'Oblicuos', serrato: 'Serrato',
  dorsal: 'Dorsal ancho', redondo: 'Redondo e infraespinoso', lumbar: 'Lumbares', gluteo: 'Glúteo mayor',
  gluteoMedio: 'Glúteo medio', cuadriceps: 'Cuádriceps', aductores: 'Aductores', isquios: 'Isquiotibiales',
  gemelos: 'Gemelos', soleo: 'Sóleo', tibial: 'Tibial anterior',
};

/** Contorno de media figura, de la coronilla a la entrepierna por fuera. Igual de frente y de espalda. */
export const SILUETA: number[] = [
  100, 5, 111, 7, 118, 16, 119, 30, 116, 42, 110, 50, 109, 58, 122, 63, 140, 67, 155, 71, 164, 82,
  167, 100, 170, 124, 173, 146, 180, 168, 186, 192, 189, 210, 193, 222, 193, 236, 187, 246, 179, 245,
  175, 232, 173, 216, 165, 192, 157, 168, 151, 152, 146, 132, 141, 116, 139, 140, 135, 168, 137, 190,
  143, 210, 146, 238, 144, 268, 139, 298, 136, 318, 139, 340, 138, 366, 132, 394, 131, 412, 140, 426,
  138, 435, 118, 436, 114, 422, 115, 404, 111, 380, 107, 350, 108, 326, 109, 310, 106, 282, 103, 252,
  101, 230, 100, 228,
];

/** Partes que no son musculo (manos, rodillas, pies): se pintan neutras. */
export const HUESOS_FRENTE: number[][] = [
  [173, 216, 189, 212, 193, 228, 191, 242, 182, 246, 176, 236],
  [111, 312, 122, 308, 133, 312, 133, 324, 122, 328, 111, 324],
  [116, 404, 130, 404, 132, 414, 139, 428, 136, 434, 119, 434, 114, 420],
];
export const HUESOS_ESPALDA: number[][] = [
  [173, 216, 189, 212, 193, 228, 191, 242, 182, 246, 176, 236],
  [116, 396, 130, 396, 132, 414, 139, 428, 136, 434, 119, 434, 114, 420],
];

export const FRENTE: FormaMusculo[] = [
  // Cuello y trapecio superior, la parte que se ve de frente.
  { musculo: 'trapecio', puntos: [110, 56, 124, 63, 140, 68, 128, 70, 113, 66] },
  { musculo: 'deltoides', puntos: [141, 69, 154, 72, 163, 83, 165, 100, 161, 112, 153, 104, 146, 92, 140, 78] },
  { musculo: 'pectoral', puntos: [102, 74, 116, 71, 134, 73, 144, 82, 147, 96, 141, 106, 127, 114, 110, 118, 102, 114] },
  { musculo: 'biceps', puntos: [147, 104, 154, 108, 161, 118, 165, 136, 162, 148, 155, 150, 150, 138, 147, 120] },
  { musculo: 'triceps', puntos: [162, 112, 166, 118, 169, 134, 168, 146, 165, 146, 165, 132] },
  { musculo: 'antebrazo', puntos: [152, 154, 162, 150, 170, 150, 177, 166, 183, 190, 186, 208, 175, 212, 166, 192, 158, 172] },
  { musculo: 'serrato', puntos: [138, 112, 143, 118, 141, 132, 137, 138, 135, 126] },
  { musculo: 'oblicuos', puntos: [124, 122, 136, 136, 135, 160, 134, 176, 137, 192, 130, 200, 122, 194, 121, 160, 121, 132] },
  // Recto abdominal: cuatro bloques por lado, el ultimo hasta el pubis.
  { musculo: 'abdominales', puntos: [102, 120, 118, 121, 119, 138, 102, 138] },
  { musculo: 'abdominales', puntos: [102, 142, 119, 142, 119, 160, 102, 160] },
  { musculo: 'abdominales', puntos: [102, 164, 119, 164, 119, 182, 102, 182] },
  { musculo: 'abdominales', puntos: [102, 186, 119, 186, 118, 204, 110, 216, 102, 220] },
  { musculo: 'gluteoMedio', puntos: [134, 198, 141, 204, 145, 222, 141, 236, 136, 220] },
  { musculo: 'aductores', puntos: [103, 228, 112, 218, 120, 222, 118, 246, 112, 266, 106, 262, 103, 244] },
  // Cuadriceps: vasto lateral, recto femoral y vasto medial.
  { musculo: 'cuadriceps', puntos: [138, 226, 144, 244, 143, 270, 138, 296, 133, 308, 129, 290, 131, 258, 135, 236] },
  { musculo: 'cuadriceps', puntos: [123, 214, 132, 222, 133, 250, 129, 282, 124, 300, 119, 284, 117, 252, 118, 228] },
  { musculo: 'cuadriceps', puntos: [113, 270, 118, 280, 122, 296, 121, 308, 112, 310, 108, 296, 109, 280] },
  { musculo: 'gemelos', puntos: [109, 330, 115, 334, 116, 356, 113, 376, 108, 368, 107, 348] },
  { musculo: 'gemelos', puntos: [133, 330, 137, 340, 137, 360, 133, 376, 129, 360, 130, 342] },
  { musculo: 'tibial', puntos: [121, 332, 127, 334, 128, 356, 125, 384, 121, 398, 118, 378, 118, 352] },
];

export const ESPALDA: FormaMusculo[] = [
  // Trapecio entero: del occipital al hombro y en punta hasta la mitad de la espalda.
  { musculo: 'trapecio', puntos: [101, 46, 109, 52, 112, 60, 126, 65, 141, 69, 132, 78, 120, 92, 112, 116, 104, 136, 101, 130] },
  { musculo: 'deltoides', puntos: [142, 69, 154, 72, 163, 83, 165, 100, 161, 112, 152, 102, 144, 88, 135, 78] },
  { musculo: 'redondo', puntos: [120, 94, 132, 80, 142, 86, 147, 100, 143, 112, 130, 112, 122, 106] },
  { musculo: 'dorsal', puntos: [114, 118, 124, 110, 142, 114, 140, 136, 136, 160, 134, 178, 120, 188, 108, 194, 105, 172, 106, 142] },
  { musculo: 'lumbar', puntos: [101, 140, 106, 146, 107, 176, 106, 198, 101, 204] },
  { musculo: 'triceps', puntos: [147, 104, 157, 104, 164, 114, 168, 132, 167, 146, 158, 150, 151, 138, 147, 120] },
  { musculo: 'antebrazo', puntos: [152, 154, 162, 150, 170, 150, 177, 166, 183, 190, 186, 208, 175, 212, 166, 192, 158, 172] },
  { musculo: 'gluteoMedio', puntos: [112, 198, 124, 190, 136, 192, 142, 204, 136, 212, 120, 206] },
  { musculo: 'gluteo', puntos: [102, 208, 118, 208, 136, 214, 144, 228, 143, 248, 130, 258, 114, 258, 102, 252] },
  { musculo: 'aductores', puntos: [102, 258, 108, 262, 108, 288, 105, 296, 103, 280] },
  // Isquios: biceps femoral por fuera, semitendinoso por dentro.
  { musculo: 'isquios', puntos: [126, 262, 140, 258, 142, 276, 137, 300, 131, 314, 125, 300, 124, 280] },
  { musculo: 'isquios', puntos: [110, 262, 121, 262, 122, 284, 120, 304, 114, 314, 110, 300, 109, 280] },
  { musculo: 'gemelos', puntos: [109, 326, 119, 322, 121, 340, 120, 364, 113, 372, 108, 356, 107, 338] },
  { musculo: 'gemelos', puntos: [123, 322, 133, 326, 138, 340, 136, 360, 129, 368, 123, 360, 122, 340] },
  { musculo: 'soleo', puntos: [112, 374, 120, 368, 128, 370, 135, 370, 132, 386, 126, 396, 118, 394] },
];
