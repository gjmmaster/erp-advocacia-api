import { NextRequest, NextResponse } from 'next/server';
import { fetchBackend } from '@/lib/api';
import { setSession } from '@/lib/auth';

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    console.log('[LOGIN] Tentativa de login:', body.email);

    if (!body.email || !body.password) {
      console.log('[LOGIN] Erro: campos obrigatórios faltando');
      return NextResponse.json(
        { error: 'E-mail e senha são obrigatórios' },
        { status: 400 }
      );
    }

    console.log('[LOGIN] Fazendo requisição ao backend:', process.env.BACKEND_API_URL);
    const response = await fetchBackend('/admin/login', {
      method: 'POST',
      body: JSON.stringify({
        email: body.email,
        password: body.password,
      }),
    });

    console.log('[LOGIN] Resposta do backend:', response.status);

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ error: 'Credenciais inválidas' }));
      console.log('[LOGIN] Erro do backend:', errorData);
      return NextResponse.json(
        { error: errorData.error || 'Credenciais inválidas' },
        { status: response.status }
      );
    }

    const data = await response.json();
    console.log('[LOGIN] Token recebido do backend:', data.token ? 'SIM' : 'NÃO');

    if (!data.token) {
      console.log('[LOGIN] Erro: token não recebido do backend');
      return NextResponse.json(
        { error: 'Token não recebido do servidor' },
        { status: 500 }
      );
    }

    console.log('[LOGIN] Definindo sessão com token do backend');
    await setSession(data.token);
    console.log('[LOGIN] Sessão definida com sucesso');

    return NextResponse.json({
      message: data.message || 'Login realizado com sucesso',
      user: {
        email: body.email,
        role: 'super-admin',
      },
    });
  } catch (error) {
    console.error('[LOGIN] Erro no login:', error);
    
    if (error instanceof Error) {
      if (error.message === 'Request timeout') {
        return NextResponse.json(
          { error: 'Timeout na conexão com o servidor' },
          { status: 504 }
        );
      }
      
      if (error.message.includes('ECONNREFUSED')) {
        return NextResponse.json(
          { error: 'Servidor indisponível' },
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
