import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

export async function GET(request: NextRequest) {
  console.log('=== [BFF] GET /api/tenant/clientes INICIADO ===');
  
  try {
    const cookieStore = cookies();
    const token = cookieStore.get('access_token')?.value;

    console.log('[BFF] Token presente:', token ? 'SIM' : 'NÃO');

    if (!token) {
      console.log('[BFF] ❌ Token não encontrado');
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    const searchParams = request.nextUrl.searchParams;
    const page = searchParams.get('page') || '1';
    const perPage = searchParams.get('per-page') || '20';
    const search = searchParams.get('search');

    const params = new URLSearchParams();
    params.append('page', page);
    params.append('per-page', perPage);
    if (search) params.append('search', search);

    const backendUrl = `${BACKEND_URL}/api/tenant/clientes?${params.toString()}`;
    console.log('[BFF] BACKEND_URL:', BACKEND_URL);
    console.log('[BFF] URL completa:', backendUrl);
    console.log('[BFF] Token (primeiros 20 chars):', token.substring(0, 20));

    console.log('[BFF] Fazendo requisição ao backend...');
    const response = await fetch(backendUrl, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    });

    console.log('[BFF] Resposta do backend - Status:', response.status);
    console.log('[BFF] Resposta do backend - OK:', response.ok);

    if (!response.ok) {
      console.log('[BFF] ❌ Backend retornou erro');
      const error = await response.json();
      console.log('[BFF] Erro:', error);
      return NextResponse.json(error, { status: response.status });
    }

    const data = await response.json();
    console.log('[BFF] ✅ Dados recebidos do backend');
    console.log('[BFF] Total de clientes:', data.total);
    return NextResponse.json(data);
  } catch (error) {
    console.error('[BFF] ❌ EXCEÇÃO:', error);
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    );
  }
}

export async function POST(request: NextRequest) {
  console.log('=== [BFF] POST /api/tenant/clientes INICIADO ===');
  
  try {
    const cookieStore = cookies();
    const token = cookieStore.get('access_token')?.value;

    console.log('[BFF] Token presente:', token ? 'SIM' : 'NÃO');

    if (!token) {
      console.log('[BFF] ❌ Token não encontrado');
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    const body = await request.json();
    console.log('[BFF] Body recebido:', body);

    const backendUrl = `${BACKEND_URL}/api/tenant/clientes`;
    console.log('[BFF] BACKEND_URL:', BACKEND_URL);
    console.log('[BFF] URL completa:', backendUrl);
    console.log('[BFF] Token (primeiros 20 chars):', token.substring(0, 20));

    console.log('[BFF] Fazendo requisição POST ao backend...');
    const response = await fetch(backendUrl, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    console.log('[BFF] Resposta do backend - Status:', response.status);
    console.log('[BFF] Resposta do backend - OK:', response.ok);

    if (!response.ok) {
      console.log('[BFF] ❌ Backend retornou erro');
      const error = await response.json();
      console.log('[BFF] Erro:', error);
      return NextResponse.json(error, { status: response.status });
    }

    const data = await response.json();
    console.log('[BFF] ✅ Cliente criado com sucesso!');
    console.log('[BFF] Cliente ID:', data.id);
    return NextResponse.json(data, { status: 201 });
  } catch (error) {
    console.error('[BFF] ❌ EXCEÇÃO:', error);
    return NextResponse.json(
      { error: 'Internal server error', message: error instanceof Error ? error.message : 'Unknown error' },
      { status: 500 }
    );
  }
}
