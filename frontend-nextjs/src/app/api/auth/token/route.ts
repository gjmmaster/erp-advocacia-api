import { NextResponse } from 'next/server'
import { getToken } from '@/lib/auth'

export async function GET(request: Request) {
  const token = await getToken(request)
  
  if (!token) {
    return NextResponse.json({ error: 'Não autorizado' }, { status: 401 })
  }

  return NextResponse.json({ token })
}
