# Requirements Document

## Introduction

Este documento define os requisitos para implementar melhorias críticas de segurança no backend do ERP para Advocacia multi-tenant. As melhorias foram identificadas através de uma análise de segurança detalhada e visam corrigir vulnerabilidades de severidade Alta e Média, fortalecendo a postura de segurança da aplicação sem comprometer a arquitetura existente que já é robusta.

O foco principal está em três áreas críticas:
1. Geração segura de senhas temporárias usando criptografia adequada
2. Gerenciamento robusto de segredos JWT com fail-fast em produção
3. Validação rigorosa de formato de subdomínios conforme RFC 1035

## Requirements

### Requirement 1: Geração Criptograficamente Segura de Senhas Temporárias

**User Story:** Como administrador do sistema, eu quero que as senhas temporárias sejam geradas usando métodos criptograficamente seguros, para que não possam ser previstas ou adivinhadas por atacantes.

#### Acceptance Criteria

1. WHEN o sistema provisiona um novo tenant THEN SHALL gerar a senha temporária usando um gerador de números aleatórios criptograficamente seguro (CSPRNG)
2. WHEN a senha temporária é gerada THEN SHALL ter no mínimo 12 caracteres alfanuméricos
3. WHEN a senha temporária é gerada THEN SHALL ter um espaço de possibilidades suficientemente grande para resistir a ataques de força bruta (mínimo 2^64 possibilidades)
4. IF a biblioteca buddy já está disponível no projeto THEN SHALL utilizar buddy.core.nonce para geração de bytes aleatórios seguros
5. WHEN a senha é convertida para formato legível THEN SHALL usar apenas caracteres alfanuméricos [a-zA-Z0-9]

### Requirement 2: Gerenciamento Seguro de Segredo JWT com Fail-Fast

**User Story:** Como engenheiro de segurança, eu quero que a aplicação se recuse a iniciar se o segredo JWT não estiver configurado em produção, para que não haja risco de usar uma chave padrão conhecida.

#### Acceptance Criteria

1. WHEN a aplicação inicia em ambiente de produção AND a variável JWT_SECRET não está definida THEN SHALL lançar uma exceção e impedir a inicialização
2. WHEN a aplicação inicia em ambiente de desenvolvimento AND a variável JWT_SECRET não está definida THEN SHALL permitir o uso de uma chave padrão com aviso no log
3. WHEN a variável de ambiente APP_ENV é "production" THEN SHALL validar obrigatoriamente a presença de JWT_SECRET
4. WHEN a validação de JWT_SECRET falha THEN SHALL exibir uma mensagem de erro clara indicando "ERRO CRÍTICO: A variável de ambiente JWT_SECRET não foi definida"
5. WHEN a aplicação inicia com sucesso THEN SHALL registrar no log se está usando chave padrão (desenvolvimento) ou chave configurada (produção)

### Requirement 3: Validação Rigorosa de Formato de Subdomínio

**User Story:** Como administrador do sistema, eu quero que os subdomínios sejam validados conforme as normas DNS (RFC 1035), para que não sejam criados subdomínios malformados que possam causar problemas de roteamento ou segurança.

#### Acceptance Criteria

1. WHEN um subdomínio é fornecido explicitamente via API THEN SHALL validar o formato usando regex compatível com RFC 1035
2. WHEN um subdomínio é gerado automaticamente a partir do nome da empresa THEN SHALL aplicar a mesma validação de formato
3. WHEN o formato do subdomínio é validado THEN SHALL aceitar apenas: letras minúsculas [a-z], dígitos [0-9] e hífens [-]
4. WHEN o formato do subdomínio é validado THEN SHALL garantir que não comece nem termine com hífen
5. WHEN o formato do subdomínio é validado THEN SHALL ter comprimento entre 1 e 63 caracteres
6. WHEN o formato do subdomínio é validado THEN SHALL rejeitar subdomínios vazios ou compostos apenas de caracteres especiais
7. IF a validação falhar THEN SHALL retornar erro HTTP 400 com mensagem clara indicando o formato esperado
8. WHEN a spec ::subdomain é definida THEN SHALL usar o padrão regex: `[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?`
