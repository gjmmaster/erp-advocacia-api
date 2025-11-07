# 📤 Guia Completo - Upload de Arquivos

**Data:** 07/11/2025  
**Status:** Guia de Implementação

---

## 🎯 Visão Geral

Atualmente, o sistema registra apenas **metadados** dos documentos no banco de dados. Este guia mostra como implementar o **upload real** dos arquivos.

---

## 📁 Estrutura Atual

### O Que Já Está Implementado ✅

1. **Frontend:**
   - Componente `DocumentUpload` com drag-and-drop
   - Validação de tipo e tamanho
   - Interface de upload

2. **Backend:**
   - Endpoint `/api/tenant/processos/:id/documentos` (POST)
   - Salva metadados no banco de dados
   - Tabela `processo_documentos`

3. **Banco de Dados:**
   - Campo `caminho_storage` para guardar o path do arquivo
   - Campos `nome_arquivo`, `tipo_arquivo`, `tamanho_bytes`

### O Que Falta Implementar ⏳

- Upload real do arquivo para storage
- Download do arquivo
- Exclusão física do arquivo

---

## 🛠️ Opções de Implementação

### Opção 1: AWS S3 (Recomendado para Produção) ⭐

**Vantagens:**
- ✅ Escalável e confiável
- ✅ CDN integrado (CloudFront)
- ✅ Backup automático
- ✅ Controle de acesso (IAM)
- ✅ Custo baixo (~$0.023/GB/mês)

**Desvantagens:**
- ⚠️ Requer configuração AWS
- ⚠️ Custo adicional (mínimo)

---

### Opção 2: Cloudinary (Alternativa Simples)

**Vantagens:**
- ✅ Setup muito simples
- ✅ Free tier generoso (10GB)
- ✅ CDN incluído
- ✅ Transformações de imagem

**Desvantagens:**
- ⚠️ Mais caro que S3 em escala
- ⚠️ Focado em imagens/vídeos

---

### Opção 3: Filesystem Local (Apenas Desenvolvimento)

**Vantagens:**
- ✅ Zero configuração
- ✅ Grátis
- ✅ Simples para testar

**Desvantagens:**
- ❌ Não funciona em produção (Render)
- ❌ Não escalável
- ❌ Sem backup

---

## 🚀 Implementação com AWS S3

### Passo 1: Criar Bucket S3

