import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';

const API_URL = process.env.BACKEND_URL || process.env.NEXT_PUBLIC_API_URL || 'http://localhost:3000';

export async function POST(
  request: NextRequest,
  { params }: { params: { userId: string } }
) {
  try {
    console.log('[IMPERSONATE] Iniciando impersonation para user:', params.userId);
    console.log('[IMPERSONATE] API_URL:', API_URL);
    
    const cookieStore = cookies();
    const token = cookieStore.get('auth-token')?.value || cookieStore.get('access_token')?.value;

    console.log('[IMPERSONATE] Token presente:', token ? 'SIM' : 'NÃO');

    if (!token) {
      console.log('[IMPERSONATE] ERRO: Sem token');
      return NextResponse.json(
        { error: 'Não autenticado' },
        { status: 401 }
      );
    }

    const url = `${API_URL}/admin/impersonate/${params.userId}`;
    console.log('[IMPERSONATE] Chamando backend:', url);

    // Chamar backend para iniciar impersonation
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
    });

    console.log('[IMPERSONATE] Resposta do backend:', response.status);

    if (!response.ok) {
      const error = await response.json();
      console.log('[IMPERSONATE] Erro do backend:', error);
      return NextResponse.json(error, { status: response.status });
    }

    const data = await response.json();
    console.log('[IMPERSONATE] Token recebido:', data.token ? 'SIM' : 'NÃO');

    // Atualizar cookie com novo token
    const responseObj = NextResponse.json(data);
    responseObj.cookies.set('auth-token', data.token, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'lax',
      maxAge: 3600, // 1 hora
      path: '/',
    });

    return responseObj;
  } catch (error) {
    console.error('Erro ao iniciar impersonation:', error);
    return NextResponse.json(
      { error: 'Erro ao iniciar impersonation' },
      { status: 500 }
    );
  }
}
