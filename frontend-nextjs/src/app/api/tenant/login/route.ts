import { NextRequest, NextResponse } from 'next/server';
import { setSession } from '@/lib/auth';

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { email, password } = body;

    // Extrair subdomain e tenant-id dos headers (adicionados pelo middleware)
    const subdomain = request.headers.get('x-tenant-subdomain');
    const tenantId = request.headers.get('x-tenant-id');

    console.log('[TENANT LOGIN] Subdomain:', subdomain);
    console.log('[TENANT LOGIN] Tenant ID:', tenantId);

    if (!subdomain || !tenantId) {
      console.error('[TENANT LOGIN] Tenant não identificado');
      return NextResponse.json(
        { error: 'Tenant não identificado' },
        { status: 400 }
      );
    }

    if (!email || !password) {
      return NextResponse.json(
        { error: 'E-mail e senha são obrigatórios' },
        { status: 400 }
      );
    }

    // Fazer login no backend
    const backendUrl = process.env.BACKEND_URL || 'http://localhost:3000';
    console.log('[TENANT LOGIN] Fazendo requisição ao backend:', backendUrl);

    const response = await fetch(`${backendUrl}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email,
        password,
        subdomain, // Backend valida que usuário pertence a este tenant
      }),
    });

    console.log('[TENANT LOGIN] Resposta do backend:', response.status);

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ 
        error: 'Credenciais inválidas' 
      }));
      console.error('[TENANT LOGIN] Erro do backend:', errorData);
      return NextResponse.json(
        { error: errorData.error || 'Credenciais inválidas' },
        { status: response.status }
      );
    }

    const data = await response.json();
    console.log('[TENANT LOGIN] Token recebido:', data.token ? 'SIM' : 'NÃO');

    if (!data.token) {
      console.error('[TENANT LOGIN] Token não recebido');
      return NextResponse.json(
        { error: 'Token não recebido do servidor' },
        { status: 500 }
      );
    }

    // Validar que o tenant-id do token corresponde ao subdomínio
    try {
      const tokenPayload = JSON.parse(
        Buffer.from(data.token.split('.')[1], 'base64').toString()
      );
      
      console.log('[TENANT LOGIN] Token tenant-id:', tokenPayload['tenant-id']);
      console.log('[TENANT LOGIN] Header tenant-id:', tenantId);

      if (tokenPayload['tenant-id'] !== parseInt(tenantId)) {
        console.error('[TENANT LOGIN] Tenant-id não corresponde');
        return NextResponse.json(
          { error: 'Usuário não pertence a este escritório' },
          { status: 403 }
        );
      }
    } catch (error) {
      console.error('[TENANT LOGIN] Erro ao validar token:', error);
      return NextResponse.json(
        { error: 'Erro ao validar token' },
        { status: 500 }
      );
    }

    // Armazenar token em cookie HttpOnly
    console.log('[TENANT LOGIN] Definindo sessão');
    await setSession(data.token);

    console.log('[TENANT LOGIN] Login bem-sucedido');
    return NextResponse.json({
      message: 'Login realizado com sucesso',
    });
  } catch (error) {
    console.error('[TENANT LOGIN] Erro:', error);
    
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
