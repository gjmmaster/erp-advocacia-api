# Design Document - Dev Mode Mock Repository

## Overview

Este documento descreve o design da implementação de um repositório mock baseado em `atom` para modo de desenvolvimento. A solução permite que desenvolvedores trabalhem sem dependência de banco de dados, acelerando o ciclo de desenvolvimento e facilitando testes.

A arquitetura segue o padrão de repositório com inversão de dependência, onde:
- **Protocolos** definem contratos (já existentes em `db/protocols.clj`)
- **Implementação PostgreSQL** para produção (já existe em `db/postgres.clj`)
- **Implementação Mock** para desenvolvimento (nova, em `db/mock.clj`)
- **Factory/Constructor** decide qual implementação usar baseado em configuração

## Architecture

### Diagrama de Componentes

```mermaid
graph TB
    subgraph "Application Layer"
        H[Handlers]
        M[Middleware]
    end
    
    subgraph "Repository Layer"
        P[Protocols<br/>db/protocols.clj]
    end
    
    subgraph "Implementations"
        PG[PostgresRepository<br/>db/postgres.clj]
        MOCK[MockRepository<br/>db/mock.clj]
    end
    
    subgraph "Configuration"
        ENV[Environment Variables<br/>DEV_MODE=true/false]
        FACTORY[Repository Factory<br/>db/core.clj]
    end
    
    H --> P
    M --> P
    P -.implements.-> PG
    P -.implements.-> MOCK
    ENV --> FACTORY
    FACTORY --> PG
    FACTORY --> MOCK
    
    style MOCK fill:#90EE90
    style PG fill:#87CEEB
```

### Fluxo de Decisão

```mermaid
flowchart TD
    START[Application Starts] --> CHECK{DEV_MODE=true?}
    CHECK -->|Yes| MOCK[Create MockRepository<br/>with atom storage]
    CHECK -->|No| DB{DATABASE_URL<br/>defined?}
    DB -->|Yes| PG[Create PostgresRepository<br/>with DB connection]
    DB -->|No| ERROR[Throw Error:<br/>No database configured]
    
    MOCK --> SEED[Initialize seed data]
    SEED --> LOG1[Log: Running in DEV MODE]
    PG --> LOG2[Log: Running in PRODUCTION MODE]
    
    LOG1 --> READY[Application Ready]
    LOG2 --> READY
    ERROR --> FAIL[Application Fails to Start]
    
    style MOCK fill:#90EE90
    style PG fill:#87CEEB
    style ERROR fill:#FFB6C1
```

## Components and Interfaces

### 1. MockRepository (src/juridico/api/db/mock.clj)

Implementação em memória de todos os protocolos usando `defrecord` e `atom`.

```clojure
(ns juridico.api.db.mock
  (:require [juridico.api.db.protocols :refer [AuthRepository ProcessoRepository 
                                                 DocumentoRepository HistoricoRepository 
                                                 ClienteRepository]]
            [buddy.hashers :as hashers]
            [clojure.string :as str]))

(defrecord MockRepository [db-atom]
  ;; Implementa todos os protocolos
  AuthRepository
  ProcessoRepository
  DocumentoRepository
  HistoricoRepository
  ClienteRepository
  ;; ... implementações detalhadas abaixo
  )

;; Constructor function
(defn create-mock-repository []
  (let [db (atom (initialize-seed-data))]
    (println "[MOCK] 🚀 Repositório Mock inicializado com dados de seed")
    (->MockRepository db)))
```

### 2. Repository Factory (src/juridico/api/db/core.clj)

Novo arquivo que centraliza a criação de repositórios baseado em configuração.

