# 🔧 Configurar Cloudflare R2 Storage

**Data:** 07/11/2025  
**Status:** Guia de Configuração

---

## ✅ O Que Já Está Implementado

O código para upload de documentos com Cloudflare R2 já está **100% implementado**! 

Você só precisa:
1. Criar conta no Cloudflare
2. Configurar as variáveis de ambiente
3. Fazer deploy

---

## 📋 Passo 1: Criar Conta e Bucket (10 minutos)

### 1.1 Criar Conta Cloudflare

1. Acesse: https://dash.cloudflare.com/sign-up
2. Crie uma conta gratuita
3. Verifique seu email

### 1.2 Ativar R2

1. No dashboard, clique em **R2** no menu lateral
2. Clique em **Purchase R2 Plan**
3. Selecione o plano **Workers Free** (grátis!)
4. Confirme (não vai cobrar nada)

### 1.3 Criar Bucket

1. Clique em **Create bucket**
2. Configure:
   - **Nome:** `erp-advocacia-documents`
   - **Região:** Automatic
3. Clique em **Create bucket**

### 1.4 Gerar API Token

1. No menu R2, clique em **Manage R2 API Tokens**
2. Clique em **Create API token**
3. Configure:
   - **Token name:** `erp-advocacia-uploader`
   - **Permissions:** Object Read & Write
   - **TTL:** Never expire
4. Clique em **Create API Token**

### 1.5 Copiar Credenciais

Você verá uma tela com 3 informações. **Copie e salve:**

```
Access Key ID: xxxxxxxxxxxxxxxxxxxxxxxx
Secret Access Key: yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy
Endpoint for S3 Clients: https://xxxxxxxxx.r2.cloudflarestorage.com
```

⚠️ **IMPORTANTE:** Você só verá essas credenciais UMA VEZ! Salve em local seguro.

---

## 🔐 Passo 2: Configurar Variáveis de Ambiente

### 2.1 Desenvolvimento Local

**Criar arquivo:** `.env` (na raiz do projeto backend)

```bash
# Database
DATABASE_URL=sua-database-url-aqui

# JWT
JWT_SECRET=seu-jwt-secret-aqui

# Cloudflare R2 Storage
R2_ACCESS_KEY_ID=cole-seu-access-key-id-aqui
R2_SECRET_ACCESS_KEY=cole-seu-secret-access-key-aqui
R2_ENDPOINT=cole-seu-endpoint-aqui
R2_BUCKET=erp-advocacia-documents
```

**Exemplo real:**
```bash
R2_ACCESS_KEY_ID=a1b2c3d4e5f6g7h8i9j0
R2_SECRET_ACCESS_KEY=k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6
R2_ENDPOINT=https://abc123def456.r2.cloudflarestorage.com
R2_BUCKET=erp-advocacia-documents
```

### 2.2 Produção (Render)

1. Acesse seu projeto no Render
2. Vá em **Environment**
3. Adicione as variáveis:

```
R2_ACCESS_KEY_ID = [seu-access-key-id]
R2_SECRET_ACCESS_KEY = [seu-secret-access-key]
R2_ENDPOINT = [seu-endpoint]
R2_BUCKET = erp-advocacia-documents
```

4. Clique em **Save Changes**
5. O Render vai fazer redeploy automático

---

## 🧪 Passo 3: Testar Localmente

### 3.1 Instalar Dependências

```bash
cd erp-advocacia-api
lein deps
```

### 3.2 Iniciar Backend

```bash
lein run
```

### 3.3 Testar Upload

```bash
# Via curl
curl -X POST \
  http://localhost:3001/api/tenant/processos/1/documentos \
  -H "Authorization: Bearer SEU_TOKEN" \
  -F "file=@/caminho/para/documento.pdf"
```

### 3.4 Verificar no R2

1. Acesse Cloudflare Dashboard → R2
2. Abra seu bucket
3. Veja a estrutura:
   ```
   tenants/
   └── 1/
       └── processos/
           └── 123/
               └── 1699999999_documento.pdf
   ```

---

## 🚀 Passo 4: Deploy em Produção

### 4.1 Commit e Push

```bash
git add .
git commit -m "feat: add Cloudflare R2 storage for document uploads"
git push
```

### 4.2 Configurar no Render

