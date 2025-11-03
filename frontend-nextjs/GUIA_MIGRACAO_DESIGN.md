# 🔄 Guia de Migração para o Novo Design

## 📋 Checklist de Migração

### ✅ Já Migrado
- [x] Design System Base (globals.css)
- [x] Componentes UI (Button, Card, Input, StatusBadge)
- [x] LoginScreen component
- [x] DashboardLayout component
- [x] Super Admin Login page
- [x] TenantsTableModern component
- [x] Font Awesome integrado

### 🔄 Pendente de Migração
- [ ] Super Admin Dashboard (substituir page.tsx por page-modern.tsx)
- [ ] Tenant Login page
- [ ] Tenant Dashboard page
- [ ] Página de Processos
- [ ] Página de Clientes
- [ ] Página de Operadores
- [ ] Modais existentes (CreateTenantModal, EditTenantModal, etc.)

## 🚀 Como Migrar uma Página

### Passo 1: Identificar a Página

Exemplo: `frontend-nextjs/src/app/dashboard/page.tsx`

### Passo 2: Importar Componentes do Design System

```tsx
// Antes
import styles from './page.module.css';

// Depois
import { DashboardLayout, StatCard, Card, Button } from '@/components';
import styles from './page.module.css';
```

### Passo 3: Substituir Layout

```tsx
// Antes
<div className={styles.container}>
  <header>...</header>
  <main>...</main>
</div>

// Depois
<DashboardLayout
  navItems={[
    { href: '/dashboard', icon: 'fas fa-chart-line', label: 'Dashboard' },
    { href: '/processos', icon: 'fas fa-folder', label: 'Processos' }
  ]}
  userName={userName}
  userRole={userRole}
  onLogout={handleLogout}
>
  {/* Conteúdo aqui */}
</DashboardLayout>
```

### Passo 4: Substituir Componentes

#### Botões
```tsx
// Antes
<button className={styles.button} onClick={handleClick}>
  Clique Aqui
</button>

// Depois
<Button variant="primary" glow onClick={handleClick}>
  Clique Aqui
</Button>
```

#### Cards
```tsx
// Antes
<div className={styles.card}>
  <div className={styles.cardHeader}>Título</div>
  <div className={styles.cardBody}>Conteúdo</div>
</div>

// Depois
<Card hover>
  <CardHeader>Título</CardHeader>
  <CardBody>Conteúdo</CardBody>
</Card>
```

#### Inputs
```tsx
// Antes
<div className={styles.formGroup}>
  <label>Email</label>
  <input type="email" value={email} onChange={e => setEmail(e.target.value)} />
</div>

// Depois
<Input
  type="email"
  label="Email"
  value={email}
  onChange={e => setEmail(e.target.value)}
  icon={<i className="fas fa-envelope"></i>}
/>
```

#### Status Badges
```tsx
// Antes
<span className={styles.statusActive}>Ativo</span>

// Depois
<StatusBadge 
  status="active"
  icon={<i className="fas fa-check-circle"></i>}
>
  Ativo
</StatusBadge>
```

### Passo 5: Atualizar CSS

Remova estilos que agora são fornecidos pelo design system:

```css
/* Pode remover: */
.button { ... }
.card { ... }
.input { ... }

/* Manter apenas estilos específicos da página: */
.pageHeader { ... }
.customLayout { ... }
```

### Passo 6: Testar Responsividade

1. Abra a página no navegador
2. Teste em diferentes tamanhos:
   - Mobile (< 768px)
   - Tablet (768px - 1023px)
   - Desktop (≥ 1024px)
3. Verifique dark mode (se disponível no navegador)

## 📝 Exemplo Completo: Migração do Dashboard

### Antes (dashboard/page.tsx)

```tsx
'use client';

import { useState } from 'react';
import styles from './dashboard.module.css';

export default function DashboardPage() {
  const [data, setData] = useState([]);

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <h1>Dashboard</h1>
        <button onClick={handleLogout}>Sair</button>
      </header>

      <main className={styles.main}>
        <div className={styles.stats}>
          <div className={styles.statCard}>
            <span>156</span>
            <span>Processos</span>
          </div>
        </div>

        <div className={styles.card}>
          <h2>Processos Recentes</h2>
          <table>...</table>
        </div>
      </main>
    </div>
  );
}
```

### Depois (dashboard/page.tsx)