```clojure
(ns juridico.api.db.core
  (:require [juridico.api.db.postgres :as pg]
            [juridico.api.db.mock :as mock]
            [environ.core :refer [env]]))

(defn create-repository
  "Factory function que retorna a implementação apropriada do repositório.
   Se DEV_MODE=true, retorna MockRepository.
   Caso contrário, retorna PostgresRepository."
  ([]
   (create-repository nil))
  ([tenant-id]
   (if (= "true" (env :dev-mode))
     (do
       (println "========================================")
       (println "🔧 MODO DE DESENVOLVIMENTO ATIVADO")
       (println "📦 Usando Repositório MOCK (in-memory)")
       (println "========================================")
       (mock/create-mock-repository))
     (do
       (println "========================================")
       (println "🚀 MODO DE PRODUÇÃO")
       (println "🗄️  Usando Repositório PostgreSQL")
       (println "========================================")
       (pg/->PostgresRepository @pg/datasource tenant-id)))))

(def repository
  "Instância global do repositório (lazy-loaded)."
  (delay (create-repository)))
```

### 3. Seed Data Module (src/juridico/api/db/seed.clj)

Módulo separado para geração de dados de seed, facilitando manutenção.

```clojure
(ns juridico.api.db.seed
  (:require [buddy.hashers :as hashers]
            [clojure.string :as str]))

(defn generate-seed-data []
  "Gera estrutura completa de dados de seed para desenvolvimento."
  {:tenants {...}
   :users {...}
   :clientes {...}
   :processos {...}
   :processo_documentos {...}
   :processo_historico {...}
   :counters {:tenant-id 1
              :user-id 4
              :cliente-id 3
              :processo-id 5
              :documento-id 8
              :historico-id 10}})
```

## Data Models

### Estrutura do Atom

O `atom` armazenará um mapa com todas as "tabelas" e contadores para IDs:

```clojure
{:tenants {1 {:id 1 :company_name "Demo Company" :subdomain "demo" ...}}
 :users {1 {:id 1 :email "admin@demo.com" :role "super-admin" ...}
         2 {:id 2 :email "master@demo.com" :role "master" ...}}
 :clientes {1 {:id 1 :tenant_id 1 :nome "João Silva" ...}}
 :processos {1 {:id 1 :tenant_id 1 :cliente_id 1 :numero_processo "0001234-56.2024.8.26.0100" ...}}
 :processo_documentos {1 {:id 1 :processo_id 1 :nome_arquivo "contrato.pdf" ...}}
 :processo_historico {1 {:id 1 :processo_id 1 :acao "criacao" ...}}
 
 ;; Contadores para auto-increment
 :counters {:tenant-id 1
            :user-id 4
            :cliente-id 3
            :processo-id 5
            :documento-id 8
            :historico-id 10}}
```

### Seed Data Details

#### Tenants
- **ID 1**: Demo Company (subdomain: "demo", operator_limit: 10)

#### Users
- **ID 1**: Super Admin (admin@demo.com / admin123)
- **ID 2**: Master User (master@demo.com / master123)
- **ID 3**: Operador 1 (operador1@demo.com / operador123)
- **ID 4**: Operador 2 (operador2@demo.com / operador123)

#### Clientes
- **ID 1**: João Silva (CPF: 123.456.789-00)
- **ID 2**: Maria Santos (CPF: 987.654.321-00)
- **ID 3**: Empresa XYZ Ltda (CNPJ: 12.345.678/0001-90)

#### Processos
- **ID 1-5**: Processos variados (cível, trabalhista, criminal) com diferentes status

#### Documentos
- **ID 1-8**: Documentos PDF/DOCX vinculados aos processos

#### Histórico
- **ID 1-10**: Entradas de histórico (criação, edição, mudança de status)

## Error Handling

### Validações Implementadas no Mock

1. **Email Duplicado**: Lançar exceção ao tentar criar usuário com email existente
2. **CPF/CNPJ Duplicado**: Lançar exceção ao tentar criar cliente com documento existente
3. **Número de Processo Duplicado**: Lançar exceção ao tentar criar processo com número existente
4. **Tenant Isolation**: Garantir que queries filtrem por tenant_id
5. **Soft Delete**: Filtrar registros com `deleted_at` não nulo
6. **Not Found**: Retornar `nil` quando registro não existe (comportamento idêntico ao PostgreSQL)

