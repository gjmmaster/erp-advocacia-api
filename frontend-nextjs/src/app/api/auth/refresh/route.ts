import { NextRequest, NextResponse } from 'next/server';
import { refreshSession } from '@/lib/auth';

export async function POST(request: NextRequest) {
  try {
    const success = await refreshSession();

    if (!success) {
      return NextResponse.json(
        { error: 'Sessão expirada' },
        { status: 401 }
      );
    }

    return NextResponse.json({
      message: 'Token renovado com sucesso',
    });
  } catch (error) {
    console.error('Erro ao renovar token:', error);
    
    return NextResponse.json(
      { error: 'Erro ao renovar sessão' },
      { status: 500 }
    );
  }
}
