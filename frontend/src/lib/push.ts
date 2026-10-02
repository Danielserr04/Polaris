import { borrarSuscripcionPush, guardarSuscripcionPush, obtenerClavePush } from '../api/recordatorios';

// Avisos al movil (Web Push). El service worker (public/sw.js) los recibe aunque Polaris
// este cerrado. Funciona en Android y en ordenador; en iPhone solo con Polaris añadido a la
// pantalla de inicio (iOS 16.4 o mas). Siempre hace falta HTTPS, salvo en localhost.

export type EstadoPush =
  | 'no-soportado' // navegador sin push, o iPhone sin instalar la app
  | 'bloqueado' // el usuario denego el permiso: solo se cambia en los ajustes del navegador
  | 'activo'
  | 'inactivo';

export function pushSoportado(): boolean {
  return typeof window !== 'undefined' && 'serviceWorker' in navigator && 'PushManager' in window && 'Notification' in window;
}

/** iPhone/iPad sin Polaris en la pantalla de inicio: ahi Safari no ofrece push. */
export function esIosSinInstalar(): boolean {
  const ios = /iphone|ipad|ipod/i.test(navigator.userAgent);
  const instalada =
    window.matchMedia?.('(display-mode: standalone)').matches ||
    (navigator as Navigator & { standalone?: boolean }).standalone === true;
  return ios && !instalada;
}

/** Se registra al arrancar: sin el, ni push ni app instalable. */
export function registrarServiceWorker(): void {
  if (!('serviceWorker' in navigator)) return;
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js').catch(() => {
      // Sin HTTPS o en un navegador que lo bloquea: la app funciona igual, sin push.
    });
  });
}

async function registro(): Promise<ServiceWorkerRegistration> {
  const existente = await navigator.serviceWorker.getRegistration();
  return existente ?? navigator.serviceWorker.register('/sw.js');
}

export async function estadoPush(): Promise<EstadoPush> {
  if (!pushSoportado()) return 'no-soportado';
  if (Notification.permission === 'denied') return 'bloqueado';
  try {
    const reg = await navigator.serviceWorker.getRegistration();
    const sub = await reg?.pushManager.getSubscription();
    return sub && Notification.permission === 'granted' ? 'activo' : 'inactivo';
  } catch {
    return 'inactivo';
  }
}

function base64UrlABytes(b64: string): Uint8Array<ArrayBuffer> {
  const relleno = '='.repeat((4 - (b64.length % 4)) % 4);
  const bin = atob((b64 + relleno).replace(/-/g, '+').replace(/_/g, '/'));
  const bytes = new Uint8Array(new ArrayBuffer(bin.length));
  for (let i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i);
  return bytes;
}

function bytesABase64Url(buf: ArrayBuffer | null): string {
  if (!buf) return '';
  let bin = '';
  new Uint8Array(buf).forEach((b) => (bin += String.fromCharCode(b)));
  return btoa(bin).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

/** Pide permiso, suscribe este dispositivo y lo guarda en el backend. */
export async function activarPush(): Promise<EstadoPush> {
  if (!pushSoportado()) return 'no-soportado';
  const permiso = await Notification.requestPermission();
  if (permiso === 'denied') return 'bloqueado';
  if (permiso !== 'granted') return 'inactivo';

  const reg = await registro();
  await navigator.serviceWorker.ready;
  const { clavePublica } = await obtenerClavePush();
  let sub = await reg.pushManager.getSubscription();
  // Una suscripcion hecha con otra clave (el servidor cambio de claves) ya no sirve.
  const clave = base64UrlABytes(clavePublica);
  const actual = sub?.options.applicationServerKey;
  if (sub && actual && bytesABase64Url(actual) !== clavePublica) {
    await sub.unsubscribe();
    sub = null;
  }
  sub ??= await reg.pushManager.subscribe({ userVisibleOnly: true, applicationServerKey: clave });
  await guardarSuscripcionPush({
    endpoint: sub.endpoint,
    p256dh: bytesABase64Url(sub.getKey('p256dh')),
    auth: bytesABase64Url(sub.getKey('auth')),
  });
  return 'activo';
}

/** Deja de recibir avisos en este dispositivo (los demas siguen). */
export async function desactivarPush(): Promise<EstadoPush> {
  if (!pushSoportado()) return 'no-soportado';
  const reg = await navigator.serviceWorker.getRegistration();
  const sub = await reg?.pushManager.getSubscription();
  if (sub) {
    await borrarSuscripcionPush(sub.endpoint).catch(() => undefined);
    await sub.unsubscribe();
  }
  return 'inactivo';
}