### Exemplo de Validação

```clojure
(defn- validate-unique-email! [db-atom email]
  (when (some #(= (:email %) email) (vals (:users @db-atom)))
    (throw (ex-info "Email já cadastrado"
                    {:type :duplicate-email
                     :email email}))))
```

## Testing Strategy

### Unit Tests

Criar testes para o MockRepository que validem:

1. **CRUD Operations**: Todas as operações básicas funcionam corretamente
2. **Auto-increment**: IDs são gerados sequencialmente
3. **Timestamps**: created_at e updated_at são adicionados automaticamente
4. **Soft Deletes**: Registros deletados não aparecem em queries normais
5. **Validations**: Constraints são respeitadas (emails únicos, etc.)
6. **Pagination**: Paginação retorna resultados corretos
7. **Search**: Busca case-insensitive funciona
8. **Tenant Isolation**: Dados de um tenant não vazam para outro

### Integration Tests

Testes que validem a integração com handlers:

1. **Authentication Flow**: Login funciona com usuários de seed
2. **CRUD via API**: Endpoints funcionam com mock repository
3. **Tenant Context**: Middleware de tenant funciona corretamente
4. **Error Responses**: Erros retornam status codes corretos

### Manual Testing

Checklist para testes manuais:

- [ ] Iniciar aplicação com `DEV_MODE=true`
- [ ] Fazer login com usuários de seed
- [ ] Criar, editar e deletar clientes
- [ ] Criar, editar e deletar processos
- [ ] Upload de documentos (mock de storage também necessário)
- [ ] Visualizar histórico de processos
- [ ] Alternar para modo produção e verificar que PostgreSQL é usado

## Implementation Details

### Helper Functions

```clojure
;; Auto-increment ID
(defn- next-id! [db-atom counter-key]
  (let [current (get-in @db-atom [:counters counter-key])]
    (swap! db-atom update-in [:counters counter-key] inc)
    (inc current)))

;; Add timestamps
(defn- add-timestamps [data]
  (let [now (java.time.Instant/now)]
    (assoc data
           :created_at now
           :updated_at now)))

;; Filter soft-deleted
(defn- filter-active [records]
  (filter #(nil? (:deleted_at %)) (vals records)))

;; Case-insensitive search
(defn- matches-search? [text search-term]
  (when (and text search-term)
    (str/includes? (str/lower-case (str text))
                   (str/lower-case search-term))))

;; Paginate results
(defn- paginate [items page per-page]
  (let [offset (* (dec page) per-page)]
    {:items (take per-page (drop offset items))
     :total (count items)
     :page page
     :per-page per-page}))
```

### Example Implementation: find-all-clientes

```clojure
(find-all-clientes [this tenant-id opts]
  (println "[MOCK] 📋 Listando clientes - tenant:" tenant-id "opts:" opts)
  (let [{:keys [page per-page search]} opts
        page (or page 1)
        per-page (or per-page 20)
        
        ;; Filtra clientes do tenant e ativos
        all-clientes (->> (vals (:clientes @db-atom))
                          (filter #(= (:tenant_id %) tenant-id))
                          (filter #(nil? (:deleted_at %))))
        
        ;; Aplica busca se fornecida
        filtered (if search
                   (filter #(or (matches-search? (:nome %) search)
                               (matches-search? (:cpf_cnpj %) search)
                               (matches-search? (:email %) search))
                           all-clientes)
                   all-clientes)
        
        ;; Ordena por nome
        sorted (sort-by :nome filtered)
        
        ;; Pagina resultados
        result (paginate sorted page per-page)]
    
    (println "[MOCK] ✅ Retornando" (count (:items result)) "de" (:total result) "clientes")
    {:clientes (:items result)
     :total (:total result)
     :page page
     :per-page per-page}))
```

