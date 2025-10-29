import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';
import { jwtVerify } from 'jose';
import type { TenantValidation } from '@/types/tenant';

const JWT_SECRET = new TextEncoder().encode(
  process.env.JWT_SECRET || 'chave-padrao-para-desenvolvimento-segura'
);

/**
 * Extrai o subdomínio do hostname
 */
function extractSubdomain(hostname: string): string | null {
  // Remove porta se existir
  const host = hostname.split(':')[0];
  
  // Exemplo: escritorio-silva.localhost → escritorio-silva
  // Exemplo: escritorio-silva.seudominio.com → escritorio-silva
  const parts = host.split('.');
  
  // Se tem 3+ partes, o primeiro é o subdomínio
  if (parts.length >= 3) {
    return parts[0];
  }
  
  // Localhost ou domínio sem subdomínio
  return null;
}

/**
 * Valida se o tenant existe no backend
 */
async function validateTenant(subdomain: string): Promise<TenantValidation | null> {
  try {
    const backendUrl = process.env.BACKEND_URL || 'http://localhost:3000';
    const response = await fetch(
      `${backendUrl}/api/tenants/by-subdomain/${subdomain}`,
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      }
    );
    
    if (!response.ok) {
      return null;
    }
    
    return await response.json();
  } catch (error) {
    console.error('[MIDDLEWARE] Erro ao validar tenant:', error);
    return null;
  }
}

export async function middleware(request: NextRequest) {
  const { hostname, pathname } = request.nextUrl;
  console.log('[MIDDLEWARE] Requisição para:', pathname);
  console.log('[MIDDLEWARE] Hostname:', hostname);

  // Extrair subdomínio
  const subdomain = extractSubdomain(hostname);
  console.log('[MIDDLEWARE] Subdomínio extraído:', subdomain);

  // Rotas públicas (sem autenticação necessária)
  const publicPaths = [
    '/super-admin/login', 
    '/api/auth/login',
    '/login',              // Login de tenants (auto-descoberta)
    '/api/tenant/login'    // API route de login de tenants
  ];
  
  if (publicPaths.some(path => pathname.startsWith(path))) {
    console.log('[MIDDLEWARE] Rota pública');
    return NextResponse.next();
  }

  // Se não tem subdomínio, trata como rotas do super admin
  if (!subdomain) {
    console.log('[MIDDLEWARE] Sem subdomínio - rotas do super admin');
    
    // Se está tentando acessar rotas do super admin, valida autenticação
    if (pathname.startsWith('/super-admin') || pathname.startsWith('/api/admin')) {
      const accessToken = request.cookies.get('access_token')?.value;
      console.log('[MIDDLEWARE] Access token presente:', accessToken ? 'SIM' : 'NÃO');

      if (!accessToken) {
        console.log('[MIDDLEWARE] Sem token, redirecionando para login');
        if (pathname.startsWith('/api/')) {
          return NextResponse.json({ error: 'Não autenticado' }, { status: 401 });
        }
        return NextResponse.redirect(new URL('/super-admin/login', request.url));
      }

      try {
        console.log('[MIDDLEWARE] Verificando token do super admin...');
        
        // Verifica o token
        const { payload } = await jwtVerify(accessToken, JWT_SECRET);
        console.log('[MIDDLEWARE] Token verificado com sucesso');
        console.log('[MIDDLEWARE] Payload role:', payload.role);

        // Verifica se é super-admin
        const role = payload.role as string;
        if (role !== 'super-admin' && role !== 'superadmin') {
          console.error('[MIDDLEWARE] Role inválida:', role);
          if (pathname.startsWith('/api/')) {
            return NextResponse.json({ error: 'Acesso negado' }, { status: 403 });
          }
          return NextResponse.redirect(new URL('/super-admin/login', request.url));
        }

        console.log('[MIDDLEWARE] Autenticação super admin OK');
        return NextResponse.next();
      } catch (error) {
        console.error('[MIDDLEWARE] Erro ao verificar token:', error);
        
        // Token inválido, redireciona para login
        if (pathname.startsWith('/api/')) {
          return NextResponse.json({ error: 'Não autenticado' }, { status: 401 });
        }
        return NextResponse.redirect(new URL('/super-admin/login', request.url));
      }
    }

    // Outras rotas sem subdomínio, redireciona para super admin
    return NextResponse.redirect(new URL('/super-admin/login', request.url));
  }

  // Se tem subdomínio, valida o tenant
  console.log('[MIDDLEWARE] Validando tenant:', subdomain);
  const tenant = await validateTenant(subdomain);
  
  if (!tenant) {
    console.log('[MIDDLEWARE] Tenant não encontrado');
    if (pathname.startsWith('/api/')) {
      return NextResponse.json(
        { error: 'Escritório não encontrado' },
        { status: 404 }
      );
    }
    // Redireciona para página de erro ou super admin
    return NextResponse.redirect(new URL('/super-admin/login', request.url));
  }

  if (!tenant.active) {
    console.log('[MIDDLEWARE] Tenant inativo');
    if (pathname.startsWith('/api/')) {
      return NextResponse.json(
        { error: 'Escritório desativado' },
        { status: 403 }
      );
    }
    return NextResponse.redirect(new URL('/super-admin/login', request.url));
  }

  console.log('[MIDDLEWARE] Tenant válido:', tenant.name);

  // Adicionar tenant info aos headers para uso posterior
  const response = NextResponse.next();
  response.headers.set('x-tenant-id', tenant.id);
  response.headers.set('x-tenant-subdomain', subdomain);
  response.headers.set('x-tenant-name', tenant.name);

  // TODO: Adicionar validação de autenticação para rotas protegidas do tenant
  // Isso será implementado nas próximas tasks

  return response;
}

export const config = {
  matcher: [
    '/((?!_next/static|_next/image|favicon.ico).*)',
  ],
};
