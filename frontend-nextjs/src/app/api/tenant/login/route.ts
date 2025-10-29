import { NextRequest, NextResponse } from 'next/server';
import { setSession } from '@/lib/auth';

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    console.log('[TENANT LOGIN] Tentativa de login:', body.email);

    if (!body.email || !body.password) {
      return NextResponse.json(
        { error: 'E-mail e senha são obrigatórios' },
        { status: 400 }
      );
    }

    // Fazer login no backend (auto-descoberta)
    const backendUrl = process.env.BACKEND_URL || 'http://localhost:3000';
    console.log('[TENANT LOGIN] Fazendo requisição ao backend:', backendUrl);

    const response = await fetch(`${backendUrl}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: body.email,
        password: body.password,
      }),
    });

    console.log('[TENANT LOGIN] Resposta do backend:', response.status);

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ 
        error: 'Credenciais inválidas' 
      }));
      console.log('[TENANT LOGIN] Erro do backend:', errorData);
      return NextResponse.json(
        { error: errorData.error || 'Credenciais inválidas' },
        { status: response.status }
      );
    }

    const data = await response.json();
    console.log('[TENANT LOGIN] Token recebido:', data.token ? 'SIM' : 'NÃO');
    console.log('[TENANT LOGIN] Dados do usuário:', data.user);

    if (!data.token) {
      return NextResponse.json(
        { error: 'Token não recebido do servidor' },
        { status: 500 }
      );
    }

    // Armazenar token em cookie HttpOnly
    await setSession(data.token);
    console.log('[TENANT LOGIN] Sessão criada com sucesso');

    return NextResponse.json({
      message: 'Login realizado com sucesso',
      user: data.user,
    });
  } catch (error) {
    console.error('[TENANT LOGIN] Erro:', error);
    
    if (error instanceof Error) {
      if (error.message.includes('ECONNREFUSED')) {
        return NextResponse.json(
          { error: 'Servidor indisponível. Aguarde 60 segundos e tente novamente.' },
          { status: 503 }
        );
      }
    }

    return NextResponse.json(
      { error: 'Erro interno do servidor' },
      { status: 500 }
    );
  }
}
