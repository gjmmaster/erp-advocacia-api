# Implementation Plan - Fix Dev Mock Script

- [x] 1. Atualizar script dev-mock.sh para Linux/Mac


  - Adicionar verificação de porta 3000 usando `lsof -Pi :3000 -sTCP:LISTEN`
  - Adicionar verificação de porta 3001 usando `lsof -Pi :3001 -sTCP:LISTEN`
  - Modificar para iniciar backend em background se porta 3000 disponível
  - Adicionar sleep de 3 segundos após iniciar backend
  - Adicionar código para iniciar frontend em background se porta 3001 disponível
  - Atualizar mensagens para indicar "MODO DE DESENVOLVIMENTO COMPLETO"
  - Adicionar exibição de URLs (Backend: 3000, Frontend: 3001)
  - Adicionar trap para capturar Ctrl+C e encerrar ambos os processos
  - Adicionar comando wait para manter script rodando
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.1, 2.2, 2.3, 2.4, 2.5, 3.1, 3.2, 3.3, 3.4, 3.5, 5.1, 5.2_



- [ ] 2. Atualizar script dev-mock.ps1 para Windows
  - Adicionar verificação de porta 3000 usando `Get-NetTCPConnection -LocalPort 3000`
  - Adicionar verificação de porta 3001 usando `Get-NetTCPConnection -LocalPort 3001`
  - Modificar para iniciar backend em janela separada se porta 3000 disponível
  - Adicionar Start-Sleep de 3 segundos após iniciar backend
  - Adicionar código para iniciar frontend em janela separada se porta 3001 disponível
  - Atualizar mensagens coloridas usando Write-Host
  - Adicionar exibição de URLs (Backend: 3000, Frontend: 3001)

  - Adicionar instruções sobre como fechar janelas para parar serviços
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 2.4, 2.5, 3.1, 3.2, 3.3, 3.4, 4.1, 4.2, 4.3, 4.4, 4.5, 5.1, 5.2_

- [ ] 3. Adicionar tratamento de erros nos scripts
  - Adicionar verificação se lein está instalado (Linux/Mac)
  - Adicionar verificação se npm está instalado (Linux/Mac)
  - Adicionar verificação se diretório frontend-nextjs existe (Linux/Mac)


  - Adicionar mensagens de erro claras com instruções de instalação
  - Replicar verificações no script PowerShell (Windows)
  - _Requirements: 5.3_

- [ ] 4. Atualizar documentação README.md
  - Adicionar seção explicando que dev-mock.sh inicia backend e frontend


  - Listar URLs de acesso (Backend: 3000, Frontend: 3001)
  - Explicar diferença entre dev-mock.sh e outros scripts
  - Adicionar instruções para Windows (dev-mock.ps1)
  - Adicionar instruções sobre como parar serviços (Ctrl+C)




  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [ ] 5. Atualizar documentação COMO_RODAR.md
  - Atualizar seção de desenvolvimento para mencionar novo comportamento
  - Adicionar troubleshooting para problemas comuns (portas ocupadas)
  - Adicionar seção sobre requisitos (Leiningen, Node.js)
  - _Requirements: 6.1, 6.2, 6.5_

- [ ] 6. Testar script atualizado em diferentes cenários
  - Testar inicialização limpa (nenhum serviço rodando)
  - Testar com backend já rodando
  - Testar com frontend já rodando
  - Testar com ambos já rodando
  - Testar encerramento com Ctrl+C (Linux/Mac)
  - Testar script PowerShell no Windows
  - Verificar que aplicação funciona corretamente após inicialização
  - Fazer login e testar funcionalidades básicas
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.1, 2.2, 2.3, 2.4, 2.5, 4.1, 4.2, 4.3, 4.4, 4.5, 7.1, 7.2, 7.3, 7.4, 7.5_
