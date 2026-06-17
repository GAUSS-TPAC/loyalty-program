import createMiddleware from "next-intl/middleware";
import { routing } from "./i18n/routing";

export default createMiddleware(routing);

export const config = {
  // Match only internationalized pathnames, exclude /backend/* proxy paths
  matcher: ['/', '/(fr|en)/:path*', '/((?!backend|_next|_vercel|.*\\..*).*)',],
};
