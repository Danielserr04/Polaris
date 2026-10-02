// Dibujo del mapa muscular (frente y espalda) en un espacio de 200 x 440, dibujo propio.
// Es un puzle: cada pieza es una lista de bordes con nombre y dos piezas vecinas usan el
// mismo borde, asi que encajan sin solaparse ni dejar huecos. Solo se dibuja la mitad
// derecha de la imagen (x >= 100); el componente la refleja y suaviza cada borde.

export type Musculo =
  | 'pectoral' | 'deltoides' | 'trapecio' | 'biceps' | 'triceps' | 'antebrazo'
  | 'braquial' | 'cuello' | 'abdominales' | 'oblicuos' | 'serrato' | 'dorsal' | 'redondo' | 'lumbar'
  | 'gluteo' | 'gluteoMedio' | 'tensor' | 'cuadriceps' | 'sartorio' | 'aductores' | 'isquios'
  | 'gemelos' | 'soleo' | 'tibial' | 'peroneos';

export const NOMBRE_MUSCULO: Record<Musculo, string> = {
  pectoral: 'Pectoral', deltoides: 'Deltoides', trapecio: 'Trapecio', biceps: 'Bíceps', triceps: 'Tríceps',
  antebrazo: 'Antebrazo', braquial: 'Braquial', cuello: 'Esternocleidomastoideo', abdominales: 'Abdominales',
  oblicuos: 'Oblicuos', serrato: 'Serrato', dorsal: 'Dorsal ancho', redondo: 'Redondo e infraespinoso',
  lumbar: 'Lumbares', gluteo: 'Glúteo mayor', gluteoMedio: 'Glúteo medio', tensor: 'Tensor de la fascia lata',
  cuadriceps: 'Cuádriceps', sartorio: 'Sartorio', aductores: 'Aductores', isquios: 'Isquiotibiales',
  gemelos: 'Gemelos', soleo: 'Sóleo', tibial: 'Tibial anterior', peroneos: 'Peroneos',
};

/** Pieza del puzle: un musculo, o null si es una parte neutra (cabeza, manos, rodillas, pies...). */
export interface Pieza {
  musculo: Musculo | null;
  /** Bordes en orden; con '-' delante se recorre al reves. */
  bordes: string[];
}

export interface Vista {
  bordes: Record<string, number[]>;
  piezas: Pieza[];
}

/** Contorno de media figura, de la coronilla a la entrepierna por fuera. Igual de frente y de espalda. */
const CONTORNO: number[] = [
  100, 5, 111, 7, 118, 16, 119, 30, 116, 42, 110, 50, 109, 58, 122, 63, 140, 67, 155, 71, 164, 82,
  167, 100, 168, 112, 170, 124, 173, 146, 180, 168, 186, 192, 189, 210, 193, 222, 193, 236, 187, 246,
  179, 245, 175, 232, 173, 216, 165, 192, 157, 168, 151, 152, 146, 132, 141, 116, 139, 140, 137.9, 148,
  135, 168, 136, 182, 137, 190, 140, 200, 143, 210, 145, 226, 146, 236, 144, 256, 144, 268, 139, 298,
  137.5, 309, 136, 318, 138.5, 330, 138.3, 350, 138, 366, 136.7, 372, 132, 394, 131, 412, 140, 426,
  138, 435, 118, 436, 114, 422, 115, 404, 113, 392, 111, 380, 109.4, 365, 107, 350, 108, 326, 109, 310,
  107.5, 296, 106, 282, 103, 252, 101, 230, 100, 228,
];

/** Tramo del contorno entre dos de sus puntos, en el sentido en que se piden. */
function contorno(x1: number, y1: number, x2: number, y2: number): number[] {
  const indice = (x: number, y: number) => {
    for (let i = 0; i < CONTORNO.length; i += 2) if (CONTORNO[i] === x && CONTORNO[i + 1] === y) return i / 2;
    throw new Error(`El punto ${x},${y} no esta en el contorno`);
  };
  const [a, b] = [indice(x1, y1), indice(x2, y2)];
  const tramo = CONTORNO.slice(Math.min(a, b) * 2, Math.max(a, b) * 2 + 2);
  if (a < b) return tramo;
  const alReves: number[] = [];
  for (let i = tramo.length - 2; i >= 0; i -= 2) alReves.push(tramo[i], tramo[i + 1]);
  return alReves;
}

