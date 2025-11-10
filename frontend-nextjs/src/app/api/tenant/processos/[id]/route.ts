import { NextResponse } from 'next/server'
import { getToken } from '@/lib/auth'
import api from '@/lib/api' // Importação 'default'

// GET handler para buscar um processo específico
export async function GET(
  request: Request,
  { params }: { params: { id: string } },
) {
  const token = await getToken(request)
  if (!token) {
    return NextResponse.json({ error: 'Não autorizado' }, { status: 401 })
  }

  try {
    const apiResponse = await api.get(
      `/api/tenant/processos/${params.id}`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      },
    )
    return NextResponse.json(apiResponse.data)
  } catch (error: any) {
    return NextResponse.json(
      {
        error: 'Erro ao buscar processo',
        details: error.response?.data || error.message,
      },
      { status: error.response?.status || 500 },
    )
  }
}

// PUT handler para atualizar um processo
export async function PUT(
  request: Request,
  { params }: { params: { id: string } },
) {
  const token = await getToken(request)
  if (!token) {
    return NextResponse.json({ error: 'Não autorizado' }, { status: 401 })
  }

  try {
    const body = await request.json()

    const apiResponse = await api.put(
      `/api/tenant/processos/${params.id}`,
      body,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      },
    )

    return NextResponse.json(apiResponse.data)
  } catch (error: any) {
    console.error('Erro ao atualizar processo:', error.response?.data)
    return NextResponse.json(
      {
        error: 'Erro ao atualizar processo',
        details: error.response?.data || error.message,
      },
      { status: error.response?.status || 500 },
    )
  }
}

// DELETE handler para deletar um processo
export async function DELETE(
  request: Request,
  { params }: { params: { id: string } },
) {
  const token = await getToken(request)
  if (!token) {
    return NextResponse.json({ error: 'Não autorizado' }, { status: 401 })
  }

  try {
    const apiResponse = await api.delete(
      `/api/tenant/processos/${params.id}`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      },
    )

    return NextResponse.json(apiResponse.data)
  } catch (error: any) {
    console.error('Erro ao deletar processo:', error.response?.data)
    return NextResponse.json(
      {
        error: 'Erro ao deletar processo',
        details: error.response?.data || error.message,
      },
      { status: error.response?.status || 500 },
    )
  }
}
