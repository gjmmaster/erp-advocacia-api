const BACKEND_URL = process.env.BACKEND_API_URL || 'http://localhost:3000';

export interface FetchOptions extends RequestInit {
  timeout?: number;
}

/**
 * Faz requisição para o backend Clojure
 */
export async function fetchBackend(
  endpoint: string,
  options: FetchOptions = {}
): Promise<Response> {
  const { timeout = 30000, ...fetchOptions } = options;

  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeout);

  try {
    const url = `${BACKEND_URL}${endpoint}`;
    console.log('[API] Fazendo requisição para:', url);
    console.log('[API] BACKEND_URL:', BACKEND_URL);
    console.log('[API] endpoint:', endpoint);
    
    const response = await fetch(url, {
      ...fetchOptions,
      signal: controller.signal,
      headers: {
        'Content-Type': 'application/json',
        ...fetchOptions.headers,
      },
    });

    clearTimeout(timeoutId);
    return response;
  } catch (error) {
    clearTimeout(timeoutId);
    
    if (error instanceof Error && error.name === 'AbortError') {
      throw new Error('Request timeout');
    }
    
    throw error;
  }
}

/**
 * Faz requisição GET para o backend
 */
export async function get<T = any>(
  endpoint: string,
  options: FetchOptions = {}
): Promise<T> {
  const response = await fetchBackend(endpoint, {
    ...options,
    method: 'GET',
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ error: 'Request failed' }));
    throw new Error(error.error || `HTTP ${response.status}`);
  }

  return response.json();
}

/**
 * Faz requisição POST para o backend
 */
export async function post<T = any>(
  endpoint: string,
  data: any,
  options: FetchOptions = {}
): Promise<T> {
  const response = await fetchBackend(endpoint, {
    ...options,
    method: 'POST',
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ error: 'Request failed' }));
    throw new Error(error.error || `HTTP ${response.status}`);
  }

  return response.json();
}

/**
 * Faz requisição PUT para o backend
 */
export async function put<T = any>(
  endpoint: string,
  data: any,
  options: FetchOptions = {}
): Promise<T> {
  const response = await fetchBackend(endpoint, {
    ...options,
    method: 'PUT',
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ error: 'Request failed' }));
    throw new Error(error.error || `HTTP ${response.status}`);
  }

  return response.json();
}

/**
 * Faz requisição DELETE para o backend
 */
export async function del<T = any>(
  endpoint: string,
  options: FetchOptions = {}
): Promise<T> {
  const response = await fetchBackend(endpoint, {
    ...options,
    method: 'DELETE',
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ error: 'Request failed' }));
    throw new Error(error.error || `HTTP ${response.status}`);
  }

  // DELETE pode não retornar body
  const text = await response.text();
  return text ? JSON.parse(text) : ({} as T);
}
