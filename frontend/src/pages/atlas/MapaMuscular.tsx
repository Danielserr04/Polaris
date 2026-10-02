import type { TrabajoMuscular } from '../../api/atlas';
import { BACK_MUSCLES, FRONT_MUSCLES, type PoligonosMusculo } from './mapaMuscular.data';

// El grupo muscular de un ejercicio es texto libre ("Pecho", "Espalda alta", "Cuádriceps"...):
// se reconoce por palabras clave y se reparte entre los musculos del dibujo.
const CLAVES: [RegExp, string[]][] = [
  [/pecho|pectoral/, ['CHEST']],
  [/hombro|deltoid/, ['FRONT_DELTOIDS', 'BACK_DELTOIDS']],
  [/bicep/, ['BICEPS']],
  [/tricep/, ['TRICEPS']],
  [/antebrazo/, ['FOREARM']],
  [/(^|[^e])brazo/, ['BICEPS', 'TRICEPS']],
  [/abdom|abs\b|core|oblicuo/, ['ABS', 'OBLIQUES']],
  [/lumbar/, ['LOWER_BACK']],
  [/trapecio/, ['TRAPEZIUS']],
  [/espalda|dorsal/, ['UPPER_BACK', 'LOWER_BACK', 'TRAPEZIUS']],
  [/cuadricep/, ['QUADRICEPS']],
  [/isquio|femoral/, ['HAMSTRING']],
  [/glute/, ['GLUTEAL']],
  [/gemelo|pantorrilla|soleo/, ['CALVES', 'LEFT_SOLEUS', 'RIGHT_SOLEUS']],
  [/aductor|abductor/, ['ABDUCTORS', 'ABDUCTOR']],
  [/pierna|tren inferior/, ['QUADRICEPS', 'HAMSTRING', 'GLUTEAL', 'CALVES']],
  [/cuerpo completo|full ?body/, ['CHEST', 'FRONT_DELTOIDS', 'BACK_DELTOIDS', 'UPPER_BACK', 'QUADRICEPS', 'HAMSTRING', 'GLUTEAL', 'ABS']],
];
const NEUTROS = new Set(['HEAD', 'NECK', 'KNEES']);

const normalizar = (s: string) => s.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');

/** Musculos del dibujo que corresponden a un grupo escrito a mano; vacio si no se reconoce. */
export function musculosDe(grupo: string): string[] {
  const g = normalizar(grupo);
  const encontrados = CLAVES.filter(([re]) => re.test(g)).flatMap(([, m]) => m);
  return [...new Set(encontrados)];
}

/** Series por musculo del dibujo: cada grupo suma sus series a todos sus musculos. */
export function seriesPorMusculo(trabajo: TrabajoMuscular[]): Map<string, number> {
  const series = new Map<string, number>();
  for (const t of trabajo) {
    for (const m of musculosDe(t.grupoMuscular)) series.set(m, (series.get(m) ?? 0) + t.numeroSeries);
  }
  return series;
}

function Figura({ musculos, series, maximo, titulo }: { musculos: PoligonosMusculo[]; series: Map<string, number>; maximo: number; titulo: string }) {
  return (
    <figure className="atl-mapa__fig">
      <svg viewBox="0 0 100 222" role="img" aria-label={titulo}>
        {musculos.map((m) => {
          const n = series.get(m.muscle) ?? 0;
          // Opacidad entre 0,25 y 1 segun las series respecto al musculo mas trabajado.
          const estilo = n > 0 ? { fill: 'var(--accent)', fillOpacity: 0.25 + 0.75 * (n / maximo) } : undefined;
          return m.svgPoints.map((p, i) => (
            <polygon key={`${m.muscle}-${i}`} points={p} className={NEUTROS.has(m.muscle) ? 'atl-mapa__hueso' : undefined} style={estilo}>
              {n > 0 && <title>{`${n} series`}</title>}
            </polygon>
          ));
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
      <Figura musculos={FRONT_MUSCLES} series={series} maximo={maximo} titulo="Frente" />
      <Figura musculos={BACK_MUSCLES} series={series} maximo={maximo} titulo="Espalda" />
    </div>
  );
}
