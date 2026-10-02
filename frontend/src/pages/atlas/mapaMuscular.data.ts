// Dibujo del mapa muscular (frente y espalda) en un espacio de 200 x 440, dibujo propio.
// Cada musculo es una lista de puntos de la mitad derecha de la imagen (x >= 100):
// el componente la refleja para pintar el otro lado y suaviza los puntos en curvas. Solo datos: el dibujo y el color estan en MapaMuscular.tsx.

export type Musculo =
  | 'pectoral' | 'deltoides' | 'trapecio' | 'biceps' | 'triceps' | 'antebrazo'
  | 'braquial' | 'cuello' | 'abdominales' | 'oblicuos' | 'serrato' | 'dorsal' | 'redondo' | 'lumbar'
  | 'gluteo' | 'gluteoMedio' | 'tensor' | 'cuadriceps' | 'sartorio' | 'aductores' | 'isquios'
  | 'gemelos' | 'soleo' | 'tibial';

export interface FormaMusculo {
  musculo: Musculo;
  /** Pares x,y seguidos, en el sentido de las agujas del reloj. */
  puntos: number[];
}

export const NOMBRE_MUSCULO: Record<Musculo, string> = {
  pectoral: 'Pectoral', deltoides: 'Deltoides', trapecio: 'Trapecio', biceps: 'Bíceps', triceps: 'Tríceps',
  antebrazo: 'Antebrazo', braquial: 'Braquial', cuello: 'Esternocleidomastoideo', abdominales: 'Abdominales', oblicuos: 'Oblicuos', serrato: 'Serrato',
  dorsal: 'Dorsal ancho', redondo: 'Redondo e infraespinoso', lumbar: 'Lumbares', gluteo: 'Glúteo mayor',
  gluteoMedio: 'Glúteo medio', tensor: 'Tensor de la fascia lata', cuadriceps: 'Cuádriceps', sartorio: 'Sartorio', aductores: 'Aductores', isquios: 'Isquiotibiales',
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

// El orden importa: lo que va despues se pinta encima (el deltoides tapa la punta del pectoral,
// el sartorio cruza el cuadriceps, los gemelos tapan el centro del soleo).
export const FRENTE: FormaMusculo[] = [
  // Trapecio superior, la parte que se ve de frente, y esternocleidomastoideo (de detras de la oreja al esternon).
  { musculo: 'trapecio', puntos: [110, 56, 124, 63, 140, 68, 128, 70, 113, 66] },
  { musculo: 'cuello', puntos: [111, 40, 114, 45, 108, 58, 104, 70, 101, 70, 104, 56] },
  // Pectoral en abanico: del esternon y la clavicula converge hacia el brazo, bajo el deltoides.
  { musculo: 'pectoral', puntos: [102, 74, 118, 70, 134, 72, 144, 80, 151, 93, 146, 100, 134, 109, 119, 117, 106, 118, 102, 112] },
  { musculo: 'deltoides', puntos: [140, 69, 154, 72, 163, 83, 165, 100, 162, 114, 155, 106, 147, 92, 139, 79] },
  { musculo: 'biceps', puntos: [149, 106, 155, 108, 161, 118, 163, 134, 160, 146, 154, 148, 151, 136, 149, 120] },
  // Triceps: de frente solo asoma la cabeza larga por dentro del brazo; por fuera, el braquial.
  { musculo: 'triceps', puntos: [143, 114, 147, 116, 149, 132, 150, 146, 147, 142, 144, 128] },
  { musculo: 'braquial', puntos: [162, 116, 166, 122, 168, 138, 166, 148, 162, 148, 164, 134] },
  // Antebrazo: braquiorradial por fuera, flexores por dentro.
  { musculo: 'antebrazo', puntos: [161, 144, 168, 146, 176, 164, 184, 190, 187, 207, 182, 207, 173, 182, 163, 160] },
  { musculo: 'antebrazo', puntos: [150, 151, 158, 150, 164, 162, 173, 186, 179, 207, 174, 211, 163, 190, 154, 170] },
  // Dentadura del serrato entre pectoral y dorsal, y el borde del dorsal que asoma por el costado.
  { musculo: 'dorsal', puntos: [140, 106, 143, 112, 141, 132, 137, 146, 136, 126] },
  { musculo: 'serrato', puntos: [133, 112, 139, 114, 136, 120] },
  { musculo: 'serrato', puntos: [132, 121, 139, 122, 135, 128] },
  { musculo: 'serrato', puntos: [131, 129, 138, 131, 134, 136] },
  { musculo: 'oblicuos', puntos: [122, 124, 130, 128, 136, 138, 135, 160, 134, 176, 137, 192, 130, 200, 122, 194, 121, 160, 121, 132] },
  // Recto abdominal: cuatro bloques por lado, el ultimo hasta el pubis.
  { musculo: 'abdominales', puntos: [102, 120, 118, 121, 119, 138, 102, 138] },
  { musculo: 'abdominales', puntos: [102, 142, 119, 142, 119, 160, 102, 160] },
  { musculo: 'abdominales', puntos: [102, 164, 119, 164, 119, 182, 102, 182] },
  { musculo: 'abdominales', puntos: [102, 186, 119, 186, 118, 204, 110, 216, 102, 220] },
  // Cadera por fuera: gluteo medio arriba y tensor de la fascia lata debajo.
  { musculo: 'gluteoMedio', puntos: [134, 192, 140, 198, 142, 206, 137, 204] },
  { musculo: 'tensor', puntos: [136, 204, 142, 208, 146, 222, 143, 236, 139, 230, 136, 216] },
  { musculo: 'aductores', puntos: [103, 228, 113, 220, 120, 224, 116, 250, 110, 276, 106, 282, 104, 262] },
  // Cuadriceps: vasto lateral, recto femoral y vasto medial.
  { musculo: 'cuadriceps', puntos: [139, 230, 144, 246, 143, 270, 138, 296, 133, 310, 129, 290, 131, 258, 135, 240] },
  { musculo: 'cuadriceps', puntos: [125, 216, 133, 224, 134, 250, 130, 282, 124, 302, 119, 284, 118, 252, 120, 230] },
  { musculo: 'cuadriceps', puntos: [113, 272, 118, 280, 122, 296, 121, 310, 112, 312, 108, 298, 109, 282] },
  // Sartorio: de la espina iliaca a la cara interna de la rodilla, cruzando el muslo en diagonal.
  { musculo: 'sartorio', puntos: [136, 202, 139, 207, 127, 246, 116, 286, 111, 310, 107, 306, 111, 282, 123, 240] },
  { musculo: 'soleo', puntos: [108, 368, 112, 372, 114, 394, 111, 396, 108, 382] },
  { musculo: 'soleo', puntos: [134, 368, 136, 374, 133, 392, 131, 390] },
  { musculo: 'gemelos', puntos: [109, 330, 115, 334, 116, 352, 113, 372, 108, 364, 107, 346] },
  { musculo: 'gemelos', puntos: [133, 330, 137, 340, 137, 356, 134, 370, 130, 356, 130, 342] },
  { musculo: 'tibial', puntos: [121, 332, 127, 334, 128, 356, 125, 384, 121, 398, 118, 378, 118, 352] },
];

export const ESPALDA: FormaMusculo[] = [
  // Dorsal: de la columna baja (fascia toracolumbar) sube en abanico hasta la axila.
  { musculo: 'dorsal', puntos: [108, 132, 116, 118, 128, 110, 140, 106, 147, 108, 143, 122, 139, 144, 135, 168, 126, 184, 112, 194, 108, 180] },
  { musculo: 'lumbar', puntos: [101, 138, 106, 144, 108, 176, 107, 198, 101, 206] },
  // Trapecio entero: del occipital al hombro y en punta hasta la mitad de la espalda.
  { musculo: 'trapecio', puntos: [101, 46, 109, 52, 112, 60, 126, 65, 141, 69, 132, 78, 120, 92, 112, 116, 104, 136, 101, 130] },
  { musculo: 'redondo', puntos: [120, 94, 132, 80, 142, 86, 147, 100, 143, 110, 130, 110, 122, 104] },
  { musculo: 'deltoides', puntos: [142, 69, 154, 72, 163, 83, 165, 100, 162, 114, 153, 102, 145, 88, 135, 78] },
  { musculo: 'triceps', puntos: [147, 104, 157, 104, 164, 114, 168, 132, 167, 146, 158, 150, 151, 138, 147, 120] },
  // Antebrazo: braquiorradial y extensores por fuera, flexores por dentro.
  { musculo: 'antebrazo', puntos: [161, 144, 168, 146, 176, 164, 184, 190, 187, 207, 182, 207, 173, 182, 163, 160] },
  { musculo: 'antebrazo', puntos: [150, 151, 158, 150, 164, 162, 173, 186, 179, 207, 174, 211, 163, 190, 154, 170] },
  // Gluteo medio asoma por encima y por fuera; el mayor baja en diagonal hasta el pliegue.
  { musculo: 'gluteoMedio', puntos: [114, 198, 126, 190, 138, 190, 143, 202, 141, 214, 128, 206] },
  { musculo: 'gluteo', puntos: [102, 206, 114, 200, 130, 206, 142, 218, 146, 236, 142, 250, 128, 258, 112, 258, 102, 254] },
  { musculo: 'aductores', puntos: [102, 260, 108, 262, 108, 288, 105, 296, 103, 280] },
  // Isquios: biceps femoral por fuera, semitendinoso y semimembranoso por dentro.
  { musculo: 'isquios', puntos: [126, 262, 140, 256, 142, 276, 137, 300, 131, 314, 125, 300, 124, 280] },
  { musculo: 'isquios', puntos: [110, 262, 121, 262, 122, 284, 120, 304, 114, 314, 110, 300, 109, 280] },
  // Soleo: asoma a los dos lados de los gemelos y sigue por debajo hasta el tendon de Aquiles.
  { musculo: 'soleo', puntos: [107, 344, 112, 360, 120, 368, 128, 368, 136, 356, 138, 344, 137, 362, 132, 384, 126, 396, 118, 396, 111, 384] },
  { musculo: 'gemelos', puntos: [109, 326, 119, 322, 121, 340, 120, 362, 114, 372, 109, 358, 107, 340] },
  { musculo: 'gemelos', puntos: [123, 322, 133, 326, 137, 340, 135, 356, 129, 366, 123, 360, 122, 340] },
];
