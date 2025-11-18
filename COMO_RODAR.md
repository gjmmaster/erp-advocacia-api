# 🚀 Como Rodar o Projeto

## ⚡ Modo Rápido (Recomendado para Desenvolvimento)

### 1️⃣ Iniciar Tudo (Modo Mock - Sem Banco de Dados)

#### Windows

Basta dar **duplo clique** no arquivo:

```
start-dev.bat
```

Ou executar no PowerShell:

```powershell
.\dev-mock.ps1
```

#### Linux/Mac

Execute no terminal:

```bash
./dev-mock.sh
```

Isso vai:
- ✅ Verificar se as portas 3000 e 3001 estão disponíveis
- ✅ Iniciar o backend na porta 3000 (modo mock - dados em memória)
- ✅ Aguardar 3 segundos para o backend inicializar
- ✅ Iniciar o frontend na porta 3001
- ✅ Exibir URLs de acesso e credenciais

**Acesse:** http://localhost:3001

---

### 2️⃣ Parar Tudo

#### Windows

Basta dar **duplo clique** no arquivo:

```
stop-dev.bat
```

Ou feche as janelas do PowerShell que foram abertas.

#### Linux/Mac

Pressione `Ctrl+C` no terminal onde o script está rodando.

Isso vai parar todos os processos Java (backend) e Node (frontend).

---

## 🗄️ Modo com Banco de Dados (PostgreSQL)

Se você já configurou o PostgreSQL e quer usar dados persistentes:

### 1️⃣ Certifique-se que o PostgreSQL está rodando

```cmd
psql -l
```

### 2️⃣ Inicie os serviços

```cmd
start-dev-db.bat
```

---

## 👤 Credenciais de Acesso

### Modo Mock (start-dev.bat)

**Master User:**
- Email: `master@demo.com`
- Senha: `master123`

**Operador 1:**
- Email: `operador1@demo.com`
- Senha: `operador123`

**Operador 2:**
- Email: `operador2@demo.com`
- Senha: `operador123`

**Super Admin:**
- Email: `admin@demo.com`
- Senha: `admin123`

### Modo com Banco (start-dev-db.bat)

Use as credenciais que você configurou no banco de dados.

---

## 🔍 Verificar se está Rodando

Abra o navegador em:

- **Frontend:** http://localhost:3001
- **Backend:** http://localhost:3000/health (deve retornar "OK")

---

## ❌ Problemas Comuns

### "npm não é reconhecido"

**Causa:** Node.js não está instalado.

**Solução:** Instale o Node.js: https://nodejs.org/

### "lein não é reconhecido"

**Causa:** Leiningen não está instalado.

**Solução:** 
- **Windows:** Baixe em https://leiningen.org/
- **Mac:** `brew install leiningen`
- **Linux:** Siga instruções em https://leiningen.org/

### "Porta já em uso" ou "Backend/Frontend já está rodando"

**Causa:** Outro processo está usando as portas 3000 ou 3001.

**Solução:**
- **Windows:** Execute `stop-dev.bat` para parar os serviços antigos
- **Linux/Mac:** Pressione `Ctrl+C` no terminal ou use `lsof -ti:3000 | xargs kill` e `lsof -ti:3001 | xargs kill`

### Backend não inicia

**Causa:** Java não está instalado ou versão incompatível.

**Solução:** Verifique se o Java está instalado:

```cmd
java -version
```

Instale Java 11 ou superior se necessário.

### Frontend não inicia

**Causa:** Dependências do Node.js não estão instaladas.

**Solução:** Instale as dependências:

```cmd
cd frontend-nextjs
npm install
```

### "Diretório frontend-nextjs não encontrado"

**Causa:** Script está sendo executado do diretório errado.

**Solução:** Execute o script da raiz do projeto (onde está o arquivo `project.clj`).

### Dados não aparecem no modo mock

**Causa:** Isso é esperado! Dados em modo mock não persistem entre reinícios.

**Solução:** Use o modo com banco de dados se precisar de persistência.

---

## 📚 Mais Informações

- **Documentação Completa:** [README.md](./README.md)
- **Guia de Desenvolvimento:** [README_DEV.md](./README_DEV.md)
- **Setup Inicial:** [docs/guias/SETUP_INICIAL.md](./docs/guias/SETUP_INICIAL.md)

---

**Criado em:** 17 de Novembro de 2025  
**Versão:** 1.0
