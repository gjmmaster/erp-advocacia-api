# 🚀 Próximos Passos Imediatos

**Data:** 07/11/2025  
**Progresso Atual:** 75%  
**Meta:** 100% em 1-2 semanas

---

## ✅ O Que Acabamos de Fazer

- ✅ Página de detalhes do cliente
- ✅ Página de edição do cliente
- ✅ CRUD completo de Clientes (100%)
- ✅ CRUD completo de Processos (100%)

**Progresso:** 68% → 75% (+7%)

---

## 🎯 Próximos 4 Passos (25% restante)

### 1️⃣ Upload de Documentos (Prioridade ALTA)

**Por quê?** Funcionalidade mais esperada pelos usuários.

**O que fazer:**
- Criar componente de upload com drag-and-drop
- Integrar com backend (endpoints já existem)
- Lista de documentos por processo
- Download e delete de documentos

**Onde implementar:**
- `frontend-nextjs/src/app/dashboard/processos/[id]/page.tsx`
- Adicionar seção "Documentos" na página de detalhes

**Tempo estimado:** 2-3 horas

**Referência:**
- Backend já tem endpoints: `/api/tenant/processos/:id/documentos`
- Usar `FormData` para upload
- Validar tipo (PDF, DOC, DOCX, JPG, PNG) e tamanho (< 10MB)

---

### 2️⃣ Timeline de Histórico (Prioridade ALTA)

**Por quê?** Importante para auditoria e transparência.

**O que fazer:**
- Criar componente de timeline vertical
- Mostrar histórico de alterações do processo
- Formatação amigável de datas
- Ícones por tipo de ação

**Onde implementar:**
- `frontend-nextjs/src/app/dashboard/processos/[id]/page.tsx`
- Adicionar seção "Histórico" na página de detalhes

**Tempo estimado:** 1-2 horas

**Referência:**
- Backend já tem endpoint: `/api/tenant/processos/:id/historico`
- Mostrar: data, usuário, ação, campo alterado, valores

---

### 3️⃣ Filtro de Arquivados (Prioridade MÉDIA)

**Por quê?** Melhora organização da lista de processos.

**O que fazer:**
- Adicionar toggle "Mostrar Arquivados" nos filtros
- Por padrão, ocultar processos arquivados
- Badge "Arquivado" nos processos arquivados

**Onde implementar:**
- `frontend-nextjs/src/app/dashboard/processos/page.tsx`
- Adicionar ao componente de filtros existente

**Tempo estimado:** 30 minutos

**Referência:**
- Adicionar parâmetro `include_archived=true` na query
- CSS para badge já existe

---

### 4️⃣ Controle de Permissões (Prioridade MÉDIA)

**Por quê?** Segurança e preparação para múltiplos operadores.

**O que fazer:**
- Criar hook `usePermissions`
- Ocultar botões sem permissão
- Mensagens de acesso negado
- Read-only para super-admin em impersonation

**Onde implementar:**
- `frontend-nextjs/src/hooks/usePermissions.ts` (criar)
- Aplicar em todas as páginas com botões de ação

**Tempo estimado:** 1-2 horas

**Referência:**
- Verificar `user.role` do JWT
- Master: todas as permissões
- Operador: read + create (configurável)
- Super-admin impersonating: read-only

---

## 📅 Cronograma Sugerido

### Hoje (07/11)
- ✅ Deploy das páginas de cliente
- ✅ Testar fluxo completo

### Amanhã (08/11)
- [ ] Implementar upload de documentos
- [ ] Testar upload

### Sábado (09/11)
- [ ] Implementar timeline de histórico
- [ ] Implementar filtro de arquivados

### Domingo (10/11)
- [ ] Implementar controle de permissões
- [ ] Testes finais

### Segunda (11/11)
- [ ] Documentação final
- [ ] Deploy em produção
- [ ] 🎉 Sistema 100% completo!

---

## 🛠️ Como Implementar Cada Feature

### 1. Upload de Documentos

**Passo 1:** Criar componente de upload

```typescript
// frontend-nextjs/src/components/DocumentUpload.tsx
'use client';

import { useState } from 'react';

interface Props {
  processoId: string;
  onUploadComplete: () => void;
}

export default function DocumentUpload({ processoId, onUploadComplete }: Props) {
  const [uploading, setUploading] = useState(false);

  const handleUpload = async (file: File) => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch(`/api/tenant/processos/${processoId}/documentos`, {
      method: 'POST',
      body: formData,
    });

    if (response.ok) {
      onUploadComplete();
    }
  };

  return (
    <div>
      <input
        type="file"
        onChange={(e) => e.target.files?.[0] && handleUpload(e.target.files[0])}
        accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
      />
    </div>
  );
}
```

**Passo 2:** Adicionar na página de detalhes

```typescript
// Em frontend-nextjs/src/app/dashboard/processos/[id]/page.tsx
import DocumentUpload from '@/components/DocumentUpload';

// Adicionar seção:
<div className={styles.section}>
  <h2>Documentos</h2>
  <DocumentUpload processoId={id} onUploadComplete={loadDocumentos} />
  {/* Lista de documentos */}
</div>
```

