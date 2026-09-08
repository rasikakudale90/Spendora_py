// Spendora Progressive Web App Service Worker
const CACHE_NAME = "spendora-pwa-v1";
const STATIC_CACHE = "spendora-static-v1";
const DATA_CACHE = "spendora-data-v1";

const PRECACHE_ASSETS = [
  "/",
  "/dashboard",
  "/expenses",
  "/manifest.json",
  "/favicon.svg",
  "/apple-touch-icon.png",
  "/icons/icon-192x192.png",
  "/icons/icon-512x512.png",
  "/icons/icon-maskable-512x512.png",
  "/icons/icon.svg",
];

// Install Event — Cache Core App Shell
self.addEventListener("install", (event) => {
  event.waitUntil(
    caches
      .open(STATIC_CACHE)
      .then((cache) => {
        return cache.addAll(PRECACHE_ASSETS);
      })
      .then(() => self.skipWaiting())
      .catch((err) => {
        console.warn("[Spendora SW] Precaching notice:", err);
      })
  );
});

// Activate Event — Cleanup Old Caches
self.addEventListener("activate", (event) => {
  const currentCaches = [STATIC_CACHE, DATA_CACHE, CACHE_NAME];
  event.waitUntil(
    caches
      .keys()
      .then((cacheNames) => {
        return Promise.all(
          cacheNames.map((cacheName) => {
            if (!currentCaches.includes(cacheName)) {
              return caches.delete(cacheName);
            }
          })
        );
      })
      .then(() => self.clients.claim())
  );
});

// Fetch Event — Smart Network-First & Stale-While-Revalidate Strategies
self.addEventListener("fetch", (event) => {
  const { request } = event;
  const url = new URL(request.url);

  // Ignore non-HTTP/HTTPS requests (like chrome-extension://)
  if (!url.protocol.startsWith("http")) return;

  // 1. Next.js Static Chunks & Public Assets: Cache-First / Stale-While-Revalidate
  if (
    url.pathname.startsWith("/_next/static/") ||
    url.pathname.startsWith("/icons/") ||
    url.pathname.endsWith(".svg") ||
    url.pathname.endsWith(".png") ||
    url.pathname.endsWith(".woff2")
  ) {
    event.respondWith(
      caches.open(STATIC_CACHE).then((cache) => {
        return cache.match(request).then((cachedResponse) => {
          const fetchPromise = fetch(request)
            .then((networkResponse) => {
              if (networkResponse && networkResponse.status === 200) {
                cache.put(request, networkResponse.clone());
              }
              return networkResponse;
            })
            .catch(() => cachedResponse);

          return cachedResponse || fetchPromise;
        });
      })
    );
    return;
  }

  // 2. API Calls (/api/v1/): Network-First with Data Cache Fallback
  if (url.pathname.startsWith("/api/v1/")) {
    if (request.method === "GET") {
      event.respondWith(
        fetch(request)
          .then((networkResponse) => {
            if (networkResponse && networkResponse.status === 200) {
              const responseClone = networkResponse.clone();
              caches.open(DATA_CACHE).then((cache) => {
                cache.put(request, responseClone);
              });
            }
            return networkResponse;
          })
          .catch(() => {
            return caches.match(request).then((cachedResponse) => {
              if (cachedResponse) return cachedResponse;
              return new Response(
                JSON.stringify({ error: "Offline mode: Network unavailable" }),
                {
                  status: 503,
                  headers: { "Content-Type": "application/json" },
                }
              );
            });
          })
      );
    }
    return;
  }

  // 3. HTML Navigation / Page Routes: Network-First falling back to Pre-cached Pages
  if (request.mode === "navigate") {
    event.respondWith(
      fetch(request)
        .then((networkResponse) => {
          if (networkResponse && networkResponse.status === 200) {
            const responseClone = networkResponse.clone();
            caches.open(STATIC_CACHE).then((cache) => {
              cache.put(request, responseClone);
            });
          }
          return networkResponse;
        })
        .catch(() => {
          return caches.match(request).then((cached) => {
            if (cached) return cached;
            return caches.match("/dashboard");
          });
        })
    );
    return;
  }

  // Default: Network with Cache Fallback
  event.respondWith(
    fetch(request).catch(() => caches.match(request))
  );
});

// Listen for message events (e.g. skipWaiting)
self.addEventListener("message", (event) => {
  if (event.data && event.data.type === "SKIP_WAITING") {
    self.skipWaiting();
  }
});