```tsx
'use client';

import { useState } from 'react';
import { DashboardLayout, StatCard, Card, CardHeader, CardBody } from '@/components';
import styles from './dashboard.module.css';

export default function DashboardPage() {
  const [data, setData] = useState([]);

  const navItems = [
    { href: '/dashboard', icon: 'fas fa-chart-line', label: 'Dashboard' },
    { href: '/processos', icon: 'fas fa-folder', label: 'Processos' },
    { href: '/clientes', icon: 'fas fa-users', label: 'Clientes' }
  ];

  const handleLogout = async () => {
    await fetch('/api/auth/logout', { method: 'POST' });
    window.location.href = '/login';
  };

  return (
    <DashboardLayout
      navItems={navItems}
      userName="João Silva"
      userRole="Administrador"
      onLogout={handleLogout}
    >
      <div className={styles.pageHeader}>
        <h2 className={styles.pageTitle}>Dashboard</h2>
        <p className={styles.pageSubtitle}>Visão geral do escritório</p>
      </div>

      <div className={styles.statsGrid}>
        <StatCard
          icon="fas fa-folder"
          value={156}
          label="Processos Ativos"
          color="blue"
        />
        <StatCard
          icon="fas fa-users"
          value={89}
          label="Clientes"
          color="green"
        />
        <StatCard
          icon="fas fa-user-tie"
          value={12}
          label="Operadores"
          color="orange"
        />
      </div>

      <Card hover>
        <CardHeader>
          <div className={styles.cardTitle}>
            <i className="fas fa-clock"></i>
            <span>Processos Recentes</span>
          </div>
        </CardHeader>
        <CardBody>
          <table className={styles.table}>...</table>
        </CardBody>
      </Card>
    </DashboardLayout>
  );
}
```

### CSS Atualizado (dashboard.module.css)

```css
/* Remover estilos genéricos, manter apenas específicos */

.pageHeader {
  margin-bottom: 32px;
}

.pageTitle {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.pageSubtitle {
  font-size: 14px;
  color: var(--text-secondary);
}

.statsGrid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.cardTitle {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
}

.table {
  width: 100%;
  /* estilos específicos da tabela */
}

/* Mobile */
@media (max-width: 767px) {
  .statsGrid {
    grid-template-columns: 1fr;
  }
}
```

## 🎯 Prioridades de Migração

### Alta Prioridade
1. **Super Admin Dashboard** - Página principal do sistema
2. **Tenant Dashboard** - Experiência do usuário final
3. **Login Pages** - Primeira impressão

### Média Prioridade
4. **Páginas de Listagem** (Processos, Clientes, Operadores)
5. **Modais** (Create, Edit, Delete)

### Baixa Prioridade
6. **Páginas de Detalhes**
7. **Páginas de Configuração**

## ⚠️ Cuidados ao Migrar

### ✅ Fazer
- Testar em todos os tamanhos de tela
- Verificar acessibilidade (tab navigation, focus states)
- Manter funcionalidades existentes
- Usar componentes do design system sempre que possível
- Documentar mudanças significativas

### ❌ Não Fazer
- Não remover funcionalidades existentes
- Não quebrar APIs ou integrações
- Não ignorar responsividade
- Não criar estilos duplicados
- Não misturar design antigo com novo na mesma página

## 🔧 Ferramentas Úteis

### Verificar Responsividade
```bash
# Chrome DevTools
F12 > Toggle Device Toolbar (Ctrl+Shift+M)
```

### Verificar Dark Mode
```bash
# Chrome DevTools
F12 > Console > Executar:
document.documentElement.setAttribute('data-color-scheme', 'dark')
```

### Verificar Acessibilidade
```bash
# Chrome DevTools
F12 > Lighthouse > Accessibility
```

## 📞 Suporte

Se encontrar problemas durante a migração:

1. Consulte `DESIGN_SYSTEM.md` para documentação completa
2. Veja exemplos em `page-modern.tsx`
3. Verifique os componentes em `src/components/`
4. Teste com diferentes dados e estados

## ✨ Benefícios da Migração

- ✅ Interface moderna e profissional
- ✅ Responsividade total
- ✅ Dark mode automático
- ✅ Melhor experiência do usuário
- ✅ Código mais limpo e manutenível
- ✅ Componentes reutilizáveis
- ✅ Performance otimizada

---

**Boa sorte com a migração! 🚀**