---

### 2. Timeline de Histórico

**Passo 1:** Criar componente de timeline

```typescript
// frontend-nextjs/src/components/HistoricoTimeline.tsx
'use client';

interface HistoricoItem {
  id: string;
  acao: string;
  campo_alterado?: string;
  valor_anterior?: string;
  valor_novo?: string;
  user_name: string;
  created_at: string;
}

interface Props {
  items: HistoricoItem[];
}

export default function HistoricoTimeline({ items }: Props) {
  return (
    <div className="timeline">
      {items.map((item) => (
        <div key={item.id} className="timeline-item">
          <div className="timeline-icon">📝</div>
          <div className="timeline-content">
            <p><strong>{item.user_name}</strong> {item.acao}</p>
            {item.campo_alterado && (
              <p>
                {item.campo_alterado}: {item.valor_anterior} → {item.valor_novo}
              </p>
            )}
            <span className="timeline-date">{formatDate(item.created_at)}</span>
          </div>
        </div>
      ))}
    </div>
  );
}
```

**Passo 2:** Adicionar na página de detalhes

```typescript
// Carregar histórico
const [historico, setHistorico] = useState([]);

useEffect(() => {
  fetch(`/api/tenant/processos/${id}/historico`)
    .then(res => res.json())
    .then(data => setHistorico(data.historico));
}, [id]);

// Renderizar
<div className={styles.section}>
  <h2>Histórico</h2>
  <HistoricoTimeline items={historico} />
</div>
```

---

### 3. Filtro de Arquivados

**Passo 1:** Adicionar estado

```typescript
// Em frontend-nextjs/src/app/dashboard/processos/page.tsx
const [showArchived, setShowArchived] = useState(false);
```

**Passo 2:** Adicionar toggle

```typescript
<label>
  <input
    type="checkbox"
    checked={showArchived}
    onChange={(e) => setShowArchived(e.target.checked)}
  />
  Mostrar Arquivados
</label>
```

**Passo 3:** Atualizar query

```typescript
const url = `/api/tenant/processos?page=${page}&per-page=20&include_archived=${showArchived}`;
```

---

### 4. Controle de Permissões

**Passo 1:** Criar hook

```typescript
// frontend-nextjs/src/hooks/usePermissions.ts
import { useSession } from 'next-auth/react';

export function usePermissions() {
  const { data: session } = useSession();
  const user = session?.user;

  return {
    canCreate: user?.role === 'master' || user?.role === 'operador',
    canEdit: user?.role === 'master',
    canDelete: user?.role === 'master',
    isReadOnly: user?.impersonating === true,
  };
}
```

**Passo 2:** Usar nas páginas

```typescript
const { canEdit, canDelete } = usePermissions();

// Ocultar botões
{canEdit && <button>Editar</button>}
{canDelete && <button>Deletar</button>}
```

---

## 📦 Comandos Úteis

### Deploy
```bash
git add .
git commit -m "feat: add [feature name]"
git push
```

### Testar Localmente
```bash
# Backend
cd erp-advocacia-api
lein run

# Frontend
cd frontend-nextjs
npm run dev
```

### Ver Logs
```bash
# Render dashboard
# Backend: https://dashboard.render.com
# Frontend: https://dashboard.render.com
```

---

## ✅ Checklist de Implementação

### Upload de Documentos
- [ ] Criar componente DocumentUpload
- [ ] Criar componente DocumentList
- [ ] Integrar com API
- [ ] Validar tipo e tamanho
- [ ] Adicionar loading states
- [ ] Testar upload
- [ ] Testar download
- [ ] Testar delete

### Timeline de Histórico
- [ ] Criar componente HistoricoTimeline
- [ ] Integrar com API
- [ ] Formatar datas
- [ ] Adicionar ícones
- [ ] Testar visualização

### Filtro de Arquivados
- [ ] Adicionar toggle
- [ ] Atualizar query
- [ ] Adicionar badge
- [ ] Testar filtro

### Controle de Permissões
- [ ] Criar hook usePermissions
- [ ] Aplicar em todas as páginas
- [ ] Ocultar botões
- [ ] Adicionar mensagens
- [ ] Testar com diferentes roles

---

## 🎯 Meta Final

**Sistema 100% completo até 11/11/2025**

- ✅ Backend: 100%
- ✅ Frontend Core: 100%
- 🎯 Frontend Avançado: 75% → 100%
- 🎯 Documentação: 80% → 100%

**Você está quase lá! Faltam apenas 4 features! 🚀**

---

## 💡 Dicas

1. **Implemente uma feature por vez**
   - Commit após cada feature
   - Teste antes de continuar

2. **Use os componentes existentes**
   - Copie estilos de páginas similares
   - Reutilize lógica de loading/error

3. **Teste em produção**
   - Deploy após cada feature
   - Valide no ambiente real

4. **Documente conforme avança**
   - Atualize CHANGELOG.md
   - Anote problemas encontrados

---

**Boa sorte! Você consegue! 💪**

---

**Criado em:** 07/11/2025  
**Objetivo:** Guia prático para completar o sistema  
**Prazo:** 4 dias (07/11 - 11/11)
