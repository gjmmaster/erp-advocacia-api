# 📤 Guia - Upload com Cloudflare R2 (GRÁTIS)

**Data:** 07/11/2025  
**Status:** Guia de Implementação - Opção Gratuita ⭐

---

## 🎯 Por Que Cloudflare R2?

### Vantagens ✅

- **100% GRÁTIS** até 10GB de storage
- **GRÁTIS** para egress (download) - sem limites!
- **Compatível com S3** - mesma API
- **Rápido** - CDN global da Cloudflare
- **Simples** de configurar
- **Sem custos surpresa**

### Comparação com Outras Opções

| Serviço | Storage Grátis | Egress Grátis | Custo após limite |
|---------|----------------|---------------|-------------------|
| **Cloudflare R2** | 10GB | ∞ Ilimitado | $0.015/GB |
| AWS S3 | 5GB (12 meses) | 100GB | $0.023/GB + $0.09/GB egress |
| Cloudinary | 10GB | 25GB/mês | $99/mês |
| Supabase Storage | 1GB | 2GB | $0.021/GB |

**Vencedor:** Cloudflare R2 🏆

---

## 🚀 Setup Cloudflare R2 (10 minutos)

### Passo 1: Criar Conta Cloudflare

1. Acesse [cloudflare.com](https://cloudflare.com)
2. Crie uma conta (grátis)
3. Verifique seu email

### Passo 2: Criar Bucket R2

1. No dashboard, vá em **R2** no menu lateral
2. Clique em **Create bucket**
3. Configure:
   - **Nome:** `erp-advocacia-documents`
   - **Região:** Automatic (Cloudflare escolhe a melhor)
4. Clique em **Create bucket**

### Passo 3: Gerar API Token

1. No dashboard R2, vá em **Manage R2 API Tokens**
2. Clique em **Create API token**
3. Configure:
   - **Nome:** `erp-advocacia-uploader`
   - **Permissions:** 
     - ✅ Object Read & Write
   - **TTL:** Never expire (ou escolha um período)
4. Clique em **Create API Token**
5. **IMPORTANTE:** Copie e salve:
   - `Access Key ID`
   - `Secret Access Key`
   - `Endpoint URL` (algo como: `https://xxxxx.r2.cloudflarestorage.com`)

### Passo 4: Configurar Variáveis de Ambiente

**Backend (.env):**
```bash
# Cloudflare R2
R2_ACCESS_KEY_ID=xxxxxxxxxxxxx
R2_SECRET_ACCESS_KEY=yyyyyyyyyyyyyyy
R2_ENDPOINT=https://xxxxx.r2.cloudflarestorage.com
R2_BUCKET=erp-advocacia-documents
R2_PUBLIC_URL=https://pub-xxxxx.r2.dev  # Opcional, para URLs públicas
```

**Frontend (.env.local):**
```bash
NEXT_PUBLIC_R2_BUCKET=erp-advocacia-documents
```

---

## 💻 Implementação Backend (Clojure)

### Passo 1: Adicionar Dependência

**Modificar:** `project.clj`

```clojure
(defproject juridico-api "0.1.0"
  :dependencies [
    ;; ... outras dependências
    [amazonica "0.3.163"]  ; Funciona com R2 também!
  ])
```

Instalar:
```bash
lein deps
```

### Passo 2: Criar Namespace de Storage

**Criar arquivo:** `src/juridico/api/storage/r2.clj`

```clojure
(ns juridico.api.storage.r2
  (:require [amazonica.aws.s3 :as s3]
            [amazonica.core :as aws]
            [clojure.java.io :as io]))

;; Configuração do R2
(def r2-config
  {:access-key (System/getenv "R2_ACCESS_KEY_ID")
   :secret-key (System/getenv "R2_SECRET_ACCESS_KEY")
   :endpoint (System/getenv "R2_ENDPOINT")})

(def bucket (System/getenv "R2_BUCKET"))

(defn upload-file!
  "Faz upload de arquivo para Cloudflare R2.
   Retorna o caminho do arquivo no R2."
  [tenant-id processo-id file-name file-bytes content-type]
  (let [timestamp (System/currentTimeMillis)
        key (str "tenants/" tenant-id 
                 "/processos/" processo-id 
                 "/" timestamp "_" file-name)]
    
    ;; Upload para R2 (API compatível com S3)
    (aws/with-credential r2-config
      (s3/put-object
        :bucket-name bucket
        :key key
        :input-stream (io/input-stream file-bytes)
        :metadata {:content-type content-type
                   :content-length (count file-bytes)}))
    
    key))

(defn get-presigned-url
  "Gera URL temporária para download (válida por 1 hora)."
  [key]
  (aws/with-credential r2-config
    (s3/generate-presigned-url
      :bucket-name bucket
      :key key
      :expiration (+ (System/currentTimeMillis) (* 60 60 1000)))))

(defn delete-file!
  "Deleta arquivo do R2."
  [key]
  (aws/with-credential r2-config
    (s3/delete-object
      :bucket-name bucket
      :key key)))

(defn list-files
  "Lista arquivos de um processo."
  [tenant-id processo-id]
  (let [prefix (str "tenants/" tenant-id "/processos/" processo-id "/")]
    (aws/with-credential r2-config
      (s3/list-objects
        :bucket-name bucket
        :prefix prefix))))
```

### Passo 3: Atualizar Handler de Upload

**Modificar:** `src/juridico/api/handlers/processos.clj`

```clojure
(ns juridico.api.handlers.processos
  (:require [juridico.api.storage.r2 :as r2]
            [juridico.api.db.protocols :as p]
            [clojure.tools.logging :as log]))

(defn upload-documento-handler
  "Faz upload de arquivo para R2 e registra metadados."
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
    
    (log/info "Upload request:" {:tenant-id tenant-id 
                                  :processo-id processo-id 
                                  :file-name file-name
                                  :file-size file-size})
    
    ;; Validações
    (cond
      (not file-data)
      {:status 400
       :body {:error "Nenhum arquivo enviado"}}
      
      (> file-size (* 10 1024 1024))
      {:status 400
       :body {:error "Arquivo muito grande (máx. 10MB)"}}
      
      (not (re-matches #".*\.(pdf|doc|docx|jpg|jpeg|png)$" 
                       (clojure.string/lower-case file-name)))
      {:status 400
       :body {:error "Tipo de arquivo não permitido"}}
      
      :else
      (try
        ;; Upload para R2
        (let [r2-key (r2/upload-file! 
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
                             :caminho_storage r2-key
                             :uploaded_by user-id}
              
              result (p/create-documento! db-repo documento-data)]
          
          (log/info "Upload successful:" {:r2-key r2-key :documento-id (:id result)})
          
          {:status 201
           :body result})
        
        (catch Exception e
          (log/error e "Upload failed")
          {:status 500
           :body {:error "Erro ao fazer upload"
                  :message (.getMessage e)}})))))

(defn download-documento-handler
  "Gera URL temporária para download do R2."
  [{:keys [db-repo identity path-params]}]
  (let [tenant-id (:tenant-id identity)
        documento-id (Long/parseLong (:documento-id path-params))]
    
    (if-let [documento (p/find-documento-by-id db-repo documento-id)]
      ;; Validar que documento pertence ao tenant
      (if (= tenant-id (:tenant-id documento))
        (let [download-url (r2/get-presigned-url (:caminho_storage documento))]
          {:status 200
           :body {:download_url download-url
                  :nome_arquivo (:nome_arquivo documento)}})
        {:status 403
         :body {:error "Acesso negado"}})
      
      {:status 404
       :body {:error "Documento não encontrado"}})))

(defn delete-documento-handler
  "Soft delete de documento e remove do R2."
  [{:keys [db-repo identity path-params]}]
  (let [tenant-id (:tenant-id identity)
        documento-id (Long/parseLong (:documento-id path-params))]
    
    (if-let [documento (p/find-documento-by-id db-repo documento-id)]
      (if (= tenant-id (:tenant-id documento))
        (do
          ;; Deletar do R2
          (try
            (r2/delete-file! (:caminho_storage documento))
            (catch Exception e
              (log/warn e "Erro ao deletar arquivo do R2")))
          
          ;; Soft delete no banco
          (if (p/soft-delete-documento! db-repo documento-id)
            {:status 204}
            {:status 500
             :body {:error "Erro ao deletar documento"}}))
        {:status 403
         :body {:error "Acesso negado"}})
      
      {:status 404
       :body {:error "Documento não encontrado"}})))
```

### Passo 4: Configurar Multipart no Core

**Modificar:** `src/juridico/api/core.clj`

```clojure
(ns juridico.api.core
  (:require [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.params :refer [wrap-params]]
            ;; ... outros requires
            ))

;; Configurar tamanho máximo de upload (10MB)
(def multipart-config
  {:store (ring.middleware.multipart-params.byte-array/byte-array-store)
   :max-size (* 10 1024 1024)})  ; 10MB

;; Nas rotas
(def app-routes
  ["/api"
   ["/tenant"
    {:middleware [wrap-jwt-auth wrap-tenant-validation]}
    
    ["/processos"
     ["/:processo-id/documentos"
      ["" {:get {:handler processos/list-documentos-handler}
           :post {:handler processos/upload-documento-handler
                  :middleware [(fn [handler]
                                (wrap-multipart-params handler multipart-config))]}}]
      ["/:documento-id" 
       {:get {:handler processos/download-documento-handler}
        :delete {:handler processos/delete-documento-handler}}]]]]])
```

---

## 🌐 Frontend (Next.js)

### Passo 1: Atualizar API Route

**Modificar:** `frontend-nextjs/src/app/api/tenant/processos/[id]/documentos/route.ts`

```typescript
import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:3001';

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
        },
        body: formData,
      }
    );

    if (!response.ok) {
      const error = await response.json().catch(() => ({ error: 'Erro ao fazer upload' }));
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

### Passo 2: Atualizar Componente de Upload

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

    // Criar FormData
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch(`/api/tenant/processos/${processoId}/documentos`, {
      method: 'POST',
      body: formData,
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.error || 'Erro ao fazer upload do documento');
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

### Passo 3: Implementar Download

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

**Modificar:** `frontend-nextjs/src/components/DocumentList.tsx`

```typescript
const handleDownload = async (documento: Documento) => {
  try {
    // Buscar URL temporária do R2
    const response = await fetch(
      `/api/tenant/processos/${processoId}/documentos/${documento.id}/download`
    );

    if (!response.ok) {
      throw new Error('Erro ao gerar link de download');
    }

    const { download_url, nome_arquivo } = await response.json();

    // Criar link temporário e clicar
    const link = document.createElement('a');
    link.href = download_url;
    link.download = nome_arquivo;
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (err) {
    alert('Erro ao baixar documento');
    console.error(err);
  }
};
```

---

## 🧪 Testando

### 1. Testar Localmente

```bash
# Backend
cd erp-advocacia-api
lein run

# Frontend
cd frontend-nextjs
npm run dev
```

### 2. Testar Upload

1. Acesse um processo
2. Vá para seção "Documentos"
3. Arraste um arquivo PDF
4. ✅ Veja o upload acontecer
5. ✅ Documento aparece na lista

### 3. Verificar no R2

1. Acesse Cloudflare Dashboard → R2
2. Abra seu bucket
3. Veja a estrutura:
   ```
   tenants/
   └── 1/
       └── processos/
           └── 123/
               └── 1699999999_document.pdf
   ```

### 4. Testar Download

1. Clique no botão ⬇️
2. ✅ Arquivo baixa automaticamente

---

## 🚀 Deploy

### Backend (Render)

1. Vá em Dashboard → Environment
2. Adicione variáveis:
   ```
   R2_ACCESS_KEY_ID=xxxxx
   R2_SECRET_ACCESS_KEY=yyyyy
   R2_ENDPOINT=https://xxxxx.r2.cloudflarestorage.com
   R2_BUCKET=erp-advocacia-documents
   ```
3. Redeploy

### Frontend (Render/Vercel)

1. Adicione variável:
   ```
   NEXT_PUBLIC_R2_BUCKET=erp-advocacia-documents
   ```
2. Redeploy

---

## 💰 Custos

### Free Tier (Permanente!)

- **Storage:** 10GB grátis
- **Class A Operations:** 1 milhão/mês grátis (uploads)
- **Class B Operations:** 10 milhões/mês grátis (downloads)
- **Egress:** ∞ ILIMITADO E GRÁTIS!

### Após Free Tier

- **Storage:** $0.015/GB/mês
- **Class A:** $4.50/milhão
- **Class B:** $0.36/milhão

**Para 100 processos/mês:**
- Storage: 1GB = **GRÁTIS**
- Uploads: 100 = **GRÁTIS**
- Downloads: 1000 = **GRÁTIS**

**Total: $0/mês** 🎉

---

## 🔒 Segurança

### Configurar CORS no R2

1. No bucket, vá em **Settings**
2. Em **CORS Policy**, adicione:

```json
[
  {
    "AllowedOrigins": [
      "https://seu-frontend.onrender.com",
      "http://localhost:3000"
    ],
    "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
    "AllowedHeaders": ["*"],
    "ExposeHeaders": ["ETag"],
    "MaxAgeSeconds": 3000
  }
]
```

### Boas Práticas

1. **Nunca exponha as credenciais**
2. **Use presigned URLs** para download
3. **Valide tenant_id** antes de acessar
4. **Limite tamanho de arquivo**
5. **Valide tipo de arquivo**

---

## 📊 Monitoramento

### Dashboard R2

1. Acesse Cloudflare Dashboard → R2
2. Veja métricas:
   - Storage usado
   - Número de objetos
   - Operações (uploads/downloads)
   - Tráfego

---

## ✅ Checklist de Implementação

- [ ] Criar conta Cloudflare
- [ ] Criar bucket R2
- [ ] Gerar API token
- [ ] Configurar variáveis de ambiente
- [ ] Adicionar dependência amazonica
- [ ] Criar namespace r2.clj
- [ ] Atualizar handler de upload
- [ ] Configurar multipart
- [ ] Atualizar API routes do Next.js
- [ ] Atualizar componente de upload
- [ ] Implementar download
- [ ] Testar upload local
- [ ] Testar download local
- [ ] Deploy backend
- [ ] Deploy frontend
- [ ] Testar em produção

---

## 🆘 Troubleshooting

### Erro: "Invalid credentials"
- Verificar R2_ACCESS_KEY_ID e R2_SECRET_ACCESS_KEY
- Verificar se token não expirou

### Erro: "Bucket not found"
- Verificar nome do bucket
- Verificar R2_ENDPOINT

### Erro: "CORS error"
- Configurar CORS policy no bucket
- Adicionar origem do frontend

### Erro: "File too large"
- Verificar limite no backend (10MB)
- Verificar configuração multipart

---

## 📚 Recursos

- [Cloudflare R2 Docs](https://developers.cloudflare.com/r2/)
- [R2 Pricing](https://developers.cloudflare.com/r2/pricing/)
- [S3 API Compatibility](https://developers.cloudflare.com/r2/api/s3/)

---

## 🎉 Conclusão

**Cloudflare R2 é a melhor opção gratuita!**

✅ **10GB grátis** para sempre  
✅ **Egress ilimitado** e grátis  
✅ **Compatível com S3** - código similar  
✅ **Rápido** - CDN global  
✅ **Simples** de configurar  

**Custo total: $0/mês** 🎉

---

**Criado em:** 07/11/2025  
**Status:** Guia completo - Opção gratuita recomendada  
**Tempo de setup:** ~30 minutos
