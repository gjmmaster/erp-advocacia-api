# 📊 Status dos Serviços

**Data:** 17 de Novembro de 2025  
**Hora:** Agora

---

## ✅ Backend (Porta 3000)

**Status:** ✅ **RODANDO**

- Processo: Java (PID 15156)
- Modo: Mock (dados em memória)
- URL: http://localhost:3000
- Health Check: http://localhost:3000/health

---

## ❌ Frontend (Porta 3001)

**Status:** ❌ **NÃO RODANDO**

**Motivo:** Node.js não está instalado ou não está no PATH do sistema.

---

## 🔧 Como Resolver

### Opção 1: Instalar Node.js (Recomendado)

1. **Baixe o Node.js:**
   - Acesse: https://nodejs.org/
   - Baixe a versão **LTS** (Long Term Support)

2. **Instale:**
   - Execute o instalador
   - ⚠️ **IMPORTANTE:** Marque "Add to PATH" durante a instalação
   - Clique em "Next" até finalizar

3. **Reinicie o terminal**

4. **Verifique a instalação:**
   ```cmd
   node --version
   npm --version
   ```

5. **Instale as dependências do frontend:**
   ```cmd
   cd frontend-nextjs
   npm install
   cd ..
   ```

6. **Rode o projeto:**
   ```cmd
   start-dev.bat
   ```

---

### Opção 2: Usar Apenas o Backend (Temporário)

Se você só quer testar a API por enquanto:

**Backend já está rodando!**

Teste em: http://localhost:3000/health

---

## 📋 Checklist

- [x] Backend instalado (Leiningen/Java)
- [x] Backend rodando (porta 3000)
- [ ] Node.js instalado
- [ ] Node.js no PATH
- [ ] Dependências do frontend instaladas
- [ ] Frontend rodando (porta 3001)

---

## 🆘 Precisa de Ajuda?

Leia os guias:
- **INSTALAR_NODE.md** - Como instalar o Node.js
- **COMO_RODAR.md** - Como rodar o projeto completo

---

## 🎯 Próximo Passo

**Instale o Node.js seguindo as instruções acima!**

Depois execute: `start-dev.bat`

---

**Atualizado:** Agora mesmo