## Configuration

### Environment Variables

```bash
# .env.development (para modo dev)
DEV_MODE=true
# DATABASE_URL não é necessária em modo dev

# .env.production (para modo produção)
DEV_MODE=false
DATABASE_URL=postgresql://user:pass@host:5432/dbname
```

### Scripts

#### dev-mock.sh (Linux/Mac)
```bash
#!/bin/bash
export DEV_MODE=true
echo "🔧 Iniciando em MODO DE DESENVOLVIMENTO (Mock Repository)"
echo "📦 Dados em memória - não persistem entre reinícios"
echo ""
echo "👤 Credenciais de acesso:"
echo "   Super Admin: admin@demo.com / admin123"
echo "   Master User: master@demo.com / master123"
echo "   Operador 1:  operador1@demo.com / operador123"
echo ""
lein run
```

#### dev-mock.ps1 (Windows)
```powershell
$env:DEV_MODE="true"
Write-Host "🔧 Iniciando em MODO DE DESENVOLVIMENTO (Mock Repository)" -ForegroundColor Green
Write-Host "📦 Dados em memória - não persistem entre reinícios" -ForegroundColor Yellow
Write-Host ""
Write-Host "👤 Credenciais de acesso:" -ForegroundColor Cyan
Write-Host "   Super Admin: admin@demo.com / admin123"
Write-Host "   Master User: master@demo.com / master123"
Write-Host "   Operador 1:  operador1@demo.com / operador123"
Write-Host ""
lein run
```

## Migration Path

### Fase 1: Implementação Base
1. Criar `db/seed.clj` com dados de seed
2. Criar `db/mock.clj` com implementação básica
3. Criar `db/core.clj` com factory function
4. Atualizar `core.clj` para usar factory

### Fase 2: Implementação Completa
5. Implementar todos os métodos dos protocolos
6. Adicionar validações e error handling
7. Adicionar logging detalhado
8. Criar scripts de inicialização

### Fase 3: Testes e Documentação
9. Escrever testes unitários
10. Escrever testes de integração
11. Criar documentação de uso
12. Testar manualmente todos os fluxos

## Limitations and Considerations

### Limitações do Modo Mock

1. **Dados não persistem**: Ao reiniciar a aplicação, todos os dados são perdidos
2. **Performance**: Para grandes volumes de dados, pode ser mais lento que banco real
3. **Concorrência**: Não simula locks ou transações distribuídas
4. **Storage**: Upload de arquivos precisa de mock separado (não coberto nesta spec)
5. **Queries complexas**: Joins e agregações complexas podem ter comportamento ligeiramente diferente

### Quando NÃO usar modo mock

- Testes de performance
- Testes de carga
- Validação de queries SQL complexas
- Testes de migração de banco
- Debugging de problemas específicos do PostgreSQL/CockroachDB

### Boas Práticas

1. **Sempre testar em produção antes de deploy**: Mock é para desenvolvimento, não substitui testes com banco real
2. **Manter seed data atualizado**: Quando schema muda, atualizar dados de seed
3. **Documentar diferenças**: Se mock se comporta diferente do PostgreSQL, documentar claramente
4. **Usar para testes rápidos**: Ideal para TDD e desenvolvimento iterativo
5. **Não commitar DEV_MODE=true**: Manter apenas em `.env.development` local

## Future Enhancements

1. **Persistência opcional**: Salvar estado do atom em arquivo JSON para persistir entre reinícios
2. **Mock de Storage**: Implementar mock para Cloudflare R2 (upload de arquivos)
3. **Seed customizável**: Permitir carregar diferentes conjuntos de dados de seed
4. **Performance monitoring**: Adicionar métricas de performance do mock
5. **Query logging**: Logar todas as "queries" executadas para debugging
6. **Snapshot/Restore**: Permitir salvar e restaurar estado do atom
