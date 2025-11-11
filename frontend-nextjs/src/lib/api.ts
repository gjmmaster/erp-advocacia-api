const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:3000'

export interface FetchOptions extends RequestInit {
  timeout?: number
}

/**
 * Faz requisição para o backend Clojure
 */
export async function fetchBackend(
  endpoint: string,
  options: FetchOptions = {},
): Promise<Response> {
  const { timeout = 60000, ...fetchOptions } = options // Aumentado para 60s para sleep mode

  const controller = new AbortController()
  const timeoutId = setTimeout(() => controller.abort(), timeout)

  try {
    const url = `${BACKEND_URL}${endpoint}`
    console.log('[API] Fazendo requisição para:', url)
    console.log('[API] BACKEND_URL:', BACKEND_URL)
    console.log('[API] endpoint:', endpoint)

    // Detecta se o body é FormData
    const isFormData = fetchOptions.body instanceof FormData

    // Define cabeçalhos padrão
    const defaultHeaders: HeadersInit = {
      // NÃO defina Content-Type se for FormData; o fetch() fará isso
      ...(!isFormData && { 'Content-Type': 'application/json' }),
    }

    const response = await fetch(url, {
      ...fetchOptions,
      signal: controller.signal,
      headers: {
        ...defaultHeaders, // Aplica os padrões
        ...fetchOptions.headers, // Permite sobrescrever (ex: Authorization)
      },
    })

    clearTimeout(timeoutId)
    return response
  } catch (error) {
    clearTimeout(timeoutId)

    if (error instanceof Error && error.name === 'AbortError') {
      throw new Error('Request timeout')
    }

    throw error
  }
}

/**
 * Faz requisição GET para o backend
 */
export async function get<T = any>(
  endpoint: string,
  options: FetchOptions = {},
): Promise<T> {
  const response = await fetchBackend(endpoint, {
    ...options,
    method: 'GET',
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({ error: 'Request failed' }))
    throw new Error(error.error || `HTTP ${response.status}`)
  }

  return response.json()
}

/**
 * Faz requisição POST para o backend
 */
export async function post<T = any>(
  endpoint: string,
  data: any, // 'data' pode ser JSON ou FormData
  options: FetchOptions = {},
): Promise<T> {
  // Não faz stringify se for FormData
  const isFormData = data instanceof FormData
  const body = isFormData ? data : JSON.stringify(data)

  console.log('[API.POST] endpoint:', endpoint)
  console.log('[API.POST] isFormData:', isFormData)
  console.log('[API.POST] body type:', body?.constructor?.name)

  const response = await fetchBackend(endpoint, {
    ...options,
    method: 'POST',
    body: body, // Passa o body (JSON stringified ou FormData)
  })

  console.log('[API.POST] response status:', response.status)
  console.log('[API.POST] response headers:', Object.fromEntries(response.headers.entries()))

  if (!response.ok) {
    const error = await response.json().catch(() => ({ error: 'Request failed' }))
    throw new Error(error.error || `HTTP ${response.status}`)
  }

  return response.json()
}

/**
 * Faz requisição PUT para o backend
 */
export async function put<T = any>(
  endpoint: string,
  data: any,
  options: FetchOptions = {},
): Promise<T> {
  const response = await fetchBackend(endpoint, {
    ...options,
    method: 'PUT',
    body: JSON.stringify(data),
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({ error: 'Request failed' }))
    throw new Error(error.error || `HTTP ${response.status}`)
  }

  return response.json()
}

/**
 * Faz requisição DELETE para o backend
 */
export async function del<T = any>(
  endpoint: string,
  options: FetchOptions = {},
): Promise<T> {
  const response = await fetchBackend(endpoint, {
    ...options,
    method: 'DELETE',
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({ error: 'Request failed' }))
    throw new Error(error.error || `HTTP ${response.status}`)
  }

  // DELETE pode não retornar body
  const text = await response.text()
  return text ? JSON.parse(text) : ({} as T)
}

// Adiciona a exportação default que os outros arquivos esperam
const api = {
  fetchBackend,
  get,
  post,
  put,
  del,
}

export default api
