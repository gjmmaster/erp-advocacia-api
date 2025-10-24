import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';
import { fetchBackend } from '@/lib/api';
import { getSession } from '@/lib/auth';

export async function GET(request: NextRequest) {
  try {
    console.log('[API TENANTS] GET /api/admin/tenants chamado');
    const session = await getSession();
    console.log('[API TENANTS] Sessão obtida:', session ? 'SIM' : 'NÃO');
    console.log('[API TENANTS] Role da sessão:', session?.role);
    
    if (!session || session.role !== 'super-admin') {
      console.log('[API TENANTS] Acesso negado - sessão inválida ou role incorreta');
      return NextResponse.json(
        { error: 'Acesso negado' },
        { status: 403 }
      );
    }

    console.log('[API TENANTS] Fazendo requisição ao backend');
    
    // Obter o token dos cookies para enviar ao backend
    const cookieStore = cookies();
    const accessToken = cookieStore.get('access_token')?.value;
    console.log('[API TENANTS] Token para backend:', accessToken ? 'SIM' : 'NÃO');
    
    const response = await fetchBackend('/admin/tenants', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
      },
    });
    console.log('[API TENANTS] Resposta do backend:', response.status);

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ error: 'Falha ao carregar tenants' }));
      return NextResponse.json(
        { error: errorData.error || 'Falha ao carregar tenants' },
        { status: response.status }
      );
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('Erro ao listar tenants:', error);
    
    if (error instanceof Error) {
      if (error.message === 'Request timeout') {
        return NextResponse.json(
          { error: 'Timeout na conexão com o servidor' },
          { status: 504 }
        );
      }
    }

    return NextResponse.json(
      { error: 'Erro interno do servidor' },
      { status: 500 }
    );
  }
}

export async function POST(request: NextRequest) {
  try {
    const session = await getSession();
    if (!session || session.role !== 'super-admin') {
      return NextResponse.json(
        { error: 'Acesso negado' },
        { status: 403 }
      );
    }

    const body = await request.json();

    if (!body.company_name || !body.email) {
      return NextResponse.json(
        { error: 'Nome da empresa e e-mail são obrigatórios' },
        { status: 400 }
      );
    }

    // Obter o token dos cookies para enviar ao backend
    const cookieStore = cookies();
    const accessToken = cookieStore.get('access_token')?.value;
    
    const response = await fetchBackend('/admin/tenants', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
      },
      body: JSON.stringify({
        company_name: body.company_name,
        email: body.email,
        operator_limit: body.operator_limit || 4,
      }),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ error: 'Falha ao criar tenant' }));
      return NextResponse.json(
        { error: errorData.error || 'Falha ao criar tenant' },
        { status: response.status }
      );
    }

    const data = await response.json();
    return NextResponse.json(data, { status: 201 });
  } catch (error) {
    console.error('Erro ao criar tenant:', error);
    
    if (error instanceof Error) {
      if (error.message === 'Request timeout') {
        return NextResponse.json(
          { error: 'Timeout na conexão com o servidor' },
          { status: 504 }
        );
      }
    }

    return NextResponse.json(
      { error: 'Erro interno do servidor' },
      { status: 500 }
    );
  }
}
