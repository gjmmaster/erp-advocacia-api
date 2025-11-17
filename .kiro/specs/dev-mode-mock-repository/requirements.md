# Requirements Document

## Introduction

Este documento define os requisitos para implementar um modo de desenvolvimento que permite executar a aplicação sem dependência de banco de dados, utilizando um repositório mock baseado em `atom` (in-memory). O objetivo é acelerar o ciclo de desenvolvimento, facilitar testes locais e permitir que desenvolvedores trabalhem sem necessidade de configurar infraestrutura de banco de dados.

A implementação seguirá o padrão de repositório (Repository Pattern) com inversão de dependência, onde o código da aplicação depende apenas dos protocolos definidos, não das implementações concretas. Isso permite trocar facilmente entre a implementação PostgreSQL (produção) e a implementação Mock (desenvolvimento) sem alterar a lógica de negócio.

## Requirements

### Requirement 1: Implementação do Repositório Mock

**User Story:** Como desenvolvedor, quero uma implementação mock de todos os repositórios usando `atom` para armazenamento em memória, para que eu possa desenvolver e testar funcionalidades sem depender de um banco de dados real.

#### Acceptance Criteria

1. WHEN o sistema é iniciado em modo dev THEN o repositório mock deve implementar todos os protocolos existentes (`AuthRepository`, `ProcessoRepository`, `DocumentoRepository`, `HistoricoRepository`, `ClienteRepository`)
2. WHEN dados são inseridos no repositório mock THEN eles devem ser armazenados em um `atom` e permanecer disponíveis durante a sessão
3. WHEN o repositório mock é criado THEN ele deve inicializar com dados de seed (tenant padrão, usuário admin, alguns clientes e processos de exemplo)
4. WHEN operações CRUD são executadas THEN o repositório mock deve simular comportamento idêntico ao PostgreSQL (incluindo auto-incremento de IDs, validações, soft deletes)
5. IF uma operação viola constraints (ex: email duplicado, CPF duplicado) THEN o repositório mock deve lançar exceções similares ao banco real

### Requirement 2: Configuração de Ambiente para Modo Dev

**User Story:** Como desenvolvedor, quero poder alternar entre modo de desenvolvimento (mock) e modo de produção (PostgreSQL) através de variáveis de ambiente, para que eu possa escolher facilmente qual implementação usar.

#### Acceptance Criteria

1. WHEN a variável de ambiente `DEV_MODE` está definida como `true` THEN o sistema deve usar o repositório mock
2. WHEN a variável de ambiente `DEV_MODE` está ausente ou definida como `false` THEN o sistema deve usar o repositório PostgreSQL
3. WHEN o sistema inicia THEN deve logar claramente qual modo está sendo usado (DEV ou PRODUCTION)
4. IF `DEV_MODE=true` e `DATABASE_URL` não está definida THEN o sistema deve iniciar normalmente com o repositório mock
5. WHEN o modo dev é ativado THEN todas as rotas da API devem funcionar normalmente sem modificações

### Requirement 3: Dados de Seed para Desenvolvimento

**User Story:** Como desenvolvedor, quero que o repositório mock seja inicializado com dados de exemplo realistas, para que eu possa testar fluxos completos imediatamente sem precisar criar dados manualmente.

#### Acceptance Criteria

1. WHEN o repositório mock é inicializado THEN deve criar automaticamente:
   - 1 tenant padrão com subdomínio "demo"
   - 1 super-admin com email "admin@demo.com" e senha "admin123"
   - 1 usuário master do tenant com email "master@demo.com" e senha "master123"
   - 2 usuários operadores com emails "operador1@demo.com" e "operador2@demo.com"
   - 3 clientes de exemplo com dados completos
   - 5 processos de exemplo vinculados aos clientes
   - 10 entradas de histórico distribuídas entre os processos
   - 8 documentos de exemplo vinculados aos processos
2. WHEN dados de seed são criados THEN devem incluir variedade de status (ativo, arquivado, em andamento)
3. WHEN dados de seed são criados THEN devem incluir timestamps realistas (datas variadas nos últimos 6 meses)
4. WHEN o sistema reinicia em modo dev THEN os dados de seed devem ser recriados (estado limpo a cada reinício)

