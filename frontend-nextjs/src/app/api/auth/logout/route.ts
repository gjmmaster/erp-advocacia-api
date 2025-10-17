import { NextRequest, NextResponse } from 'next/server';
import { clearSession } from '@/lib/auth';

export async function POST(request: NextRequest) {
  try {
    await clearSession();

    return NextResponse.json({
      message: 'Logout realizado com sucesso',
    });
  } catch (error) {
    console.error('Erro no logout:', error);
    
    try {
      await clearSession();
    } catch (clearError) {
      console.error('Erro ao limpar sessão:', clearError);
    }

    return NextResponse.json(
      { error: 'Erro no logout' },
      { status: 500 }
    );
  }
}