// Brazo y mano: iguales de frente y de espalda salvo el reparto de la parte de arriba.
const BORDES_BRAZO: Record<string, number[]> = {
  oBrazoExt: contorno(168, 112, 173, 146),
  oBrazoInt: contorno(151, 152, 141, 116),
  codo1: [151, 152, 154, 151],
  codo2: [154, 151, 159, 150.5, 164, 149],
  codo3: [164, 149, 173, 146],
  ante: [164, 149, 172, 176, 180, 200, 181, 213],
  oAnteExt: contorno(173, 146, 189, 210),
  oAnteInt: contorno(173, 216, 151, 152),
  oMano: contorno(189, 210, 173, 216),
  muneca1: [189, 210, 181, 213],
  muneca2: [181, 213, 173, 216],
};
const PIEZAS_ANTEBRAZO: Pieza[] = [
  // Braquiorradial y extensores por fuera, flexores por dentro.
  { musculo: 'antebrazo', bordes: ['codo3', 'oAnteExt', 'muneca1', '-ante'] },
  { musculo: 'antebrazo', bordes: ['codo1', 'codo2', 'ante', 'muneca2', 'oAnteInt'] },
  { musculo: null, bordes: ['oMano', '-muneca2', '-muneca1'] },
];

// Cabeza, rodilla y pie: comunes a las dos vistas.
const BORDES_COMUNES: Record<string, number[]> = {
  oCabeza: contorno(100, 5, 116, 42),
  oDelt: contorno(140, 67, 168, 112),
  oRodilla: contorno(137.5, 309, 138.5, 330),
  oRodInt: contorno(108, 326, 109, 310),
  oSart: contorno(107.5, 296, 109, 310),
  oPie: contorno(131, 412, 115, 404),
};

