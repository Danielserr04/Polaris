// Datos de muestra. Odisea: datos reales del briefing. Kuiper/Fusión/Atlas/Núcleo: MAQUETA (inventados).
window.PD = {
  user: { id: 11, username: 'dani', email: 'danielserrano1702@gmail.com', nombre: 'Daniel', avatarUrl: null, creadoEn: '2026-08-30T16:41:44Z', emailVerificado: true, tieneGoogle: true, tienePassword: true },
  entradas: [
    { id: 1, titulo: 'Interstellar', tituloOriginal: 'Interstellar', tipo: 'PELICULA', anio: 2014, duracionMin: 169, generos: 'Aventura, Drama, Ciencia ficción', fuenteExterna: 'TMDB', estado: 'TERMINADO', valoracion: 10, favorito: true, fechaInicio: '2026-08-02', fechaFin: '2026-08-02', progreso: null, notas: 'Vista en versión original. La escena del muelle sigue igual de bien.', sinopsis: 'Un grupo de exploradores cruza un agujero de gusano buscando un nuevo hogar para la humanidad.' },
    { id: 2, titulo: 'Breaking Bad', tituloOriginal: 'Breaking Bad', tipo: 'SERIE', anio: 2008, duracionMin: 45, generos: 'Drama, Crimen', fuenteExterna: 'TMDB', estado: 'EN_CURSO', valoracion: 9, favorito: true, fechaInicio: '2026-09-01', fechaFin: null, progreso: 34, notas: null, sinopsis: null },
    { id: 3, titulo: 'The Legend of Zelda', tituloOriginal: null, tipo: 'JUEGO', anio: 1986, duracionMin: null, generos: 'Aventura', fuenteExterna: 'IGDB', estado: 'ABANDONADO', valoracion: 6, favorito: false, fechaInicio: '2026-07-12', fechaFin: null, progreso: null, notas: null, sinopsis: null },
    { id: 4, titulo: 'Dune', tituloOriginal: 'Dune', tipo: 'LIBRO', anio: 1965, duracionMin: 607, generos: 'Ciencia ficción, Ficción', fuenteExterna: 'OPEN_LIBRARY', estado: 'EN_CURSO', valoracion: null, favorito: false, fechaInicio: '2026-09-10', fechaFin: null, progreso: 212, notas: null, sinopsis: 'Frank Herbert' },
    { id: 5, titulo: 'Blade Runner 2049', tituloOriginal: 'Blade Runner 2049', tipo: 'PELICULA', anio: 2017, duracionMin: 164, generos: 'Ciencia ficción, Drama', fuenteExterna: 'TMDB', estado: 'PENDIENTE', valoracion: null, favorito: false, fechaInicio: null, fechaFin: null, progreso: null, notas: null, sinopsis: null },
  ],
  catalogo: [
    { titulo: 'The Legend of Zelda: Breath of the Wild', tipo: 'JUEGO', anio: 2017, fuenteExterna: 'IGDB' },
    { titulo: 'Dune: Part Two', tipo: 'PELICULA', anio: 2024, fuenteExterna: 'TMDB' },
    { titulo: 'Dune Messiah', tipo: 'LIBRO', anio: 1969, fuenteExterna: 'OPEN_LIBRARY' },
    { titulo: 'Better Call Saul', tipo: 'SERIE', anio: 2015, fuenteExterna: 'TMDB' },
  ],
  // ---- MAQUETA ----
  kuiper: {
    presupuesto: 1800, gastado: 1284.5, ingresos: 2450,
    dias: [42, 18, 64, 30, 12, 88, 74, 22, 35, 9, 51, 60, 140, 26, 33, 47, 15, 70, 38, 29, 95, 21, 44, 57, 31],
    categorias: [
      { nombre: 'Casa', gastado: 620, limite: 650, icon: 'house' }, { nombre: 'Comida', gastado: 318.4, limite: 380, icon: 'shopping-basket' },
      { nombre: 'Ocio', gastado: 136.6, limite: 120, icon: 'ticket' }, { nombre: 'Transporte', gastado: 128, limite: 180, icon: 'train-front' },
      { nombre: 'Suscripciones', gastado: 81.5, limite: 90, icon: 'repeat' },
    ],
    movimientos: [
      { fecha: '25 sep', concepto: 'Mercadona', categoria: 'Comida', tipo: 'GASTO', importe: 31.2 }, { fecha: '24 sep', concepto: 'Abono transporte', categoria: 'Transporte', tipo: 'GASTO', importe: 44 },
      { fecha: '23 sep', concepto: 'Cine — Yelmo', categoria: 'Ocio', tipo: 'GASTO', importe: 18.5 }, { fecha: '22 sep', concepto: 'Venta Wallapop', categoria: 'Otros', tipo: 'INGRESO', importe: 60 },
      { fecha: '21 sep', concepto: 'Luz', categoria: 'Casa', tipo: 'GASTO', importe: 57.3 }, { fecha: '20 sep', concepto: 'Spotify', categoria: 'Suscripciones', tipo: 'GASTO', importe: 10.99 },
    ],
  },
  fusion: {
    kcal: 1640, objetivo: 2300,
    macros: [{ nombre: 'Proteína', g: 112, obj: 150 }, { nombre: 'Carbohidratos', g: 168, obj: 260 }, { nombre: 'Grasa', g: 52, obj: 75 }],
    comidas: [
      { momento: 'Desayuno', hora: '08:10', lineas: [['Avena', 60, 228], ['Leche semidesnatada', 250, 115], ['Plátano', 120, 107]] },
      { momento: 'Comida', hora: '14:30', lineas: [['Arroz blanco', 90, 315], ['Pechuga de pollo', 180, 297], ['Aceite de oliva', 10, 88]] },
      { momento: 'Merienda', hora: '18:00', lineas: [['Yogur natural', 125, 76], ['Nueces', 25, 164]] },
      { momento: 'Cena', hora: '—', lineas: [] },
    ],
    semana: [2210, 2380, 1980, 2290, 2450, 2120, 2310, 2260, 2050, 2400, 2330, 2190, 2280, 1640],
  },
  atlas: {
    ultima: { rutina: 'Empuje', dia: 'Martes 23', ejercicios: 6, series: 22, volumen: 8420 },
    siguiente: { rutina: 'Tirón', dia: 'Hoy' },
    progresion: [70, 72.5, 72.5, 74, 75, 75, 76.5, 78, 77.5, 80],
    semanas: ['S28', 'S29', 'S30', 'S31', 'S32', 'S33', 'S34', 'S35', 'S36', 'S37'],
    volumen: [18.2, 20.1, 17.4, 21.8, 22.5, 19.9, 23.4, 24.1, 22.0, 25.3],
    records: [{ ej: 'Press banca', v: '80 kg × 3', fecha: '23 sep', nuevo: true }, { ej: 'Sentadilla', v: '105 kg × 5', fecha: '18 sep' }, { ej: 'Peso muerto', v: '130 kg × 3', fecha: '11 sep' }, { ej: 'Dominadas', v: '+10 kg × 6', fecha: '19 sep' }],
    sesiones: [
      { fecha: 'Mar 23', rutina: 'Empuje', ej: 6, series: 22, vol: 8420 }, { fecha: 'Dom 21', rutina: 'Pierna', ej: 5, series: 20, vol: 11260 },
      { fecha: 'Vie 19', rutina: 'Tirón', ej: 6, series: 21, vol: 7980 }, { fecha: 'Mié 17', rutina: 'Improvisado', ej: 3, series: 9, vol: 2940 },
    ],
  },
  semanaAtlas: [1, 0, 1, 0, 1, 0, 1],
  actividad: [
    { hora: '11:02', mod: 'fusion', icon: 'utensils', txt: 'Merienda registrada', det: '240 kcal' },
    { hora: '10:15', mod: 'kuiper', icon: 'shopping-basket', txt: 'Mercadona', det: '−31,20 €' },
    { hora: 'Ayer', mod: 'odisea', icon: 'tv', txt: 'Breaking Bad · episodio 34', det: 'En curso' },
    { hora: 'Ayer', mod: 'atlas', icon: 'trophy', txt: 'Récord en press banca', det: '80 kg × 3' },
    { hora: 'Mar 23', mod: 'odisea', icon: 'plus', txt: 'Blade Runner 2049 a Pendientes', det: 'TMDB' },
    { hora: 'Mar 23', mod: 'nucleo', icon: 'scale', txt: 'Peso registrado', det: '78,2 kg' },
  ],
  nucleo: { peso: 78.2, delta: -0.4, serie: [79.4, 79.1, 79.3, 78.9, 78.8, 78.9, 78.6, 78.6, 78.4, 78.2] },
};
