import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:3001';

export async function DELETE(
  request: NextRequest,
  { params }: { params: { id: string; documentoId: string } }
) {
  try {
    const cookieStore = cookies();
    const accessToken = cookieStore.get('access_token')?.value;

    if (!accessToken) {
      return NextResponse.json({ error: 'Não autenticado' }, { status: 401 });
    }

    const response = await fetch(
      `${BACKEND_URL}/api/tenant/processos/${params.id}/documentos/${params.documentoId}`,
      {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
        },
      }
    );

    if (!response.ok) {
      const error = await response.json().catch(() => ({ error: 'Erro ao deletar documento' }));
      return NextResponse.json(error, { status: response.status });
    }

    return new NextResponse(null, { status: 204 });
  } catch (error) {
    console.error('Error deleting documento:', error);
    return NextResponse.json(
      { error: 'Erro ao deletar documento' },
      { status: 500 }
    );
  }
}