export const FRENTE: Vista = {
  bordes: {
    ...BORDES_COMUNES,
    ...BORDES_BRAZO,
    // Cabeza, garganta y cuello.
    mand1: [116, 42, 114, 44.5, 112, 46],
    mand2: [112, 46, 108, 48.5, 100, 50],
    mCab: [100, 50, 100, 5],
    mCuello: [100, 50, 100, 70],
    scmIn: [112, 46, 107, 57, 100, 70],
    scmOut: [106, 71, 108, 64, 109, 58],
    oCuello: contorno(116, 42, 109, 58),
    oTrap: contorno(109, 58, 140, 67),
    // Clavicula y surco deltopectoral.
    clav1: [100, 70, 106, 71],
    clav2: [106, 71, 116, 71.5, 124, 70.5, 134, 69],
    clav3: [134, 69, 140, 67],
    dp: [134, 69, 141, 78, 147, 90, 152, 98],
    deltIn1: [168, 112, 162, 108],
    deltIn2: [162, 108, 157, 103, 152, 98],
    // Axila y borde inferior del pectoral.
    axila1: [152, 98, 147, 106],
    axila2: [147, 106, 144, 111, 141, 116],
    pecLow1: [141, 116, 134, 115, 128, 115],
    pecLow2: [128, 115, 120, 119],
    pecLow3: [120, 119, 110, 121, 100, 120],
    mPec: [100, 120, 100, 70],
    // Brazo: triceps por dentro, biceps en medio, braquial por fuera.
    bicIn: [147, 106, 150, 124, 152, 140, 154, 151],
    bicOut: [162, 108, 164, 124, 165, 138, 164, 149],
    // Costado: dorsal, dientes del serrato, oblicuos.
    latIn: [141, 116, 137, 128, 135.5, 140, 137.9, 148],
    oLat: contorno(141, 116, 137.9, 148),
    serr: [128, 115, 133, 119, 129, 123, 134, 128, 130, 132, 133, 137, 131, 141, 137.9, 148],
    oFlanco: contorno(137.9, 148, 140, 200),
    // Linea semilunar (borde del recto abdominal) e intersecciones tendinosas.
    linSem1: [120, 119, 121, 140],
    linSem2: [121, 140, 121, 162],
    linSem3: [121, 162, 121, 184],
    linSem4: [121, 184, 119, 204, 112, 218],
    linSem5: [112, 218, 106, 222, 100, 224],
    i1: [100, 140, 110, 141, 121, 140],
    i2: [100, 162, 110, 163, 121, 162],
    i3: [100, 184, 110, 185, 121, 184],
    mAbs1: [100, 120, 100, 140],
    mAbs2: [100, 140, 100, 162],
    mAbs3: [100, 162, 100, 184],
    mAbs4: [100, 184, 100, 224],
    ingle: [112, 218, 124, 212, 133, 206, 140, 200],
    // Muslo.
    mIngle: [100, 224, 100, 228],
    oMusloInt: contorno(100, 228, 107.5, 296),
    oCadera: contorno(140, 200, 146, 236),
    oVL: contorno(146, 236, 137.5, 309),
    sartIn: [140, 200, 131, 214, 121, 244, 113, 272, 107.5, 296],
    sartOut1: [140, 200, 135, 216, 127, 244, 118, 274],
    sartOut2: [118, 274, 113, 292, 109, 310],
    tfl: [140, 200, 137, 214, 140, 226, 146, 236],
    vl: [146, 236, 139, 252, 135, 280, 131, 300, 130, 307],
    vm: [118, 274, 122, 284, 123, 296, 120, 306],
    rod1: [109, 310, 114, 307.5, 120, 306],
    rod2: [120, 306, 125, 305.5, 130, 307],
    rod3: [130, 307, 137.5, 309],
    // Pierna: borde inferior de la rodilla y lo que baja de el.
    rb1: [138.5, 330, 134, 333],
    rb2: [134, 333, 127, 334],
    rb3: [127, 334, 118, 333],
    rb4: [118, 333, 112, 331],
    rb5: [112, 331, 108, 326],
    gl: [134, 333, 136, 346, 136.5, 360, 136.7, 372],
    oGemExt: contorno(138.5, 330, 136.7, 372),
    oPeroneo: contorno(136.7, 372, 131, 412),
    tibOut: [127, 334, 129, 350, 130, 372, 128, 394, 127, 410],
    tibIn: [118, 333, 121, 350, 122, 372, 121, 392, 120, 408],
    tibia1: [112, 331, 114, 348, 114.6, 362],
    tibia2: [114.6, 362, 115.5, 378, 117, 392, 117, 406],
    gsol: [114.6, 362, 111.5, 365, 109.4, 365],
    oGemInt: contorno(108, 326, 109.4, 365),
    oSoleoInt: contorno(109.4, 365, 115, 404),
    t1: [131, 412, 127, 410],
    t2: [127, 410, 120, 408],
    t3: [120, 408, 117, 406],
    t4: [117, 406, 115, 404],
  },
  piezas: [
    { musculo: null, bordes: ['oCabeza', 'mand1', 'mand2', 'mCab'] },
    { musculo: null, bordes: ['mand2', 'mCuello', '-scmIn'] },
    { musculo: 'cuello', bordes: ['mand1', 'scmIn', 'clav1', 'scmOut', '-oCuello'] },
    { musculo: 'trapecio', bordes: ['oTrap', '-clav3', '-clav2', 'scmOut'] },
    { musculo: 'deltoides', bordes: ['clav3', 'oDelt', 'deltIn1', 'deltIn2', '-dp'] },
    { musculo: 'pectoral', bordes: ['clav1', 'clav2', 'dp', 'axila1', 'axila2', 'pecLow1', 'pecLow2', 'pecLow3', 'mPec'] },
    { musculo: 'triceps', bordes: ['axila2', '-oBrazoInt', 'codo1', '-bicIn'] },
    { musculo: 'biceps', bordes: ['axila1', 'bicIn', 'codo2', '-bicOut', 'deltIn2'] },
    { musculo: 'braquial', bordes: ['-deltIn1', 'oBrazoExt', '-codo3', '-bicOut'] },
    ...PIEZAS_ANTEBRAZO,
    { musculo: 'dorsal', bordes: ['latIn', '-oLat'] },
    { musculo: 'serrato', bordes: ['pecLow1', 'serr', '-latIn'] },
    { musculo: 'oblicuos', bordes: ['pecLow2', 'linSem1', 'linSem2', 'linSem3', 'linSem4', 'ingle', '-oFlanco', '-serr'] },
    { musculo: 'abdominales', bordes: ['-pecLow3', 'linSem1', '-i1', '-mAbs1'] },
    { musculo: 'abdominales', bordes: ['i1', 'linSem2', '-i2', '-mAbs2'] },
    { musculo: 'abdominales', bordes: ['i2', 'linSem3', '-i3', '-mAbs3'] },
    { musculo: 'abdominales', bordes: ['i3', 'linSem4', 'linSem5', '-mAbs4'] },
    { musculo: 'aductores', bordes: ['-ingle', 'linSem5', 'mIngle', 'oMusloInt', '-sartIn'] },
    { musculo: 'sartorio', bordes: ['sartOut1', 'sartOut2', '-oSart', '-sartIn'] },
    { musculo: 'tensor', bordes: ['oCadera', '-tfl'] },
    // Cuadriceps: recto femoral, vasto medial y vasto lateral.
    { musculo: 'cuadriceps', bordes: ['tfl', 'vl', '-rod2', '-vm', '-sartOut1'] },
    { musculo: 'cuadriceps', bordes: ['sartOut2', 'rod1', '-vm'] },
    { musculo: 'cuadriceps', bordes: ['oVL', '-rod3', '-vl'] },
    { musculo: null, bordes: ['rod1', 'rod2', 'rod3', 'oRodilla', 'rb1', 'rb2', 'rb3', 'rb4', 'rb5', 'oRodInt'] },
    { musculo: 'gemelos', bordes: ['rb1', 'gl', '-oGemExt'] },
    { musculo: 'peroneos', bordes: ['rb2', 'tibOut', '-t1', '-oPeroneo', '-gl'] },
    { musculo: 'tibial', bordes: ['rb3', 'tibIn', '-t2', '-tibOut'] },
    // La tibia, sin musculo encima, entre el tibial y el gemelo interno.
    { musculo: null, bordes: ['rb4', 'tibia1', 'tibia2', '-t3', '-tibIn'] },
    { musculo: 'gemelos', bordes: ['rb5', 'oGemInt', '-gsol', '-tibia1'] },
    { musculo: 'soleo', bordes: ['tibia2', 't4', '-oSoleoInt', '-gsol'] },
    { musculo: null, bordes: ['oPie', '-t4', '-t3', '-t2', '-t1'] },
  ],
};

