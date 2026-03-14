import type { NextConfig } from "next";

function resolveApiURL() {
  const raw = process.env.API_URL;
  if (!raw) {
    return null;
  }

  return raw.replace(/\/+$/, "");
}

const nextConfig: NextConfig = {
  reactCompiler: true,
  async rewrites() {
    const apiURL = resolveApiURL();

    if (!apiURL) {
      return [];
    }

    return [
      {
        source: "/api/:path*",
        destination: `${apiURL}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
