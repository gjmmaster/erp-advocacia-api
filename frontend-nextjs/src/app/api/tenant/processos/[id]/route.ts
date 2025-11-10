import { NextResponse } from 'next/server'
import { getToken } from '@/lib/auth'
import api from '@/lib/api' // Importação 'default'

// GET handler para listar documentos
export async function GET(
  request: Request,
  { params }: { params: { id: string } },
) {
  const token = await getToken(request)
  if (!token) {
    return NextResponse.json({ error: 'Não autorizado' }, { status: 401 })
  }

  try {
    // --- GARANTA QUE ESTA LINHA ESTÁ CORRETA ---
    const apiResponse = await api.get(
      `/api/tenant/processos/${params.id}/documentos`,
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
        error: 'Erro ao buscar documentos',
        details: error.response?.data || error.message,
      },
      { status: error.response?.status || 500 },
    )
  }
}

// POST handler para upload de documento
export async function POST(
  request: Request,
  { params }: { params: { id: string } },
) {
  const token = await getToken(request)
  if (!token) {
    return NextResponse.json({ error: 'Não autorizado' }, { status: 401 })
  }

  try {
    const formData = await request.formData()
    const file = formData.get('file') as File
    const descricao = formData.get('descricao') as string
    const dataCriacao = formData.get('data-criacao') as string

    if (!file) {
      return NextResponse.json({ error: 'Arquivo é obrigatório' }, { status: 400 })
    }

    const backendFormData = new FormData()
    backendFormData.append('file', file, file.name)
    backendFormData.append('descricao', descricao || '')
    backendFormData.append('data-criacao', dataCriacao)

    // --- GARANTA QUE ESTA LINHA ESTÁ CORRETA ---
    const apiResponse = await api.post(
      `/api/tenant/processos/${params.id}/documentos`,
      backendFormData,
      {
        headers: {
          Authorization: `Bearer ${token}`,
          // Content-Type é tratado automaticamente pelo lib/api.ts corrigido
        },
      },
    )

    return NextResponse.json(apiResponse.data, { status: 201 })
  } catch (error: any) {
    console.error(
      'Erro ao salvar documento:',
      error.response?.data,
      error.message,
    )
    return NextResponse.json(
      {
        error: 'Erro ao salvar documento',
        details: error.response?.data || error.message,
      },
      { status: error.response?.status || 500 },
    )
  }
}
