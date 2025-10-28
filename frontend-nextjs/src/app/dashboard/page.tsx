import { getSession } from '@/lib/auth';
import { redirect, headers } from 'next/navigation';
import DashboardLayout from '@/components/tenant/DashboardLayout';
import DashboardStats from '@/components/tenant/DashboardStats';

export default async function TenantDashboardPage() {
  const session = await getSession();

  if (!session) {
    redirect('/login');
  }

  // Obter nome do tenant dos headers
  const headersList = headers();
  const tenantName = headersList.get('x-tenant-name') || undefined;

  return (
    <DashboardLayout user={session} tenantName={tenantName}>
      <h1 style={{ fontSize: '28px', fontWeight: '700', marginBottom: '24px', color: '#1a202c' }}>
        Dashboard
      </h1>
      <DashboardStats tenantId={session['tenant-id']} />
    </DashboardLayout>
  );
}
