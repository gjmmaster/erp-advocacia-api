import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';

const API_URL = process.env.BACKEND_URL || process.env.NEXT_PUBLIC_API_URL || 'http://localhost:3000';

export async function GET(
  request: NextRequest,
  { params }: { params: { id: string } }
) {
  try {
    console.log('[MASTER USER] Tenant ID:', params.id);
    console.log('[MASTER USER] API_URL:', API_URL);
    
    const cookieStore = cookies();
    const authToken = cookieStore.get('auth-token')?.value;
    const accessToken = cookieStore.get('access_token')?.value;
    const token = accessToken || authToken;

    console.log('[MASTER USER] auth-token:', authToken ? 'SIM' : 'NÃO');
    console.log('[MASTER USER] access_token:', accessToken ? 'SIM' : 'NÃO');
    console.log('[MASTER USER] Token usado:', token ? token.substring(0, 20) + '...' : 'NENHUM');

    if (!token) {
      return NextResponse.json(
        { error: 'Não autenticado' },
        { status: 401 }
      );
    }

    const url = `${API_URL}/admin/tenants/${params.id}/master-user`;
    console.log('[MASTER USER] URL completa:', url);

    // Chamar backend para buscar master user
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
    });

    console.log('[MASTER USER] Resposta do backend:', response.status);

    if (!response.ok) {
      const error = await response.json();
      return NextResponse.json(error, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('Erro ao buscar master user:', error);
    return NextResponse.json(
      { error: 'Erro ao buscar master user' },
      { status: 500 }
    );
  }
}
