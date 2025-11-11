import { NextResponse } from 'next/server'
import { getToken } from '@/lib/auth'
import api from '@/lib/api'

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
    const apiResponse = await api.get(
      `/api/tenant/processos/${params.id}/documentos`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      },
    )
    return NextResponse.json(apiResponse)
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
    console.log('[BFF] Recebendo upload request')
    const formData = await request.formData()
    const file = formData.get('file') as File
    const descricao = formData.get('descricao') as string
    const dataCriacao = formData.get('data-criacao') as string

    console.log('[BFF] File recebido:', file?.name, file?.size, file?.type)
    console.log('[BFF] Descricao:', descricao)
    console.log('[BFF] Data criacao:', dataCriacao)

    if (!file) {
      return NextResponse.json({ error: 'Arquivo é obrigatório' }, { status: 400 })
    }

    // Criar um novo FormData para enviar ao backend Clojure
    const backendFormData = new FormData()
    backendFormData.append('file', file, file.name)
    backendFormData.append('descricao', descricao || '')
    backendFormData.append('data-criacao', dataCriacao)

    console.log('[BFF] Enviando para backend Clojure...')
    console.log('[BFF] FormData entries:')
    for (const [key, value] of backendFormData.entries()) {
      console.log(`  ${key}:`, value instanceof File ? `File(${value.name}, ${value.size} bytes)` : value)
    }

    const apiResponse = await api.post(
      `/api/tenant/processos/${params.id}/documentos`,
      backendFormData,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      },
    )

    console.log('[BFF] Resposta do backend:', apiResponse)
    return NextResponse.json(apiResponse, { status: 201 })
  } catch (error: any) {
    console.error('Erro ao salvar documento:', error.response?.data)
    return NextResponse.json(
      {
        error: 'Erro ao salvar documento',
        details: error.response?.data || error.message,
      },
      { status: error.response?.status || 500 },
    )
  }
}
