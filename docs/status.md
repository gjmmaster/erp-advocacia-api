### **Diário de Bordo Técnico: Evolução da Arquitetura Multi-Tenant**

**Data de Referência:** 05 de Setembro de 2025
**Status:** Segunda fase da PoC concluída: Autenticação stateless com JWT implementada e validada.

---

### **Etapa 1: Prova de Conceito (PoC) da Arquitetura Multi-Tenant**

Esta fase inicial validou os pilares da arquitetura, incluindo o isolamento de dados via `tenant_id`, o desacoplamento da persistência com `protocols`, a validação de contratos com `clojure.spec`, e o ciclo de vida básico do tenant.

*   **Validação Crítica:** Demonstrou-se que um tenant não poderia acessar dados de outro, utilizando um header `X-Tenant-ID` para injetar o contexto de segurança.
*   **Resultado:** A arquitetura base foi considerada **bem-sucedida**, abrindo caminho para a implementação de um mecanismo de autenticação robusto.

---

### **Etapa 2: Implementação de Autenticação Stateless com JWT**

Nesta segunda fase, o mecanismo de autenticação simulado foi substituído por um sistema completo e seguro baseado em JSON Web Tokens (JWT), utilizando a biblioteca `buddy-auth`.

#### **2.1. Arquitetura de Autenticação com JWT**

*   **Geração de Token no Login:** O `login-handler` foi aprimorado. Após validar as credenciais do usuário com `buddy.hashers`, ele agora gera um token JWT assinado.
    *   **Claims do JWT:** O payload do token (claims) é enriquecido com dados essenciais para o controle de acesso:
        *   `:user-id`: Identificador do usuário autenticado.
        *   `:tenant-id`: Identificador do tenant ao qual o usuário pertence. **Esta é a fonte da verdade para o isolamento de dados.**
        *   `:role`: Papel do usuário (e.g., `:admin`, `:operador`).
        *   `:exp`: Timestamp de expiração do token (atualmente configurado para 1 hora), garantindo que as sessões sejam automaticamente invalidadas.

*   **Novo Middleware de Autenticação (`wrap-jwt-authentication`):**
    *   Este middleware substitui completamente o antigo `wrap-tenant-db-repo`.
    *   Ele é responsável por inspecionar o header `Authorization: Bearer <token>` em todas as requisições para endpoints protegidos.
    *   Utilizando `buddy.sign.jwt/unsign`, ele valida a assinatura e a expiração do token.
    *   Se o token for válido, as *claims* são extraídas. A `claim` `:tenant-id` é usada para instanciar o repositório de dados (`db-repo`) com o escopo correto, garantindo que todas as operações de banco de dados subsequentes fiquem restritas àquele tenant.
    *   A identidade completa do usuário (`identity`), contendo todas as claims, é injetada na requisição para uso futuro na lógica de negócios (e.g., autorização baseada em roles).
    *   O header `X-Tenant-ID` foi **completamente removido** e não é mais necessário para acessar a API.

#### **2.2. Validação Funcional da Autenticação JWT**

Os testes de integração foram atualizados para refletir o novo fluxo de autenticação.

1.  **Provisionamento e Login (Fluxo Inalterado):**
    * `POST /admin/provision-tenant` e `POST /auth/login` continuam funcionando como antes.
    * **Resultado do Login:** A resposta de um login bem-sucedido agora inclui um `token` JWT.
      ```json
      {
        "message": "Usuário admin@empresa-a.com autenticado com sucesso.",
        "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyLWlkIjoi..."
      }
      ```

2.  **Acesso a Recursos Protegidos com Token JWT:**
    * **Ação:** Para acessar endpoints como `/api/processos`, o cliente agora deve incluir o token JWT no header `Authorization`.
    * **Exemplo de Requisição (`curl`):**
      ```bash
      # Assumindo que a variável $JWT_TOKEN contém o token obtido no login
      curl -X GET http://localhost:3000/api/processos \
        -H "Authorization: Bearer $JWT_TOKEN"
      ```
    * **Validação do Isolamento de Tenant:** O teste crítico de segurança foi revalidado com sucesso. Um token gerado para o `tenant-1` **não permite** o acesso a recursos do `tenant-2`, resultando em um `404 Not Found`, pois o repositório instanciado pelo middleware só "enxerga" os dados do `tenant-1`.
    * **Validação do Middleware:** Uma requisição a `/api/processos` sem o header `Authorization` (ou com um token inválido/expirado) resulta em **SUCESSO (`401 Unauthorized`)**, confirmando a robustez da camada de segurança.

---

### **3. Próximos Passos Arquitetônicos**

Com a PoC e a autenticação JWT validadas, a evolução para o produto final seguirá o plano, focando em:

1.  **Migração da Persistência para PostgreSQL:** Substituir a implementação do `MockRepository` por uma nova que interaja com um banco de dados PostgreSQL. Graças ao desacoplamento provido pelos `protocols`, esta migração não exigirá alterações na camada de `handlers` ou na lógica de negócio.
2.  **Identificação de Tenant por Subdomínio na Requisição:** Evoluir o mecanismo de identificação de tenant para analisar o subdomínio do host da requisição (e.g., `tenant-a.meuerp.com`), conforme definido na arquitetura final. Isso simplificará o processo de login, eliminando a necessidade de enviar o `subdomain` no corpo da requisição.
