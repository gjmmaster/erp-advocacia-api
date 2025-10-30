import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:3000';

export async function POST(request: NextRequest) {
  console.log('[API CHANGE PASSWORD] Iniciando troca de senha');
  
  try {
    const body = await request.json();
    const cookieStore = cookies();
    const accessToken = cookieStore.get('access_token')?.value;

    if (!accessToken) {
      console.log('[API CHANGE PASSWORD] Token não encontrado');
      return NextResponse.json(
        { success: false, error: 'Não autenticado' },
        { status: 401 }
      );
    }

    // Validação básica
    const { current_password, new_password, confirm_password } = body;

    if (!current_password || !new_password || !confirm_password) {
      console.log('[API CHANGE PASSWORD] Campos obrigatórios faltando');
      return NextResponse.json(
        { success: false, error: 'Todos os campos são obrigatórios' },
        { status: 400 }
      );
    }

    if (new_password !== confirm_password) {
      console.log('[API CHANGE PASSWORD] Senhas não coincidem');
      return NextResponse.json(
        { success: false, error: 'As senhas não coincidem' },
        { status: 400 }
      );
    }

    console.log('[API CHANGE PASSWORD] Fazendo proxy para backend');

    // Proxy para backend
    const response = await fetch(`${BACKEND_URL}/api/auth/change-password`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        current_password,
        new_password,
        confirm_password,
      }),
    });

    const data = await response.json();
    console.log('[API CHANGE PASSWORD] Resposta do backend:', response.status);

    if (!response.ok) {
      console.log('[API CHANGE PASSWORD] Erro do backend:', data);
      return NextResponse.json(data, { status: response.status });
    }

    // Atualizar cookie com novo token
    if (data.token) {
      console.log('[API CHANGE PASSWORD] Atualizando cookie com novo token');
      
      const cookieOptions = {
        httpOnly: true,
        secure: process.env.NODE_ENV === 'production',
        sameSite: 'lax' as const,
        maxAge: 60 * 60 * 24 * 7, // 7 dias
        path: '/',
      };

      cookieStore.set('access_token', data.token, cookieOptions);
    }

    console.log('[API CHANGE PASSWORD] Senha alterada com sucesso');
    return NextResponse.json(data);
  } catch (error) {
    console.error('[API CHANGE PASSWORD ERROR]', error);
    return NextResponse.json(
      { success: false, error: 'Erro ao alterar senha. Tente novamente.' },
      { status: 500 }
    );
  }
}
