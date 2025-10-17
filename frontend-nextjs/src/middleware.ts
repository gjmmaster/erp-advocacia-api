import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';
import { jwtVerify } from 'jose';

const JWT_SECRET = new TextEncoder().encode(
  process.env.JWT_SECRET || 'chave-padrao-para-desenvolvimento-segura'
);

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  console.log('[MIDDLEWARE] Requisição para:', pathname);

  // Rotas públicas (não requerem autenticação)
  const publicPaths = ['/super-admin/login', '/api/auth/login'];
  
  if (publicPaths.some(path => pathname.startsWith(path))) {
    console.log('[MIDDLEWARE] Rota pública, permitindo acesso');
    return NextResponse.next();
  }

  // Rotas protegidas do super admin
  if (pathname.startsWith('/super-admin') || pathname.startsWith('/api/admin')) {
    const accessToken = request.cookies.get('access_token')?.value;
    console.log('[MIDDLEWARE] Access token presente:', accessToken ? 'SIM' : 'NÃO');

    if (!accessToken) {
      console.log('[MIDDLEWARE] Sem token, redirecionando para login');
      // Sem token, redireciona para login
      if (pathname.startsWith('/api/')) {
        return NextResponse.json({ error: 'Não autenticado' }, { status: 401 });
      }
      return NextResponse.redirect(new URL('/super-admin/login', request.url));
    }

    try {
      console.log('[MIDDLEWARE] Verificando token...');
      console.log('[MIDDLEWARE] Token (primeiros 20 chars):', accessToken.substring(0, 20));
      
      // Verifica o token
      const { payload } = await jwtVerify(accessToken, JWT_SECRET);
      console.log('[MIDDLEWARE] Token verificado com sucesso');
      console.log('[MIDDLEWARE] Payload role:', payload.role);

      // Verifica se é super-admin (aceita tanto 'super-admin' quanto 'superadmin')
      const role = payload.role as string;
      if (role !== 'super-admin' && role !== 'superadmin') {
        console.error('[MIDDLEWARE] Role inválida:', role);
        if (pathname.startsWith('/api/')) {
          return NextResponse.json({ error: 'Acesso negado' }, { status: 403 });
        }
        return NextResponse.redirect(new URL('/super-admin/login', request.url));
      }

      console.log('[MIDDLEWARE] Autenticação OK, permitindo acesso');
      // Token válido, continua
      return NextResponse.next();
    } catch (error) {
      console.error('[MIDDLEWARE] Erro ao verificar token:', error);
      console.error('[MIDDLEWARE] Tipo do erro:', error instanceof Error ? error.message : 'Desconhecido');
      // Token inválido ou expirado, tenta renovar
      const refreshToken = request.cookies.get('refresh_token')?.value;

      if (refreshToken) {
        try {
          const { payload } = await jwtVerify(refreshToken, JWT_SECRET);

          // Verifica se é refresh token
          if ((payload as any).type === 'refresh' && payload.role === 'super-admin') {
            // Redireciona para endpoint de refresh
            if (pathname.startsWith('/api/')) {
              return NextResponse.json({ error: 'Token expirado' }, { status: 401 });
            }
            // Para páginas, deixa o cliente fazer o refresh
            return NextResponse.next();
          }
        } catch (refreshError) {
          // Refresh token também inválido
        }
      }

      // Sem token válido, redireciona para login
      if (pathname.startsWith('/api/')) {
        return NextResponse.json({ error: 'Não autenticado' }, { status: 401 });
      }
      return NextResponse.redirect(new URL('/super-admin/login', request.url));
    }
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    '/super-admin/:path*',
    '/api/admin/:path*',
  ],
};
