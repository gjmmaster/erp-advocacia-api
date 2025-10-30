import { getSession } from '@/lib/auth';
import { redirect } from 'next/navigation';
import { headers } from 'next/headers';
import DashboardLayout from '@/components/tenant/DashboardLayout';
import DashboardStats from '@/components/tenant/DashboardStats';

export default async function TenantDashboardPage() {
  const session = await getSession();

  if (!session) {
    redirect('/login');
  }

  // Obter nome do tenant dos headers
  const headersList = headers();
  const tenantName = headersList.get('x-tenant-name') || 'Escritório';

  // Garantir que tenant-id é número
  const tenantId = typeof session['tenant-id'] === 'number' 
    ? session['tenant-id'] 
    : parseInt(String(session['tenant-id']));

  // Verificar se está em modo impersonation
  const impersonating = session.impersonating === true;
  const impersonatorEmail = session['impersonator-email'];

  return (
    <DashboardLayout 
      user={session as any} 
      tenantName={tenantName}
      impersonating={impersonating}
      impersonatorEmail={impersonatorEmail}
    >
      <h1 style={{ fontSize: '28px', fontWeight: '700', marginBottom: '24px', color: '#1a202c' }}>
        Dashboard
      </h1>
      <DashboardStats tenantId={tenantId} />
    </DashboardLayout>
  );
}
