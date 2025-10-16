import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'ERP Advocacia - Super Admin',
  description: 'Sistema de gerenciamento multi-tenant para escritórios de advocacia',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="pt-BR">
      <body>{children}</body>
    </html>
  );
}
