import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';
import { fetchBackend } from '@/lib/api';
import { getSession } from '@/lib/auth';

interface RouteParams {
  params: {
    id: string;
  };
}

export async function GET(request: NextRequest, { params }: RouteParams) {
  try {
    const session = await getSession();
    if (!session || session.role !== 'super-admin') {
      return NextResponse.json(
        { error: 'Acesso negado' },
        { status: 403 }
      );
    }

    const { id } = params;

    if (!id || id === 'undefined') {
      return NextResponse.json(
        { error: 'ID do tenant é obrigatório' },
        { status: 400 }
      );
    }

    const cookieStore = cookies();
    const accessToken = cookieStore.get('access_token')?.value;
    
    const response = await fetchBackend(`/admin/tenants/${id}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
      },
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ error: 'Tenant não encontrado' }));
      return NextResponse.json(
        { error: errorData.error || 'Tenant não encontrado' },
        { status: response.status }
      );
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('Erro ao obter tenant:', error);
    
    return NextResponse.json(
      { error: 'Erro interno do servidor' },
      { status: 500 }
    );
  }
}

export async function PUT(request: NextRequest, { params }: RouteParams) {
  try {
    const session = await getSession();
    if (!session || session.role !== 'super-admin') {
      return NextResponse.json(
        { error: 'Acesso negado' },
        { status: 403 }
      );
    }

    const { id } = params;
    const body = await request.json();

    if (!id || id === 'undefined') {
      return NextResponse.json(
        { error: 'ID do tenant é obrigatório' },
        { status: 400 }
      );
    }

    if (!body.company_name && !body.operator_limit) {
      return NextResponse.json(
        { error: 'Pelo menos um campo deve ser fornecido para atualização' },
        { status: 400 }
      );
    }

    const updateData: any = {};
    if (body.company_name) updateData.company_name = body.company_name;
    if (body.operator_limit) updateData.operator_limit = body.operator_limit;

    const cookieStore = cookies();
    const accessToken = cookieStore.get('access_token')?.value;
    
    const response = await fetchBackend(`/admin/tenants/${id}`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
      },
      body: JSON.stringify(updateData),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ error: 'Falha ao atualizar tenant' }));
      return NextResponse.json(
        { error: errorData.error || 'Falha ao atualizar tenant' },
        { status: response.status }
      );
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('Erro ao atualizar tenant:', error);
    
    return NextResponse.json(
      { error: 'Erro interno do servidor' },
      { status: 500 }
    );
  }
}

export async function DELETE(request: NextRequest, { params }: RouteParams) {
  try {
    const session = await getSession();
    if (!session || session.role !== 'super-admin') {
      return NextResponse.json(
        { error: 'Acesso negado' },
        { status: 403 }
      );
    }

    const { id } = params;

    if (!id || id === 'undefined') {
      return NextResponse.json(
        { error: 'ID do tenant é obrigatório' },
        { status: 400 }
      );
    }

    const cookieStore = cookies();
    const accessToken = cookieStore.get('access_token')?.value;
    
    const response = await fetchBackend(`/admin/tenants/${id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
      },
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ error: 'Falha ao deletar tenant' }));
      return NextResponse.json(
        { error: errorData.error || 'Falha ao deletar tenant' },
        { status: response.status }
      );
    }

    const text = await response.text();
    const data = text ? JSON.parse(text) : { message: 'Tenant deletado com sucesso' };
    
    return NextResponse.json(data);
  } catch (error) {
    console.error('Erro ao deletar tenant:', error);
    
    return NextResponse.json(
      { error: 'Erro interno do servidor' },
      { status: 500 }
    );
  }
}
