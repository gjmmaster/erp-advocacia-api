import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

export async function GET(request: NextRequest) {
  try {
    const cookieStore = cookies();
    const token = cookieStore.get('access_token')?.value;

    if (!token) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    const searchParams = request.nextUrl.searchParams;
    const q = searchParams.get('q') || '';
    const page = searchParams.get('page') || '1';
    const perPage = searchParams.get('per-page') || '20';

    if (q.length < 2) {
      return NextResponse.json(
        { error: 'Termo de busca deve ter pelo menos 2 caracteres' },
        { status: 400 }
      );
    }

    const params = new URLSearchParams();
    params.append('q', q);
    params.append('page', page);
    params.append('per-page', perPage);

    const response = await fetch(`${BACKEND_URL}/api/tenant/processos/search?${params.toString()}`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      const error = await response.json();
      return NextResponse.json(error, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('Error searching processos:', error);
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    );
  }
}
