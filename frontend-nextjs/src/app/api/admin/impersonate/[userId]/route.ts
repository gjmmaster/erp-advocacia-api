import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:3000';

export async function POST(
  request: NextRequest,
  { params }: { params: { userId: string } }
) {
  try {
    const cookieStore = cookies();
    const token = cookieStore.get('auth-token')?.value;

    if (!token) {
      return NextResponse.json(
        { error: 'Não autenticado' },
        { status: 401 }
      );
    }

    // Chamar backend para iniciar impersonation
    const response = await fetch(`${API_URL}/admin/impersonate/${params.userId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
    });

    if (!response.ok) {
      const error = await response.json();
      return NextResponse.json(error, { status: response.status });
    }

    const data = await response.json();

    // Atualizar cookie com novo token
    const responseObj = NextResponse.json(data);
    responseObj.cookies.set('auth-token', data.token, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'lax',
      maxAge: 3600, // 1 hora
      path: '/',
    });

    return responseObj;
  } catch (error) {
    console.error('Erro ao iniciar impersonation:', error);
    return NextResponse.json(
      { error: 'Erro ao iniciar impersonation' },
      { status: 500 }
    );
  }
}
