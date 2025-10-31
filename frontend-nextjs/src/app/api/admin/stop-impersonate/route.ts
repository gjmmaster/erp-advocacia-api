import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';

const API_URL = process.env.BACKEND_URL || process.env.NEXT_PUBLIC_API_URL || 'http://localhost:3000';

export async function POST(request: NextRequest) {
  try {
    console.log('[STOP IMPERSONATE] Parando impersonation');
    console.log('[STOP IMPERSONATE] API_URL:', API_URL);
    
    const cookieStore = cookies();
    const authToken = cookieStore.get('auth-token')?.value;
    const accessToken = cookieStore.get('access_token')?.value;
    const token = accessToken || authToken;

    console.log('[STOP IMPERSONATE] Token presente:', token ? 'SIM' : 'NÃO');

    if (!token) {
      return NextResponse.json(
        { error: 'Não autenticado' },
        { status: 401 }
      );
    }

    const url = `${API_URL}/admin/stop-impersonate`;
    console.log('[STOP IMPERSONATE] URL completa:', url);

    // Chamar backend para parar impersonation
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
    });

    console.log('[STOP IMPERSONATE] Resposta do backend:', response.status);

    if (!response.ok) {
      const error = await response.json();
      console.log('[STOP IMPERSONATE] Erro do backend:', error);
      return NextResponse.json(error, { status: response.status });
    }

    const data = await response.json();
    console.log('[STOP IMPERSONATE] Token do super admin recebido:', data.token ? 'SIM' : 'NÃO');

    // Atualizar todos os cookies com token do super admin
    const responseObj = NextResponse.json(data);
    
    const cookieOptions = {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'lax' as const,
      maxAge: 3600,
      path: '/',
    };
    
    responseObj.cookies.set('auth-token', data.token, cookieOptions);
    responseObj.cookies.set('access_token', data.token, cookieOptions);
    responseObj.cookies.set('refresh_token', data.token, cookieOptions);

    console.log('[STOP IMPERSONATE] Cookies atualizados com token do super admin');

    return responseObj;
  } catch (error) {
    console.error('Erro ao parar impersonation:', error);
    return NextResponse.json(
      { error: 'Erro ao parar impersonation' },
      { status: 500 }
    );
  }
}
