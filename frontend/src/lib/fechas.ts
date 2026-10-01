// Fechas en la zona local del navegador. toISOString() convierte a UTC y entre la
// medianoche y las 2:00 de Madrid devolveria el dia anterior: aqui se formatea a mano.

const dos = (n: number) => String(n).padStart(2, '0');

/** YYYY-MM-DD de una fecha local. */
export function iso(d: Date): string {
  return `${d.getFullYear()}-${dos(d.getMonth() + 1)}-${dos(d.getDate())}`;
}

/** YYYY-MM de una fecha local (el formato `periodo` de Kuiper). */
export function periodo(d: Date): string {
  return `${d.getFullYear()}-${dos(d.getMonth() + 1)}`;
}

export function sumarDias(d: Date, dias: number): Date {
  const r = new Date(d.getFullYear(), d.getMonth(), d.getDate());
  r.setDate(r.getDate() + dias);
  return r;
}

/** Lunes de la semana de `d` (la semana en España empieza en lunes). */
export function lunesDe(d: Date): Date {
  const dia = (d.getDay() + 6) % 7; // lunes = 0
  return sumarDias(d, -dia);
}

/** Parsea YYYY-MM-DD como fecha local (new Date('2026-10-01') seria UTC). */
export function deIso(s: string): Date {
  const [a, m, d] = s.split('-').map(Number);
  return new Date(a, m - 1, d);
}

export function diasEntre(desde: Date, hasta: Date): number {
  const ms = Date.UTC(hasta.getFullYear(), hasta.getMonth(), hasta.getDate()) -
    Date.UTC(desde.getFullYear(), desde.getMonth(), desde.getDate());
  return Math.round(ms / 86_400_000);
}

const capitalizar = (s: string) => s.charAt(0).toUpperCase() + s.slice(1);

/** "Hoy", "Ayer" o "Mar 23" para el feed de actividad. */
export function relativa(fechaIso: string, hoy = new Date()): string {
  const d = deIso(fechaIso);
  const dif = diasEntre(d, hoy);
  if (dif === 0) return 'Hoy';
  if (dif === 1) return 'Ayer';
  const dia = capitalizar(d.toLocaleDateString('es-ES', { weekday: 'short' }).replace('.', ''));
  return `${dia} ${d.getDate()}`;
}

/** "Martes 23" */
export function diaLargo(fechaIso: string): string {
  const d = deIso(fechaIso);
  return `${capitalizar(d.toLocaleDateString('es-ES', { weekday: 'long' }))} ${d.getDate()}`;
}

export function nombreMes(d: Date): string {
  return capitalizar(d.toLocaleDateString('es-ES', { month: 'long' }));
}

const eurFmt = (n: number, dec: number) =>
  n.toLocaleString('es-ES', { minimumFractionDigits: dec, maximumFractionDigits: dec });

/** 1.284,50 € */
export function eur(n: number, dec = 2): string {
  return `${eurFmt(n, dec)} €`;
}

export function num(n: number, dec = 0): string {
  return eurFmt(n, dec);
}
