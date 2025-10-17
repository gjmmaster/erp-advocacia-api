import { NextRequest, NextResponse } from 'next/server';
import { fetchBackend } from '@/lib/api';
import { setSession } from '@/lib/auth';

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();

    if (!body.email || !body.password) {
      return NextResponse.json(
        { error: 'E-mail e senha são obrigatórios' },
        { status: 400 }
      );
    }

    const response = await fetchBackend('/admin/login', {
      method: 'POST',
      body: JSON.stringify({
        email: body.email,
        password: body.password,
      }),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ error: 'Credenciais inválidas' }));
      return NextResponse.json(
        { error: errorData.error || 'Credenciais inválidas' },
        { status: response.status }
      );
    }

    const data = await response.json();

    if (!data.token) {
      return NextResponse.json(
        { error: 'Token não recebido do servidor' },
        { status: 500 }
      );
    }

    await setSession(data.token);

    return NextResponse.json({
      message: data.message || 'Login realizado com sucesso',
      user: {
        email: body.email,
        role: 'super-admin',
      },
    });
  } catch (error) {
    console.error('Erro no login:', error);
    
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
