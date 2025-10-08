# Implementation Plan

- [x] 1. Adicionar dependência buddy-core ao projeto


  - Adicionar `[buddy/buddy-core "1.11.423"]` ao vetor `:dependencies` em `project.clj`
  - Executar `lein deps` para baixar a nova dependência
  - _Requirements: 1.1, 1.4_


- [ ] 2. Implementar geração segura de senhas temporárias
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_



- [ ] 2.1 Criar função helper para geração de senha segura
  - Adicionar requires necessários em `src/juridico/api/db/postgres.clj`: `[buddy.core.nonce :as nonce]` e `[buddy.core.codecs :as codecs]`


  - Implementar função privada `generate-secure-temp-password` que gera 16 bytes aleatórios, converte para Base64, remove caracteres não-alfanuméricos e retorna os primeiros 12 caracteres
  - _Requirements: 1.1, 1.2, 1.4, 1.5_

- [ ] 2.2 Substituir geração de senha na função criar-tenant-e-usuario-master
  - Localizar a linha `temp-password (str "pass" (rand-int 10000))` na função `criar-tenant-e-usuario-master`
  - Substituir por `temp-password (generate-secure-temp-password)`
  - Verificar que o resto da lógica permanece inalterado
  - _Requirements: 1.1, 1.3_

- [ ]* 2.3 Criar testes unitários para geração de senha segura
  - Criar arquivo `test/juridico/api/db/postgres_test.clj` se não existir

  - Implementar teste que verifica comprimento de 12 caracteres
  - Implementar teste que verifica apenas caracteres alfanuméricos
  - Implementar teste que verifica não-determinismo (senhas diferentes)


  - Implementar teste que verifica ausência de colisões em 1000 gerações
  - _Requirements: 1.2, 1.3, 1.5_

- [ ] 3. Implementar validação fail-fast de JWT_SECRET
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [ ] 3.1 Modificar config.clj para validação de JWT_SECRET
  - Adicionar require para logging: `[clojure.tools.logging :as log]`
  - Substituir a definição simples de `jwt-secret` por uma implementação com `let` que verifica `APP_ENV` e `JWT_SECRET`

  - Implementar lógica condicional: se produção sem JWT_SECRET lança Exception, se desenvolvimento sem JWT_SECRET usa padrão com log de aviso, se JWT_SECRET definido usa valor com log de sucesso
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_



- [ ]* 3.2 Criar testes para validação de JWT_SECRET
  - Criar arquivo `test/juridico/api/config_test.clj`
  - Implementar teste que verifica exceção em produção sem JWT_SECRET usando `with-redefs`
  - Implementar teste que verifica permissão de chave padrão em desenvolvimento
  - _Requirements: 2.1, 2.2_

- [ ] 4. Implementar validação rigorosa de subdomínio
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8_


- [ ] 4.1 Atualizar spec de subdomínio em specs.clj
  - Localizar a definição atual de `::subdomain` em `src/juridico/api/specs.clj`


  - Substituir por nova spec que usa regex RFC 1035: `(s/and string? not-empty #(re-matches #"[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?" %))`
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.8_

- [ ]* 4.2 Criar testes para validação de subdomínio
  - Criar arquivo `test/juridico/api/specs_test.clj` se não existir
  - Implementar testes para subdomínios válidos: "a", "abc", "abc-def", "abc123", "a1b2c3", "escritorio-legal", "adv2025"
  - Implementar testes para subdomínios inválidos: "", "-abc", "abc-", "--", "ABC", "abc_def", "abc.def", "abc def", "!@#$"
  - _Requirements: 3.3, 3.4, 3.5, 3.6, 3.7_

- [ ] 5. Testes de integração e validação final
  - _Requirements: Todos_

- [ ] 5.1 Executar suite completa de testes
  - Executar `lein test` para rodar todos os testes unitários
  - Verificar que todos os testes passam
  - Corrigir quaisquer falhas identificadas
  - _Requirements: Todos_

- [ ]* 5.2 Testar provisionamento de tenant manualmente
  - Iniciar aplicação em modo desenvolvimento


  - Fazer POST para `/admin/tenants` com payload válido
  - Verificar que senha temporária retornada tem 12 caracteres alfanuméricos
  - Verificar que subdomínio gerado está no formato correto
  - Tentar criar tenant com subdomínio inválido e verificar erro 400

  - _Requirements: 1.1, 1.2, 1.5, 3.1, 3.7_

- [ ]* 5.3 Testar validação de JWT_SECRET
  - Tentar iniciar aplicação com `APP_ENV=production` sem `JWT_SECRET` e verificar que falha
  - Iniciar aplicação em desenvolvimento sem `JWT_SECRET` e verificar log de aviso
  - Iniciar aplicação com `JWT_SECRET` definido e verificar log de sucesso
  - _Requirements: 2.1, 2.2, 2.3, 2.5_

- [ ] 6. Documentação e preparação para deploy
  - _Requirements: Todos_

- [ ] 6.1 Atualizar documentação de configuração
  - Adicionar ou atualizar arquivo de documentação explicando a necessidade de configurar `JWT_SECRET` em produção
  - Documentar o comportamento de fail-fast e como resolver o erro
  - Adicionar exemplos de subdomínios válidos e inválidos
  - _Requirements: 2.4, 3.7_

- [ ] 6.2 Criar checklist de deploy
  - Criar documento com checklist pré-deploy: verificar `JWT_SECRET` configurado, verificar `APP_ENV=production`, testar em staging
  - Documentar plano de rollback caso necessário
  - Listar compatibilidades garantidas (senhas antigas, tokens JWT, subdomínios existentes)
  - _Requirements: Todos_