### Requirement 4: Simulação de Comportamento de Banco de Dados

**User Story:** Como desenvolvedor, quero que o repositório mock simule comportamentos específicos do banco de dados (como auto-incremento, timestamps automáticos, soft deletes), para que o código funcione identicamente em dev e produção.

#### Acceptance Criteria

1. WHEN um registro é criado THEN o repositório mock deve gerar automaticamente um ID sequencial único
2. WHEN um registro é criado THEN o repositório mock deve adicionar automaticamente `created_at` com timestamp atual
3. WHEN um registro é atualizado THEN o repositório mock deve atualizar automaticamente `updated_at` com timestamp atual
4. WHEN um soft delete é executado THEN o repositório mock deve definir `deleted_at` ao invés de remover o registro
5. WHEN queries são executadas THEN o repositório mock deve filtrar automaticamente registros com `deleted_at` não nulo (exceto se explicitamente solicitado)
6. WHEN paginação é solicitada THEN o repositório mock deve retornar resultados no mesmo formato que o PostgreSQL (`:clientes`, `:total`, `:page`, `:per-page`)
7. WHEN buscas com ILIKE são executadas THEN o repositório mock deve fazer busca case-insensitive

### Requirement 5: Logging e Debugging

**User Story:** Como desenvolvedor, quero ver logs claros indicando quando operações são executadas no repositório mock, para que eu possa debugar facilmente e entender o fluxo de dados.

#### Acceptance Criteria

1. WHEN qualquer operação de repositório é executada em modo dev THEN deve logar a operação com prefixo "[MOCK]"
2. WHEN dados são inseridos THEN deve logar os dados sendo inseridos (exceto senhas)
3. WHEN queries são executadas THEN deve logar os parâmetros da query
4. WHEN o repositório mock é inicializado THEN deve logar um resumo dos dados de seed criados
5. IF uma operação falha THEN deve logar o erro com contexto suficiente para debugging

### Requirement 6: Scripts de Inicialização

**User Story:** Como desenvolvedor, quero scripts convenientes para iniciar a aplicação em modo dev, para que eu não precise lembrar de configurar variáveis de ambiente manualmente.

#### Acceptance Criteria

1. WHEN executo `./dev-mock.sh` (ou `dev-mock.ps1` no Windows) THEN o backend deve iniciar em modo dev com repositório mock
2. WHEN executo `./dev-full-mock.sh` THEN o backend e frontend devem iniciar simultaneamente em modo dev
3. WHEN os scripts são executados THEN devem exibir claramente que estão rodando em modo MOCK
4. WHEN os scripts são executados THEN devem exibir as credenciais de acesso dos usuários de seed
5. IF o script detecta que o backend já está rodando THEN deve avisar e não iniciar outra instância

### Requirement 7: Compatibilidade com Código Existente

**User Story:** Como desenvolvedor, quero que a implementação mock seja totalmente compatível com o código existente, para que eu não precise modificar handlers, middleware ou lógica de negócio.

#### Acceptance Criteria

1. WHEN o repositório mock é usado THEN todos os handlers existentes devem funcionar sem modificações
2. WHEN o repositório mock é usado THEN o middleware de autenticação deve funcionar normalmente
3. WHEN o repositório mock é usado THEN as rotas de API devem retornar respostas no mesmo formato
4. WHEN testes automatizados são executados THEN devem poder usar o repositório mock sem alterações
5. IF um novo protocolo é adicionado no futuro THEN deve ser claro onde adicionar a implementação mock correspondente

### Requirement 8: Documentação

**User Story:** Como desenvolvedor novo no projeto, quero documentação clara sobre como usar o modo dev com repositório mock, para que eu possa começar a desenvolver rapidamente.

#### Acceptance Criteria

1. WHEN leio a documentação THEN deve explicar claramente a diferença entre modo dev e produção
2. WHEN leio a documentação THEN deve listar todos os usuários de seed com suas credenciais
3. WHEN leio a documentação THEN deve explicar como alternar entre mock e PostgreSQL
4. WHEN leio a documentação THEN deve incluir exemplos de como adicionar novos dados de seed
5. WHEN leio a documentação THEN deve explicar as limitações do modo mock (ex: dados não persistem entre reinícios)
