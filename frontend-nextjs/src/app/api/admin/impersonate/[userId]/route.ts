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
    const authToken = cookieStore.get('auth-token')?.value;
    const accessToken = cookieStore.get('access_token')?.value;
    const token = accessToken || authToken;

    console.log('[IMPERSONATE] auth-token:', authToken ? 'SIM' : 'NÃO');
    console.log('[IMPERSONATE] access_token:', accessToken ? 'SIM' : 'NÃO');
    console.log('[IMPERSONATE] Token usado:', token ? token.substring(0, 20) + '...' : 'NENHUM');

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

    // Atualizar cookies com novo token (ambos auth-token e access_token)
    const responseObj = NextResponse.json(data);
    
    const cookieOptions = {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'lax' as const,
      maxAge: 3600, // 1 hora
      path: '/',
    };
    
    responseObj.cookies.set('auth-token', data.token, cookieOptions);
    responseObj.cookies.set('access_token', data.token, cookieOptions);
    responseObj.cookies.set('refresh_token', data.token, cookieOptions);

    console.log('[IMPERSONATE] Cookies atualizados com novo token');

    return responseObj;
  } catch (error) {
    console.error('Erro ao iniciar impersonation:', error);
    return NextResponse.json(
      { error: 'Erro ao iniciar impersonation' },
      { status: 500 }
    );
  }
}
