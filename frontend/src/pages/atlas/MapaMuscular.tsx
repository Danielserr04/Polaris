import type { TrabajoMuscular } from '../../api/atlas';
import { ESPALDA, FRENTE, NOMBRE_MUSCULO, type Musculo, type Vista } from './mapaMuscular.data';

// El grupo muscular de un ejercicio es texto libre ("Pecho", "Espalda alta", "Cuádriceps"...):
// se reconoce por palabras clave y se reparte entre los musculos del dibujo.
const CLAVES: [RegExp, Musculo[]][] = [
  [/pecho|pectoral/, ['pectoral']],
  [/hombro|deltoid/, ['deltoides']],
  [/bicep/, ['biceps', 'braquial']],
  [/braquial/, ['braquial']],
  [/tricep/, ['triceps']],
  [/antebrazo/, ['antebrazo']],
  [/(^|[^e])brazo/, ['biceps', 'braquial', 'triceps']],
  [/abdom|abs\b|core/, ['abdominales', 'oblicuos']],
  [/oblicuo/, ['oblicuos']],
  [/serrato/, ['serrato']],
  [/lumbar/, ['lumbar']],
  [/trapecio/, ['trapecio']],
  [/redondo|infraespin|manguito/, ['redondo']],
  [/dorsal/, ['dorsal']],
  [/espalda alta/, ['trapecio', 'redondo', 'dorsal']],
  [/espalda(?! alta)/, ['dorsal', 'trapecio', 'redondo', 'lumbar']],
  [/cuadricep/, ['cuadriceps']],
  [/sartorio/, ['sartorio']],
  [/cuello|esternocleido/, ['cuello']],
  [/isquio|femoral/, ['isquios']],
  [/glute/, ['gluteo', 'gluteoMedio']],
  [/tensor|fascia lata/, ['tensor']],
  [/abductor/, ['gluteoMedio', 'tensor']],
  [/(^|[^b])aductor/, ['aductores']],
  [/gemelo|pantorrilla/, ['gemelos', 'soleo']],
  [/soleo/, ['soleo']],
  [/tibial/, ['tibial']],
  [/peroneo/, ['peroneos']],
  [/pierna|tren inferior/, ['cuadriceps', 'sartorio', 'aductores', 'isquios', 'gluteo', 'gemelos', 'soleo']],
  [/cuerpo completo|full ?body/, ['pectoral', 'deltoides', 'dorsal', 'trapecio', 'cuadriceps', 'isquios', 'gluteo', 'abdominales']],
];

const normalizar = (s: string) => s.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');

/** Musculos del dibujo que corresponden a un grupo escrito a mano; vacio si no se reconoce. */
export function musculosDe(grupo: string): Musculo[] {
  const g = normalizar(grupo);
  const encontrados = CLAVES.filter(([re]) => re.test(g)).flatMap(([, m]) => m);
  return [...new Set(encontrados)];
}

/** Series por musculo del dibujo: cada grupo suma sus series a todos sus musculos. */
export function seriesPorMusculo(trabajo: TrabajoMuscular[]): Map<Musculo, number> {
  const series = new Map<Musculo, number>();
  for (const t of trabajo) {
    for (const m of musculosDe(t.grupoMuscular)) series.set(m, (series.get(m) ?? 0) + t.numeroSeries);
  }
  return series;
}

type Punto = [number, number];

const pares = (p: number[]): Punto[] => {
  const r: Punto[] = [];
  for (let i = 0; i < p.length; i += 2) r.push([p[i], p[i + 1]]);
  return r;
};
const n1 = (n: number) => Math.round(n * 10) / 10;

/**
 * Tramos de curva (Catmull-Rom pasada a Bezier cubica) que pasan por los puntos de un borde,
 * sin el punto de partida. Solo depende del propio borde, asi que las dos piezas que lo
 * comparten dibujan exactamente la misma linea y encajan.
 */
function tramos(pts: Punto[]): string {
  const n = pts.length;
  let d = '';
  for (let i = 0; i < n - 1; i++) {
    const [p0, p1, p2, p3] = [pts[Math.max(i - 1, 0)], pts[i], pts[i + 1], pts[Math.min(i + 2, n - 1)]];
    d += `C${n1(p1[0] + (p2[0] - p0[0]) / 6)} ${n1(p1[1] + (p2[1] - p0[1]) / 6)} `
      + `${n1(p2[0] - (p3[0] - p1[0]) / 6)} ${n1(p2[1] - (p3[1] - p1[1]) / 6)} ${n1(p2[0])} ${n1(p2[1])}`;
  }
  return d;
}

/** Trazo de una pieza encadenando sus bordes; reflejada, para el lado izquierdo de la imagen. */
function trazo(vista: Vista, bordes: string[], reflejada: boolean): string {
  let d = '';
  for (const b of bordes) {
    const alReves = b.startsWith('-');
    let pts = pares(vista.bordes[alReves ? b.slice(1) : b]);
    if (alReves) pts = pts.reverse();
    if (reflejada) pts = pts.map(([x, y]): Punto => [200 - x, y]);
    if (!d) d = `M${n1(pts[0][0])} ${n1(pts[0][1])}`;
    d += tramos(pts);
  }
  return `${d}Z`;
}

// Los trazos no dependen de los datos: se calculan una vez al cargar el modulo.
const trazos = (vista: Vista) =>
  vista.piezas.map((p) => ({ musculo: p.musculo, d: [trazo(vista, p.bordes, false), trazo(vista, p.bordes, true)] }));
const VISTAS = { frente: trazos(FRENTE), espalda: trazos(ESPALDA) };

function Figura({ vista, series, maximo, titulo }: { vista: keyof typeof VISTAS; series: Map<Musculo, number>; maximo: number; titulo: string }) {
  return (
    <figure className="atl-mapa__fig">
      <svg viewBox="0 0 200 440" role="img" aria-label={titulo}>
        {VISTAS[vista].map((p, i) => {
          if (!p.musculo) return p.d.map((d) => <path key={d} d={d} className="atl-mapa__hueso" />);
          const n = series.get(p.musculo) ?? 0;
          // Opacidad entre 0,25 y 1 segun las series respecto al musculo mas trabajado.
          const estilo = n > 0 ? { fill: 'var(--accent)', fillOpacity: 0.25 + 0.75 * (n / maximo) } : undefined;
          return (
            <g key={`${p.musculo}-${i}`} className="atl-mapa__musculo" style={estilo}>
              <title>{n > 0 ? `${NOMBRE_MUSCULO[p.musculo]}: ${n} series` : NOMBRE_MUSCULO[p.musculo]}</title>
              {p.d.map((d) => <path key={d} d={d} />)}
            </g>
          );
        })}
      </svg>
      <figcaption>{titulo}</figcaption>
    </figure>
  );
}

/** Frente y espalda con los musculos coloreados segun las series del rango. */
export function MapaMuscular({ trabajo }: { trabajo: TrabajoMuscular[] }) {
  const series = seriesPorMusculo(trabajo);
  const maximo = Math.max(1, ...series.values());
  return (
    <div className="atl-mapa">
      <Figura vista="frente" series={series} maximo={maximo} titulo="Frente" />
      <Figura vista="espalda" series={series} maximo={maximo} titulo="Espalda" />
    </div>
  );
}
