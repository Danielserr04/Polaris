import type { TrabajoMuscular } from '../../api/atlas';
import { CONTORNO, ESPALDA, FRENTE, NOMBRE_MUSCULO, type Musculo, type Vista } from './mapaMuscular.data';
import './mapaMuscular.css';

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

/** Hueco entre musculos y con el contorno, en unidades del dibujo (200 x 440). */
const HUECO = 1.4;

const area = (pts: Punto[]) =>
  pts.reduce((a, [x1, y1], i) => {
    const [x2, y2] = pts[(i + 1) % pts.length];
    return a + x1 * y2 - x2 * y1;
  }, 0) / 2;

/** Poligono de una pieza encadenando sus bordes, sin puntos repetidos. */
function poligono(vista: Vista, bordes: string[]): Punto[] {
  const pts: Punto[] = [];
  for (const b of bordes) {
    const alReves = b.startsWith('-');
    const borde = pares(vista.bordes[alReves ? b.slice(1) : b]);
    for (const p of alReves ? borde.reverse() : borde) {
      const u = pts[pts.length - 1];
      if (!u || Math.hypot(p[0] - u[0], p[1] - u[1]) > 0.01) pts.push(p);
    }
  }
  const [a, z] = [pts[0], pts[pts.length - 1]];
  if (Math.hypot(a[0] - z[0], a[1] - z[1]) <= 0.01) pts.pop();
  return pts;
}

/** Mueve cada vertice hacia dentro, por la bisectriz, para dejar un hueco con las piezas vecinas. */
function encoger(pts: Punto[], d: number): Punto[] {
  const s = area(pts) > 0 ? 1 : -1;
  const n = pts.length;
  return pts.map(([x, y], i) => {
    const [xa, ya] = pts[(i - 1 + n) % n];
    const [xb, yb] = pts[(i + 1) % n];
    const la = Math.hypot(x - xa, y - ya);
    const lb = Math.hypot(xb - x, yb - y);
    // Normales hacia dentro de los dos lados que llegan al vertice.
    const [nax, nay] = [(-(y - ya) / la) * s, ((x - xa) / la) * s];
    const [nbx, nby] = [(-(yb - y) / lb) * s, ((xb - x) / lb) * s];
    const [bx, by] = [nax + nbx, nay + nby];
    const lbis = Math.hypot(bx, by) || 1;
    const coseno = Math.max(0.35, (bx * nax + by * nay) / lbis);
    const largo = d / coseno;
    return [x + (bx / lbis) * largo, y + (by / lbis) * largo];
  });
}

/** Redondea las esquinas cortandolas (Chaikin): la curva queda dentro del poligono. */
function redondear(pts: Punto[], veces: number): Punto[] {
  let r = pts;
  for (let k = 0; k < veces; k++) {
    r = r.flatMap(([x1, y1], i): Punto[] => {
      const [x2, y2] = r[(i + 1) % r.length];
      return [[0.75 * x1 + 0.25 * x2, 0.75 * y1 + 0.25 * y2], [0.25 * x1 + 0.75 * x2, 0.25 * y1 + 0.75 * y2]];
    });
  }
  return r;
}

const camino = (pts: Punto[]) => `M${pts.map(([x, y]) => `${n1(x)} ${n1(y)}`).join('L')}Z`;
const reflejar = (pts: Punto[]) => pts.map(([x, y]): Punto => [200 - x, y]);

/** Curva cerrada que pasa por los puntos (Catmull-Rom pasada a Bezier cubica), para la silueta. */
function curva(pts: Punto[]): string {
  const n = pts.length;
  let d = `M${n1(pts[0][0])} ${n1(pts[0][1])}`;
  for (let i = 0; i < n; i++) {
    const [p0, p1, p2, p3] = [pts[(i - 1 + n) % n], pts[i], pts[(i + 1) % n], pts[(i + 2) % n]];
    d += `C${n1(p1[0] + (p2[0] - p0[0]) / 6)} ${n1(p1[1] + (p2[1] - p0[1]) / 6)} `
      + `${n1(p2[0] - (p3[0] - p1[0]) / 6)} ${n1(p2[1] - (p3[1] - p1[1]) / 6)} ${n1(p2[0])} ${n1(p2[1])}`;
  }
  return `${d}Z`;
}

// Los trazos no dependen de los datos: se calculan una vez al cargar el modulo.
const mitadSilueta = pares(CONTORNO);
const SILUETA = curva([...mitadSilueta, ...reflejar(mitadSilueta).reverse().slice(1, -1)]);
const trazos = (vista: Vista) =>
  vista.piezas.flatMap((p) => {
    // Las piezas sin musculo (cabeza, manos, rodillas...) no se pintan: se ve la silueta.
    if (!p.musculo) return [];
    const forma = redondear(encoger(poligono(vista, p.bordes), HUECO), 3);
    return [{
      musculo: p.musculo,
      nombre: p.parte ? `${NOMBRE_MUSCULO[p.musculo]} (${p.parte})` : NOMBRE_MUSCULO[p.musculo],
      d: [camino(forma), camino(reflejar(forma))],
    }];
  });
const VISTAS = { frente: trazos(FRENTE), espalda: trazos(ESPALDA) };

function Figura({ vista, series, maximo, titulo, compacto }: { vista: keyof typeof VISTAS; series: Map<Musculo, number>; maximo: number; titulo: string; compacto: boolean }) {
  return (
    <figure className="atl-mapa__fig">
      <svg viewBox="0 0 200 440" role="img" aria-label={titulo}>
        <path d={SILUETA} className="atl-mapa__silueta" />
        {VISTAS[vista].map((p, i) => {
          const n = series.get(p.musculo) ?? 0;
          // Opacidad entre 0,25 y 1 segun las series respecto al musculo mas trabajado.
          const estilo = n > 0 ? { fill: 'var(--accent)', fillOpacity: 0.25 + 0.75 * (n / maximo) } : undefined;
          return (
            <g key={`${p.musculo}-${i}`} className="atl-mapa__musculo" style={estilo}>
              <title>{n > 0 ? `${p.nombre}: ${n} series` : p.nombre}</title>
              {p.d.map((d) => <path key={d} d={d} />)}
            </g>
          );
        })}
      </svg>
      {!compacto && <figcaption>{titulo}</figcaption>}
    </figure>
  );
}

/** Frente y espalda con los musculos coloreados segun las series del rango; compacto, sin rotulos. */
export function MapaMuscular({ trabajo, compacto = false }: { trabajo: TrabajoMuscular[]; compacto?: boolean }) {
  const series = seriesPorMusculo(trabajo);
  const maximo = Math.max(1, ...series.values());
  return (
    <div className={compacto ? 'atl-mapa atl-mapa--compacto' : 'atl-mapa'}>
      <Figura vista="frente" series={series} maximo={maximo} titulo="Frente" compacto={compacto} />
      <Figura vista="espalda" series={series} maximo={maximo} titulo="Espalda" compacto={compacto} />
    </div>
  );
}
