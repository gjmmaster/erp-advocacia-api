import { SignJWT, jwtVerify } from 'jose';
import { cookies } from 'next/headers';

const JWT_SECRET = new TextEncoder().encode(
  process.env.JWT_SECRET || 'chave-padrao-para-desenvolvimento-segura'
);

const ACCESS_TOKEN_EXPIRY = 15 * 60; // 15 minutos
const REFRESH_TOKEN_EXPIRY = 7 * 24 * 60 * 60; // 7 dias

export interface TokenPayload {
  email: string;
  role: string;
  'tenant-id'?: string;
  type?: 'access' | 'refresh';
  exp?: number;
}

/**
 * Cria um token JWT
 */
export async function createToken(
  payload: TokenPayload,
  expiresIn: number = ACCESS_TOKEN_EXPIRY
): Promise<string> {
  const token = await new SignJWT(payload as any)
    .setProtectedHeader({ alg: 'HS256' })
    .setIssuedAt()
    .setExpirationTime(Math.floor(Date.now() / 1000) + expiresIn)
    .sign(JWT_SECRET);

  return token;
}

/**
 * Verifica e decodifica um token JWT
 */
export async function verifyToken(token: string): Promise<TokenPayload | null> {
  try {
    const { payload } = await jwtVerify(token, JWT_SECRET);
    return payload as unknown as TokenPayload;
  } catch (error) {
    console.error('Token verification failed:', error);
    return null;
  }
}

/**
 * Obtém a sessão atual dos cookies
 */
export async function getSession(): Promise<TokenPayload | null> {
  const cookieStore = cookies();
  const accessToken = cookieStore.get('access_token')?.value;

  if (!accessToken) {
    return null;
  }

  return verifyToken(accessToken);
}

/**
 * Define os cookies de sessão
 */
export async function setSession(backendToken: string) {
  const cookieStore = cookies();
  
  // Usa o token do backend DIRETAMENTE como access_token
  // Não recria o token para evitar problemas de JWT_SECRET
  cookieStore.set('access_token', backendToken, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax',
    maxAge: ACCESS_TOKEN_EXPIRY,
    path: '/',
  });

  // Usa o mesmo token como refresh_token por enquanto
  cookieStore.set('refresh_token', backendToken, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax',
    maxAge: REFRESH_TOKEN_EXPIRY,
    path: '/',
  });
}

/**
 * Limpa a sessão (logout)
 */
export async function clearSession() {
  const cookieStore = cookies();
  cookieStore.delete('access_token');
  cookieStore.delete('refresh_token');
}

/**
 * Renova o access token usando o refresh token
 */
export async function refreshSession(): Promise<boolean> {
  const cookieStore = cookies();
  const refreshToken = cookieStore.get('refresh_token')?.value;

  if (!refreshToken) {
    return false;
  }

  const payload = await verifyToken(refreshToken);

  if (!payload || (payload as any).type !== 'refresh') {
    return false;
  }

  // Remove o campo 'type' do payload
  const { type, ...userPayload } = payload as any;

  // Cria novo access token
  const newAccessToken = await createToken(userPayload, ACCESS_TOKEN_EXPIRY);

  cookieStore.set('access_token', newAccessToken, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'strict',
    maxAge: ACCESS_TOKEN_EXPIRY,
    path: '/',
  });

  return true;
}
