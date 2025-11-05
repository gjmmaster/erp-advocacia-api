/** @type {import('next').NextConfig} */
const nextConfig = {
  // Removido 'output: standalone' pois não está sendo usado corretamente
  // e estava causando problemas com API routes
  env: {
    BACKEND_API_URL: process.env.BACKEND_API_URL,
  },
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: '/api/:path*',
      },
    ];
  },
};

module.exports = nextConfig;
