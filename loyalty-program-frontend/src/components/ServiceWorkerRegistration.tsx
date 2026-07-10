"use client";

import { useEffect } from "react";

// Service workers and Turbopack's dev-mode HMR conflict (the SW's fetch
// interception can shadow the JS/CSS updates being iterated on), so this only
// registers in production builds — test offline support via `next build && next start`.
export function ServiceWorkerRegistration() {
  useEffect(() => {
    if (process.env.NODE_ENV !== "production") return;
    if (!("serviceWorker" in navigator)) return;

    navigator.serviceWorker
      .register("/sw.js", { scope: "/", updateViaCache: "none" })
      .catch(console.error);
  }, []);

  return null;
}
