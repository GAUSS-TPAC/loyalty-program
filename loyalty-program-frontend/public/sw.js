// Hand-rolled service worker (no Workbox/Serwist — Turbopack has no webpack-based
// precache-manifest injection step available). Two caches, bumped together on any
// logic change below so `activate` can purge stale entries.
const SHELL_CACHE = "loyalty-shell-v1";
const API_CACHE = "loyalty-api-v1";
const OFFLINE_URL = "/offline.html";

self.addEventListener("install", (event) => {
  event.waitUntil(
    caches.open(SHELL_CACHE).then((cache) => cache.add(OFFLINE_URL))
  );
  self.skipWaiting();
});

self.addEventListener("activate", (event) => {
  const keep = new Set([SHELL_CACHE, API_CACHE]);
  event.waitUntil(
    caches
      .keys()
      .then((keys) =>
        Promise.all(keys.filter((key) => !keep.has(key)).map((key) => caches.delete(key)))
      )
      .then(() => self.clients.claim())
  );
});

self.addEventListener("fetch", (event) => {
  const { request } = event;

  // Never intercept writes: let them fail/succeed exactly as the app expects,
  // so a queued offline event is never silently swallowed or duplicated here.
  if (request.method !== "GET") return;

  const url = new URL(request.url);

  // Full-page navigations: network-first, fall back to the static offline page.
  if (request.mode === "navigate") {
    event.respondWith(
      fetch(request).catch(() => caches.match(OFFLINE_URL))
    );
    return;
  }

  // Backend API reads: network-first with cache fallback, so the dashboard
  // shows fresh data whenever possible and last-known-good data when offline.
  if (url.pathname.startsWith("/backend/")) {
    event.respondWith(
      fetch(request)
        .then((response) => {
          const copy = response.clone();
          caches.open(API_CACHE).then((cache) => cache.put(request, copy));
          return response;
        })
        .catch(() => caches.match(request))
    );
    return;
  }

  // Hashed Next.js static assets and other same-origin static files: cache-first,
  // populated on first fetch. Safe because these are content-hashed/immutable.
  if (url.origin === self.location.origin) {
    event.respondWith(
      caches.match(request).then((cached) => {
        if (cached) return cached;
        return fetch(request).then((response) => {
          const copy = response.clone();
          caches.open(SHELL_CACHE).then((cache) => cache.put(request, copy));
          return response;
        });
      })
    );
  }
});
