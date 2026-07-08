import path from "path";
import type { NextConfig } from "next";
import createNextIntlPlugin from "next-intl/plugin";

const withNextIntl = createNextIntlPlugin("./src/i18n/request.ts");

const nextConfig: NextConfig = {
  output: "standalone",
  turbopack: {
    root: path.join(__dirname),
  },
  async rewrites() {
    return [
      {
        source: "/backend/:path*",
        destination: `${process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8081"}/:path*`,
      },
    ];
  },
};

export default withNextIntl(nextConfig);
