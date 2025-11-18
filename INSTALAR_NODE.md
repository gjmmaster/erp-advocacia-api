# ⚠️ Node.js Não Encontrado

## 🔍 Problema Detectado

O Node.js não está instalado ou não está no PATH do sistema.

---

## ✅ Solução: Instalar Node.js

### 1️⃣ Baixar Node.js

Acesse: **https://nodejs.org/**

Baixe a versão **LTS (Long Term Support)** - recomendada para a maioria dos usuários.

### 2️⃣ Instalar

1. Execute o instalador baixado
2. **IMPORTANTE:** Marque a opção "Add to PATH" durante a instalação
3. Clique em "Next" até finalizar
4. Reinicie o terminal/PowerShell após a instalação

### 3️⃣ Verificar Instalação

Abra um **novo** terminal e execute:

```cmd
node --version
npm --version
```

Deve mostrar as versões instaladas (ex: v20.x.x e 10.x.x).

---

## 🔄 Depois de Instalar

### 1. Instalar Dependências do Frontend

```cmd
cd frontend-nextjs
npm install
cd ..
```

### 2. Rodar o Projeto

```cmd
start-dev.bat
```

---

## 🆘 Ainda Não Funciona?

### Verificar se Node está no PATH

```cmd
where node
```

Se não retornar nada, o Node não está no PATH.

### Adicionar Node ao PATH Manualmente

1. Procure por "Variáveis de Ambiente" no Windows
2. Clique em "Variáveis de Ambiente"
3. Em "Variáveis do Sistema", encontre "Path"
4. Clique em "Editar"
5. Adicione o caminho do Node.js (geralmente `C:\Program Files\nodejs`)
6. Clique em "OK" e reinicie o terminal

---

## 📞 Alternativa: Usar Apenas o Backend

Se você só quer testar o backend (API):

```cmd
set DEV_MODE=true
lein run
```

Acesse: http://localhost:3000/health

---

**Criado em:** 17 de Novembro de 2025
