# Requirements Document

## Introduction

Este documento define os requisitos para corrigir o script `dev-mock.sh` (e sua versão Windows `dev-mock.ps1`) para que ele inicie tanto o backend quanto o frontend automaticamente, proporcionando uma experiência de desenvolvimento completa com um único comando. Atualmente, o script só inicia o backend, deixando o desenvolvedor sem acesso à interface web.

## Requirements

### Requirement 1: Iniciar Backend e Frontend Simultaneamente

**User Story:** Como desenvolvedor, quero que o comando `./dev-mock.sh` inicie tanto o backend quanto o frontend automaticamente, para que eu possa começar a desenvolver imediatamente sem precisar executar múltiplos comandos.

#### Acceptance Criteria

1. WHEN executo `./dev-mock.sh` THEN o backend deve iniciar na porta 3000 em modo mock
2. WHEN executo `./dev-mock.sh` THEN o frontend Next.js deve iniciar na porta 3001
3. WHEN ambos os serviços estão rodando THEN devo poder acessar a aplicação em `http://localhost:3001`
4. WHEN ambos os serviços estão rodando THEN o frontend deve conseguir se comunicar com o backend em `http://localhost:3000`
5. IF pressiono Ctrl+C THEN ambos os serviços devem ser encerrados graciosamente

### Requirement 2: Verificação de Portas Ocupadas

**User Story:** Como desenvolvedor, quero que o script verifique se as portas já estão em uso antes de tentar iniciar os serviços, para evitar erros e conflitos de porta.

#### Acceptance Criteria

1. WHEN executo o script THEN ele deve verificar se a porta 3000 está disponível
2. WHEN executo o script THEN ele deve verificar se a porta 3001 está disponível
3. IF a porta 3000 já está em uso THEN o script deve avisar e não tentar iniciar o backend
4. IF a porta 3001 já está em uso THEN o script deve avisar e não tentar iniciar o frontend
5. WHEN uma porta está ocupada THEN o script deve exibir mensagem clara indicando qual serviço já está rodando

### Requirement 3: Mensagens Informativas

**User Story:** Como desenvolvedor, quero ver mensagens claras sobre o que está acontecendo durante a inicialização, para que eu saiba quando os serviços estão prontos para uso.

#### Acceptance Criteria

1. WHEN o script inicia THEN deve exibir banner indicando modo de desenvolvimento completo
2. WHEN o backend está iniciando THEN deve exibir mensagem "🚀 Iniciando backend (porta 3000)..."
3. WHEN o frontend está iniciando THEN deve exibir mensagem "🌐 Iniciando frontend (porta 3001)..."
4. WHEN ambos os serviços estão prontos THEN deve exibir mensagem de sucesso com URLs de acesso
5. WHEN o script exibe informações THEN deve incluir as credenciais dos usuários de seed

### Requirement 4: Compatibilidade Windows

**User Story:** Como desenvolvedor Windows, quero que o script PowerShell `dev-mock.ps1` funcione da mesma forma que a versão Linux/Mac, para que eu tenha a mesma experiência de desenvolvimento.

#### Acceptance Criteria

1. WHEN executo `dev-mock.ps1` no Windows THEN deve iniciar backend e frontend
2. WHEN executo `dev-mock.ps1` THEN deve verificar portas usando comandos PowerShell nativos
3. WHEN executo `dev-mock.ps1` THEN deve abrir janelas separadas para backend e frontend
4. WHEN executo `dev-mock.ps1` THEN deve exibir mensagens coloridas usando Write-Host
5. IF pressiono Ctrl+C THEN os processos devem ser encerrados (ou instruções para fechar janelas)

### Requirement 5: Tempo de Espera para Inicialização

**User Story:** Como desenvolvedor, quero que o script aguarde o backend estar pronto antes de iniciar o frontend, para evitar erros de conexão durante a inicialização.

#### Acceptance Criteria

1. WHEN o backend é iniciado THEN o script deve aguardar 3-5 segundos antes de iniciar o frontend
2. WHEN o tempo de espera está em andamento THEN deve exibir mensagem indicando que está aguardando
3. IF o backend falhar ao iniciar THEN o script deve detectar e não iniciar o frontend
4. WHEN o frontend inicia THEN o backend já deve estar respondendo requisições

### Requirement 6: Documentação Atualizada

**User Story:** Como desenvolvedor novo no projeto, quero que a documentação explique claramente como usar o script atualizado, para que eu saiba qual comando executar para desenvolvimento.

#### Acceptance Criteria

1. WHEN leio o README.md THEN deve explicar que `dev-mock.sh` inicia backend e frontend
2. WHEN leio o README.md THEN deve listar as URLs de acesso (backend: 3000, frontend: 3001)
3. WHEN leio o README.md THEN deve explicar a diferença entre `dev-mock.sh` e `dev-full-mock.sh`
4. WHEN leio a documentação THEN deve incluir instruções para Windows (dev-mock.ps1)
5. WHEN leio a documentação THEN deve explicar como parar os serviços (Ctrl+C)

### Requirement 7: Manter Compatibilidade com Scripts Existentes

**User Story:** Como desenvolvedor, quero que os scripts existentes (`dev-full-mock.sh`, `dev.sh`, etc.) continuem funcionando, para que eu possa escolher qual usar dependendo da situação.

#### Acceptance Criteria

1. WHEN atualizo `dev-mock.sh` THEN `dev-full-mock.sh` deve continuar funcionando
2. WHEN atualizo `dev-mock.sh` THEN `dev.sh` (modo produção) deve continuar funcionando
3. WHEN atualizo `dev-mock.sh` THEN não deve quebrar outros scripts de desenvolvimento
4. IF um desenvolvedor prefere iniciar serviços separadamente THEN deve poder usar comandos individuais
5. WHEN todos os scripts existem THEN a documentação deve explicar quando usar cada um