1. Acesse Render Dashboard
2. Vá no seu backend
3. **Environment** → Adicione as 4 variáveis R2
4. **Save Changes**
5. Aguarde redeploy (~5 minutos)

### 4.3 Testar em Produção

1. Acesse seu sistema
2. Vá para um processo
3. Faça upload de um documento
4. ✅ Veja o documento aparecer na lista
5. Clique em download
6. ✅ Arquivo baixa corretamente

---

## 📊 Monitoramento

### Ver Uso do R2

1. Acesse Cloudflare Dashboard → R2
2. Veja métricas:
   - Storage usado (de 10GB grátis)
   - Número de objetos
   - Operações (uploads/downloads)

### Alertas

Configure alertas quando chegar perto do limite:
- 8GB usado (80%)
- 9GB usado (90%)

---

## 🔒 Segurança

### Boas Práticas Implementadas

✅ **Isolamento por Tenant**
- Cada tenant tem sua pasta separada
- Impossível acessar arquivos de outro tenant

✅ **Validações**
- Tipo de arquivo (PDF, DOC, DOCX, JPG, PNG)
- Tamanho máximo (10MB)
- Autenticação JWT obrigatória

✅ **URLs Temporárias**
- Downloads via presigned URLs
- Válidas por apenas 1 hora
- Não é possível acessar diretamente

✅ **Soft Delete**
- Arquivos deletados do R2
- Metadados mantidos no banco (auditoria)

---

## 💰 Custos

### Free Tier (Permanente)

- ✅ 10GB storage - GRÁTIS
- ✅ 1 milhão uploads/mês - GRÁTIS
- ✅ 10 milhões downloads/mês - GRÁTIS
- ✅ Egress ilimitado - GRÁTIS

### Estimativa de Uso

**100 processos/mês com 5 documentos cada:**
- Storage: 1GB (500 docs × 2MB)
- Uploads: 500/mês
- Downloads: 2.000/mês

**Custo:** $0/mês 🎉

### Quando Vai Custar?

Só vai custar se passar de:
- 10GB de storage
- 1 milhão de uploads/mês
- 10 milhões de downloads/mês

**Para um escritório médio, isso levaria anos!**

---

## 🆘 Troubleshooting

### Erro: "R2_ACCESS_KEY_ID not configured"

**Causa:** Variável de ambiente não configurada

**Solução:**
1. Verifique se adicionou no `.env` (local)
2. Verifique se adicionou no Render (produção)
3. Reinicie o servidor

### Erro: "Upload failed"

**Causa:** Credenciais inválidas ou bucket não existe

**Solução:**
1. Verifique se as credenciais estão corretas
2. Verifique se o bucket existe no Cloudflare
3. Verifique se o endpoint está correto

### Erro: "File too large"

**Causa:** Arquivo maior que 10MB

**Solução:**
1. Reduza o tamanho do arquivo
2. Ou aumente o limite no código (não recomendado)

### Erro: "Invalid file type"

**Causa:** Tipo de arquivo não permitido

**Solução:**
1. Use apenas: PDF, DOC, DOCX, JPG, PNG
2. Ou adicione mais tipos no código

---

## ✅ Checklist de Configuração

- [ ] Criar conta Cloudflare
- [ ] Ativar R2
- [ ] Criar bucket
- [ ] Gerar API token
- [ ] Copiar credenciais
- [ ] Adicionar variáveis no `.env` (local)
- [ ] Testar upload local
- [ ] Verificar arquivo no R2
- [ ] Adicionar variáveis no Render
- [ ] Fazer deploy
- [ ] Testar upload em produção
- [ ] Testar download em produção

---

## 📚 Recursos

- [Cloudflare R2 Docs](https://developers.cloudflare.com/r2/)
- [R2 Pricing](https://developers.cloudflare.com/r2/pricing/)
- [Dashboard R2](https://dash.cloudflare.com/?to=/:account/r2)

---

## 🎉 Pronto!

Depois de configurar, você terá:

✅ Upload de documentos funcionando
✅ Download seguro com URLs temporárias
✅ 10GB de storage grátis
✅ Downloads ilimitados grátis
✅ Isolamento por tenant
✅ Auditoria completa

**Custo total: $0/mês** 🎉

---

**Criado em:** 07/11/2025  
**Status:** Guia completo de configuração  
**Tempo de setup:** ~15 minutos