export const ESPALDA: Vista = {
  bordes: {
    ...BORDES_COMUNES,
    ...BORDES_BRAZO,
    occ: [116, 42, 108, 45.5, 100, 47],
    mCab: [100, 47, 100, 5],
    oCuelloTrap: contorno(116, 42, 140, 67),
    // Trapecio: borde por la espina de la escapula y en punta hasta la columna.
    trapLat1: [140, 67, 136, 71, 132, 76],
    trapLat2: [132, 76, 127, 81, 122, 86],
    trapLow1: [122, 86, 117, 96, 112, 108],
    trapLow2: [112, 108, 109, 116, 106, 124],
    trapLow3: [106, 124, 103, 131, 100, 138],
    mTrap: [100, 138, 100, 47],
    deltB1: [168, 112, 158, 105, 150, 97],
    deltB2: [150, 97, 141, 86, 132, 76],
    redLow: [112, 108, 126, 111, 140, 106, 150, 97],
    axB: [150, 97, 146, 107, 141, 116],
    // Dorsal, lumbares y gluteos.
    oLatB: contorno(141, 116, 136, 182),
    latLow: [136, 182, 124, 190, 112, 196, 108, 198],
    latMed: [108, 198, 108, 172, 107, 146, 106, 124],
    sacro: [108, 198, 104, 203, 100, 206],
    mLumbar: [100, 206, 100, 138],
    oFlancoB: contorno(136, 182, 145, 226),
    gmax: [100, 206, 110, 207, 124, 204, 136, 210, 145, 226],
    oGluteo: contorno(145, 226, 144, 256),
    oGluteoInt: contorno(103, 252, 100, 228),
    mGluteo: [100, 228, 100, 206],
    // Pliegue del gluteo y muslo.
    f1: [144, 256, 140, 257],
    f2: [140, 257, 131, 259, 122, 260],
    f3: [122, 260, 115, 259.5, 108, 258],
    f4: [108, 258, 103, 252],
    oVLB: contorno(144, 256, 137.5, 309),
    oMusloIntB: contorno(103, 252, 107.5, 296),
    bfOut: [140, 257, 139, 280, 134, 304, 131, 314],
    ham: [122, 260, 123, 280, 122, 300, 121, 314],
    addB: [108, 258, 109, 272, 108, 288, 107.5, 296],
    pop1: [137.5, 309, 131, 314],
    pop2: [131, 314, 126, 315, 121, 314],
    pop3: [121, 314, 115, 313, 109, 310],
    // Gemelos, soleo y tobillo.
    gt1: [108, 326, 114, 322, 122, 320],
    gt2: [122, 320, 130, 322, 138.5, 330],
    gmid: [122, 320, 122, 340, 121, 358, 121, 369],
    gBotI: [107, 350, 110, 360, 114, 368, 121, 369],
    gBotO: [121, 369, 128, 368, 134, 362, 138.3, 350],
    oGemIntB: contorno(108, 326, 107, 350),
    oGemExtB: contorno(138.5, 330, 138.3, 350),
    oSoleoExt: contorno(138.3, 350, 132, 394),
    oSoleoIntB: contorno(113, 392, 107, 350),
    solBot: [132, 394, 126, 398, 118, 396, 113, 392],
    oTobExt: contorno(132, 394, 131, 412),
    oTobInt: contorno(115, 404, 113, 392),
    tobB: [131, 412, 123, 410, 115, 404],
  },
  piezas: [
    { musculo: null, bordes: ['oCabeza', 'occ', 'mCab'] },
    { musculo: 'trapecio', bordes: ['-occ', 'oCuelloTrap', 'trapLat1', 'trapLat2', 'trapLow1', 'trapLow2', 'trapLow3', 'mTrap'] },
    { musculo: 'deltoides', bordes: ['oDelt', 'deltB1', 'deltB2', '-trapLat1'] },
    { musculo: 'redondo', bordes: ['trapLat2', 'trapLow1', 'redLow', 'deltB2'] },
    { musculo: 'triceps', bordes: ['oBrazoExt', '-codo3', '-codo2', '-codo1', 'oBrazoInt', '-axB', '-deltB1'] },
    ...PIEZAS_ANTEBRAZO,
    { musculo: 'dorsal', bordes: ['redLow', 'axB', 'oLatB', 'latLow', 'latMed', '-trapLow2'] },
    { musculo: 'lumbar', bordes: ['-trapLow3', '-latMed', 'sacro', 'mLumbar'] },
    { musculo: 'gluteoMedio', bordes: ['-latLow', 'oFlancoB', '-gmax', '-sacro'] },
    { musculo: 'gluteo', bordes: ['gmax', 'oGluteo', 'f1', 'f2', 'f3', 'f4', 'oGluteoInt', 'mGluteo'] },
    // Vasto lateral (se ve por fuera), biceps femoral, semitendinoso y aductor mayor.
    { musculo: 'cuadriceps', bordes: ['f1', 'bfOut', '-pop1', '-oVLB'] },
    { musculo: 'isquios', bordes: ['f2', 'ham', '-pop2', '-bfOut'] },
    { musculo: 'isquios', bordes: ['f3', 'addB', 'oSart', '-pop3', '-ham'] },
    { musculo: 'aductores', bordes: ['f4', 'oMusloIntB', '-addB'] },
    // Hueco popliteo, detras de la rodilla.
    { musculo: null, bordes: ['pop1', 'pop2', 'pop3', '-oRodInt', 'gt1', 'gt2', '-oRodilla'] },
    { musculo: 'gemelos', bordes: ['gt1', 'gmid', '-gBotI', '-oGemIntB'] },
    { musculo: 'gemelos', bordes: ['gt2', 'oGemExtB', '-gBotO', '-gmid'] },
    { musculo: 'soleo', bordes: ['gBotI', 'gBotO', 'oSoleoExt', 'solBot', 'oSoleoIntB'] },
    { musculo: null, bordes: ['oTobExt', 'tobB', 'oTobInt', '-solBot'] },
    { musculo: null, bordes: ['oPie', '-tobB'] },
  ],
};