1. Acesse [AWS Console](https://console.aws.amazon.com/s3/)
2. Clique em "Create bucket"
3. Configure:
   - **Nome:** `erp-advocacia-documents`
   - **Região:** `us-east-1` (ou mais próxima)
   - **Block Public Access:** Deixe marcado (segurança)
4. Clique em "Create bucket"

### Passo 2: Criar IAM User

1. Acesse [IAM Console](https://console.aws.amazon.com/iam/)
2. Vá em "Users" → "Add users"
3. Configure:
   - **Nome:** `erp-advocacia-uploader`
   - **Access type:** Programmatic access
4. Attach policy: `AmazonS3FullAccess` (ou crie uma custom)
5. Salve as credenciais:
   - `AWS_ACCESS_KEY_ID`
   - `AWS_SECRET_ACCESS_KEY`

### Passo 3: Configurar Variáveis de Ambiente

**Backend (.env):**
```bash
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=...
AWS_S3_BUCKET=erp-advocacia-documents
AWS_REGION=us-east-1
```

**Frontend (.env.local):**
```bash
NEXT_PUBLIC_AWS_S3_BUCKET=erp-advocacia-documents
NEXT_PUBLIC_AWS_REGION=us-east-1
```

### Passo 4: Instalar Dependências

**Backend (Clojure):**
```bash
# Adicionar ao project.clj
[amazonica "0.3.163"]  ; AWS SDK para Clojure
```

**Frontend (Next.js):**
```bash
cd frontend-nextjs
npm install aws-sdk
# ou
npm install @aws-sdk/client-s3
```

### Passo 5: Implementar Upload no Backend

**Criar arquivo:** `src/juridico/api/storage/s3.clj`

```clojure
(ns juridico.api.storage.s3
  (:require [amazonica.aws.s3 :as s3]
            [clojure.java.io :as io]))

(def bucket (System/getenv "AWS_S3_BUCKET"))
(def region (System/getenv "AWS_REGION"))

(defn upload-file!
  "Faz upload de arquivo para S3.
   Retorna o caminho do arquivo no S3."
  [tenant-id processo-id file-name file-bytes content-type]
  (let [key (str "tenants/" tenant-id 
                 "/processos/" processo-id 
                 "/" (System/currentTimeMillis) "_" file-name)]
    
    (s3/put-object
      :bucket-name bucket
      :key key
      :input-stream (io/input-stream file-bytes)
      :metadata {:content-type content-type
                 :content-length (count file-bytes)})
    
    key))

(defn get-presigned-url
  "Gera URL temporária para download (válida por 1 hora)."
  [key]
  (s3/generate-presigned-url
    :bucket-name bucket
    :key key
    :expiration (+ (System/currentTimeMillis) (* 60 60 1000))))

(defn delete-file!
  "Deleta arquivo do S3."
  [key]
  (s3/delete-object
    :bucket-name bucket
    :key key))
```

### Passo 6: Atualizar Handler de Upload

**Modificar:** `src/juridico/api/handlers/processos.clj`

```clojure
(ns juridico.api.handlers.processos
  (:require [juridico.api.storage.s3 :as s3]
            ;; ... outros requires
            ))

(defn upload-documento-handler
  "Faz upload de arquivo e registra metadados."
  [{:keys [db-repo identity path-params multipart-params]}]
  (let [tenant-id (:tenant-id identity)
        user-id (:user-id identity)
        processo-id (Long/parseLong (:processo-id path-params))
        
        ;; Extrair arquivo do multipart
        file-data (get multipart-params "file")
        file-name (:filename file-data)
        file-bytes (:bytes file-data)
        content-type (:content-type file-data)
        file-size (count file-bytes)]
    
    ;; Validações
    (cond
      (not file-data)
      {:status 400
       :body {:error "Nenhum arquivo enviado"}}
      
      (> file-size (* 10 1024 1024))
      {:status 400
       :body {:error "Arquivo muito grande (máx. 10MB)"}}
      
      :else
      (try
        ;; Upload para S3
        (let [s3-key (s3/upload-file! 
                       tenant-id 
                       processo-id 
                       file-name 
                       file-bytes 
                       content-type)
              
              ;; Salvar metadados no banco
              documento-data {:processo_id processo-id
                             :nome_arquivo file-name
                             :tipo_arquivo content-type
                             :tamanho_bytes file-size
                             :caminho_storage s3-key
                             :uploaded_by user-id}
              
              result (p/create-documento! db-repo documento-data)]
          
          {:status 201
           :body result})
        
        (catch Exception e
          {:status 500
           :body {:error "Erro ao fazer upload"
                  :message (.getMessage e)}})))))

(defn download-documento-handler
  "Gera URL temporária para download."
  [{:keys [db-repo identity path-params]}]
  (let [documento-id (Long/parseLong (:documento-id path-params))]
    
    (if-let [documento (p/find-documento-by-id db-repo documento-id)]
      (let [download-url (s3/get-presigned-url (:caminho_storage documento))]
        {:status 200
         :body {:download_url download-url}})
      
      {:status 404
       :body {:error "Documento não encontrado"}})))
```

### Passo 7: Atualizar Rotas

**Modificar:** `src/juridico/api/core.clj`

```clojure
;; Adicionar middleware para multipart
(require '[ring.middleware.multipart-params :refer [wrap-multipart-params]])

;; Nas rotas de documentos
["/:processo-id/documentos"
 ["" {:get {:handler processos/list-documentos-handler}
      :post {:handler processos/upload-documento-handler
             :middleware [wrap-multipart-params]}}]  ; ← Adicionar middleware
 ["/:documento-id" 
  {:get {:handler processos/download-documento-handler}
   :delete {:handler processos/delete-documento-handler}}]]
```

### Passo 8: Atualizar Frontend

**Modificar:** `frontend-nextjs/src/components/DocumentUpload.tsx`

```typescript
const handleUpload = async (file: File) => {
  setError('');
  
  const validationError = validateFile(file);
  if (validationError) {
    setError(validationError);
    return;
  }

  try {
    setUploading(true);

    // Criar FormData para enviar arquivo
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch(`/api/tenant/processos/${processoId}/documentos`, {
      method: 'POST',
      body: formData,  // ← Enviar FormData em vez de JSON
    });

    if (!response.ok) {
      throw new Error('Erro ao fazer upload do documento');
    }

    alert('Documento enviado com sucesso!');
    onUploadComplete();
    
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  } catch (err: any) {
    setError(err.message || 'Erro ao fazer upload');
    console.error(err);
  } finally {
    setUploading(false);
  }
};
```

**Modificar:** `frontend-nextjs/src/app/api/tenant/processos/[id]/documentos/route.ts`

```typescript
export async function POST(
  request: NextRequest,
  { params }: { params: { id: string } }
) {
  try {
    const cookieStore = cookies();
    const accessToken = cookieStore.get('access_token')?.value;

    if (!accessToken) {
      return NextResponse.json({ error: 'Não autenticado' }, { status: 401 });
    }

    // Pegar FormData do request
    const formData = await request.formData();

    // Fazer proxy para backend com FormData
    const response = await fetch(
      `${BACKEND_URL}/api/tenant/processos/${params.id}/documentos`,
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          // NÃO adicionar Content-Type, deixar o browser definir
        },
        body: formData,  // ← Enviar FormData
      }
    );

    if (!response.ok) {
      const error = await response.json();
      return NextResponse.json(error, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data, { status: 201 });
  } catch (error) {
    console.error('Error uploading documento:', error);
    return NextResponse.json(
      { error: 'Erro ao fazer upload' },
      { status: 500 }
    );
  }
}
```

### Passo 9: Implementar Download

**Modificar:** `frontend-nextjs/src/components/DocumentList.tsx`

```typescript
const handleDownload = async (documento: Documento) => {
  try {
    // Buscar URL temporária
    const response = await fetch(
      `/api/tenant/processos/${processoId}/documentos/${documento.id}/download`
    );

    if (!response.ok) {
      throw new Error('Erro ao gerar link de download');
    }

    const { download_url } = await response.json();

    // Abrir em nova aba
    window.open(download_url, '_blank');
  } catch (err) {
    alert('Erro ao baixar documento');
    console.error(err);
  }
};
```

**Criar:** `frontend-nextjs/src/app/api/tenant/processos/[id]/documentos/[documentoId]/download/route.ts`

```typescript
import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:3001';

export async function GET(
  request: NextRequest,
  { params }: { params: { id: string; documentoId: string } }
) {
  try {
    const cookieStore = cookies();
    const accessToken = cookieStore.get('access_token')?.value;

    if (!accessToken) {
      return NextResponse.json({ error: 'Não autenticado' }, { status: 401 });
    }

    const response = await fetch(
      `${BACKEND_URL}/api/tenant/processos/${params.id}/documentos/${params.documentoId}`,
      {
        headers: {
          'Authorization': `Bearer ${accessToken}`,
        },
      }
    );

    if (!response.ok) {
      const error = await response.json();
      return NextResponse.json(error, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('Error getting download URL:', error);
    return NextResponse.json(
      { error: 'Erro ao gerar link de download' },
      { status: 500 }
    );
  }
}
```

---

## 🧪 Testando

### 1. Testar Upload

```bash
# Via curl
curl -X POST \
  http://localhost:3001/api/tenant/processos/1/documentos \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@/path/to/document.pdf"
```

### 2. Verificar no S3

1. Acesse AWS Console → S3
2. Abra seu bucket
3. Veja a estrutura:
   ```
   tenants/
   └── 1/
       └── processos/
           └── 123/
               └── 1699999999_document.pdf
   ```

### 3. Testar Download

```bash
curl -X GET \
  http://localhost:3001/api/tenant/processos/1/documentos/1 \
  -H "Authorization: Bearer YOUR_TOKEN"

# Retorna:
{
  "download_url": "https://s3.amazonaws.com/..."
}
```

---

## 💰 Custos AWS S3

### Estimativa para 100 processos/mês:

- **Storage:** 1GB = $0.023/mês
- **Requests:** 1000 uploads = $0.005
- **Transfer:** 10GB download = $0.90

**Total:** ~$1/mês

### Free Tier (12 meses):
- 5GB storage
- 20.000 GET requests
- 2.000 PUT requests

---

## 🔒 Segurança

### Boas Práticas:

1. **Bucket Privado:**
   - Nunca deixe público
   - Use presigned URLs para download

2. **IAM Policy Restritiva:**
   ```json
   {
     "Version": "2012-10-17",
     "Statement": [{
       "Effect": "Allow",
       "Action": [
         "s3:PutObject",
         "s3:GetObject",
         "s3:DeleteObject"
       ],
       "Resource": "arn:aws:s3:::erp-advocacia-documents/*"
     }]
   }
   ```

3. **Validações:**
   - Tipo de arquivo
   - Tamanho máximo
   - Scan de vírus (opcional)

4. **Isolamento por Tenant:**
   - Cada tenant em sua pasta
   - Validar tenant_id antes de acessar

---

## 🚀 Deploy

### Render

1. Adicione variáveis de ambiente:
   - `AWS_ACCESS_KEY_ID`
   - `AWS_SECRET_ACCESS_KEY`
   - `AWS_S3_BUCKET`
   - `AWS_REGION`

2. Deploy normalmente

### Vercel (Frontend)

1. Adicione variáveis de ambiente no dashboard
2. Deploy normalmente

---

## 📝 Checklist de Implementação

- [ ] Criar bucket S3
- [ ] Criar IAM user
- [ ] Configurar variáveis de ambiente
- [ ] Instalar dependências
- [ ] Implementar upload no backend
- [ ] Implementar download no backend
- [ ] Atualizar frontend para enviar arquivo
- [ ] Implementar download no frontend
- [ ] Testar upload
- [ ] Testar download
- [ ] Testar delete
- [ ] Deploy em produção

---

## 🆘 Troubleshooting

### Erro: "Access Denied"
- Verificar IAM permissions
- Verificar bucket policy
- Verificar credenciais

### Erro: "File too large"
- Aumentar limite no backend
- Verificar configuração do servidor

### Erro: "Invalid file type"
- Verificar MIME type
- Adicionar tipo na lista de permitidos

---

## 📚 Recursos

- [AWS S3 Documentation](https://docs.aws.amazon.com/s3/)
- [Amazonica (Clojure AWS SDK)](https://github.com/mcohen01/amazonica)
- [AWS SDK for JavaScript](https://docs.aws.amazon.com/sdk-for-javascript/)

---

**Criado em:** 07/11/2025  
**Status:** Guia completo de implementação  
**Próximo passo:** Escolher opção de storage e implementar
