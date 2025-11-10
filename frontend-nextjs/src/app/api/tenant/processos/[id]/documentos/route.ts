import { SignJWT, jwtVerify } from 'jose'
import { cookies } from 'next/headers'

const JWT_SECRET = new TextEncoder().encode(
  process.env.JWT_SECRET || 'chave-padrao-para-desenvolvimento-segura',
)

const ACCESS_TOKEN_EXPIRY = 15 * 60 // 15 minutos
const REFRESH_TOKEN_EXPIRY = 7 * 24 * 60 * 60 // 7 dias

export interface TokenPayload {
  email: string
  role: string
  'tenant-id'?: string
  type?: 'access' | 'refresh'
  exp?: number
  // Impersonation fields
  impersonating?: boolean
  'impersonator-id'?: number
  'impersonator-email'?: string
  // Temporary password fields
  'temporary-password'?: boolean
  'requires-password-change'?: boolean
}

/**
 * Cria um token JWT
 */
export async function createToken(
  payload: TokenPayload,
  expiresIn: number = ACCESS_TOKEN_EXPIRY,
): Promise<string> {
  const token = await new SignJWT(payload as any)
    .setProtectedHeader({ alg: 'HS256' })
    .setIssuedAt()
    .setExpirationTime(Math.floor(Date.now() / 1000) + expiresIn)
    .sign(JWT_SECRET)

  return token
}

/**
 * Verifica e decodifica um token JWT
 */
export async function verifyToken(token: string): Promise<TokenPayload | null> {
  try {
    const { payload } = await jwtVerify(token, JWT_SECRET)
    return payload as unknown as TokenPayload
  } catch (error) {
    console.error('Token verification failed:', error)
    return null
  }
}

/**
 * Obtém a sessão atual dos cookies
 */
export async function getSession(): Promise<TokenPayload | null> {
  console.log('[AUTH] getSession chamado')
  const cookieStore = cookies()
  const accessToken = cookieStore.get('access_token')?.value
  console.log('[AUTH] Access token presente:', accessToken ? 'SIM' : 'NÃO')

  if (!accessToken) {
    console.log('[AUTH] Sem access token, retornando null')
    return null
  }

  console.log('[AUTH] Verificando token...')
  const result = await verifyToken(accessToken)
  console.log('[AUTH] Token verificado:', result ? 'VÁLIDO' : 'INVÁLIDO')
  console.log('[AUTH] Role do token:', result?.role)
  return result
}

/**
 * Define os cookies de sessão
 */
export async function setSession(backendToken: string) {
  console.log('[AUTH] setSession chamado')
  console.log('[AUTH] NODE_ENV:', process.env.NODE_ENV)
  console.log(
    '[AUTH] Token recebido (primeiros 20 chars):',
    backendToken.substring(0, 20),
  )

  const cookieStore = cookies()

  // Usa o token do backend DIRETAMENTE como access_token
  // Não recria o token para evitar problemas de JWT_SECRET
  console.log('[AUTH] Definindo access_token cookie')
  cookieStore.set('access_token', backendToken, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax',
    maxAge: ACCESS_TOKEN_EXPIRY,
    path: '/',
  })

  console.log('[AUTH] Definindo refresh_token cookie')
  // Usa o mesmo token como refresh_token por enquanto
  cookieStore.set('refresh_token', backendToken, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax',
    maxAge: REFRESH_TOKEN_EXPIRY,
    path: '/',
  })

  console.log('[AUTH] Cookies definidos com sucesso')
}

/**
 * Limpa a sessão (logout)
 */
export async function clearSession() {
  const cookieStore = cookies()
  cookieStore.delete('access_token')
  cookieStore.delete('refresh_token')
}

/**
 * Renova o access token usando o refresh token
 */
export async function refreshSession(): Promise<boolean> {
  const cookieStore = cookies()
  const refreshToken = cookieStore.get('refresh_token')?.value

  if (!refreshToken) {
    return false
  }

  const payload = await verifyToken(refreshToken)

  if (!payload || (payload as any).type !== 'refresh') {
    return false
  }

  // Remove o campo 'type' do payload
  const { type, ...userPayload } = payload as any

  // Cria novo access token
  const newAccessToken = await createToken(userPayload, ACCESS_TOKEN_EXPIRY)

  cookieStore.set('access_token', newAccessToken, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'strict',
    maxAge: ACCESS_TOKEN_EXPIRY,
    path: '/',
  })

  return true
}

// --- INÍCIO DA CORREÇÃO ---
// Adiciona a função 'getToken' que estava faltando para os Route Handlers
/**
 * Obtém o token de acesso (string) do cookie
 * Usado em Route Handlers (BFF) para pegar o token e repassar ao backend.
 * É 'async' para corresponder à chamada 'await getToken(request)' nos route handlers.
 */
export async function getToken(request: Request): Promise<string | undefined> {
  // O parâmetro 'request' é ignorado para usar a função 'cookies()' do next/headers,
  // que é a forma padrão de acessar cookies em Route Handlers.
  const cookieStore = cookies()
  return cookieStore.get('access_token')?.value
}
// --- FIM DA CORREÇÃO ---
// (A chave '}' extra que estava aqui foi removida)
