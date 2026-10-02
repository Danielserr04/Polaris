// Service worker de Polaris: recibe los avisos del servidor (Web Push) aunque la app este
// cerrada y abre la pantalla que toca al pulsarlos. No guarda nada en cache: Polaris
// necesita el servidor para todo. Ver docs/decisiones/044-recordatorios.md.

self.addEventListener('install', () => self.skipWaiting());
self.addEventListener('activate', (e) => e.waitUntil(self.clients.claim()));

// Chrome exige un manejador de fetch para ofrecer instalar la app. Deja pasar todo.
self.addEventListener('fetch', () => {});

self.addEventListener('push', (e) => {
  let datos = {};
  try {
    datos = e.data ? e.data.json() : {};
  } catch {
    datos = { texto: e.data ? e.data.text() : '' };
  }
  const titulo = datos.titulo || 'Polaris';
  e.waitUntil(
    self.registration.showNotification(titulo, {
      body: datos.texto || '',
      icon: '/assets/app-icon-192.png',
      // Un aviso por tipo: el de mañana sustituye al de hoy si sigue en la bandeja.
      tag: datos.tipo ? 'polaris-' + datos.tipo : 'polaris',
      data: { enlace: datos.enlace || '/' },
    }),
  );
});

self.addEventListener('notificationclick', (e) => {
  e.notification.close();
  const destino = new URL(e.notification.data?.enlace || '/', self.location.origin).href;
  e.waitUntil(
    (async () => {
      const ventanas = await self.clients.matchAll({ type: 'window', includeUncontrolled: true });
      for (const v of ventanas) {
        if (new URL(v.url).origin === self.location.origin) {
          await v.focus();
          if ('navigate' in v) await v.navigate(destino);
          return;
        }
      }
      await self.clients.openWindow(destino);
    })(),
  );
});
