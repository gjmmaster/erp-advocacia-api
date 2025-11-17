# Design System - Legal ERP

## 📋 Visão Geral

Este design system foi criado com base no Perplexity Design System, adaptado para o Legal ERP. Ele oferece uma aparência moderna, profissional e responsiva com suporte a dark mode.

## 🎨 Características

- **Design Moderno**: Interface glassmorphism com gradientes e animações suaves
- **Responsivo**: Funciona perfeitamente em desktop, tablet e mobile
- **Dark Mode**: Suporte automático baseado nas preferências do sistema
- **Acessível**: Componentes seguem as melhores práticas de acessibilidade
- **Performático**: Animações otimizadas e transições suaves

## 🚀 Como Usar

### 1. Importar Componentes

```tsx
import { 
  Button, 
  Card, 
  Input, 
  StatusBadge,
  LoginScreen,
  DashboardLayout,
  StatCard
} from '@/components';
```

### 2. Componentes Disponíveis

#### Button
```tsx
<Button 
  variant="primary" // primary | secondary | outline
  size="md" // sm | md | lg
  fullWidth={false}
  glow={true}
  icon={<i className="fas fa-arrow-right"></i>}
  iconPosition="right" // left | right
>
  Clique Aqui
</Button>
```

#### Card
```tsx
<Card hover={true}>
  <CardHeader>
    <h3>Título do Card</h3>
  </CardHeader>
  <CardBody>
    <p>Conteúdo do card</p>
  </CardBody>
  <CardFooter>
    <Button>Ação</Button>
  </CardFooter>
</Card>
```

#### Input
```tsx
<Input
  type="email"
  label="Email"
  icon={<i className="fas fa-envelope"></i>}
  suffix=".meuerp.com"
  error="Mensagem de erro"
  required
/>
```

#### StatusBadge
```tsx
<StatusBadge 
  status="success" // success | error | warning | info | active | inactive
  icon={<i className="fas fa-check-circle"></i>}
>
  Ativo
</StatusBadge>
```

#### LoginScreen
```tsx
<LoginScreen
  onSubmit={async (email, password) => {
    // Lógica de login
  }}
  title="Login"
  subtitle="Sistema Legal ERP"
  isSuperAdmin={false}
/>
```

#### DashboardLayout
```tsx
<DashboardLayout
  navItems={[
    { href: '/dashboard', icon: 'fas fa-chart-line', label: 'Dashboard' },
    { href: '/processos', icon: 'fas fa-folder', label: 'Processos' }
  ]}
  userName="João Silva"
  userRole="Administrador"
  onLogout={() => {}}
>
  {/* Conteúdo do dashboard */}
</DashboardLayout>
```

#### StatCard
```tsx
<StatCard
  icon="fas fa-building"
  value={156}
  label="Total de Processos"
  color="blue" // blue | green | orange | purple
/>
```

## 🎨 Variáveis CSS

### Cores Principais
```css
--color-primary: var(--color-teal-500);
--color-primary-hover: var(--color-teal-600);
--color-primary-active: var(--color-teal-700);
--color-error: var(--color-red-500);
--color-success: var(--color-teal-500);
--color-warning: var(--color-orange-500);
```

### Espaçamento
```css
--space-4: 4px;
--space-8: 8px;
--space-12: 12px;
--space-16: 16px;
--space-24: 24px;
--space-32: 32px;
```

### Border Radius
```css
--radius-sm: 6px;
--radius-base: 8px;
--radius-md: 10px;
--radius-lg: 12px;
--radius-full: 9999px;
```

### Sombras
```css
--shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.04);
--shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.04);
--shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.04);
```

## 📱 Responsividade

O design system usa breakpoints padrão:

- **Mobile**: < 768px
- **Tablet**: 768px - 1023px
- **Desktop**: ≥ 1024px

### Exemplo de uso:
```css
/* Mobile */
@media (max-width: 767px) {
  .element {
    padding: 16px;
  }
}

/* Tablet */
@media (min-width: 768px) and (max-width: 1023px) {
  .element {
    padding: 24px;
  }
}

/* Desktop */
@media (min-width: 1024px) {
  .element {
    padding: 32px;
  }
}
```

## 🌙 Dark Mode

O dark mode é ativado automaticamente baseado nas preferências do sistema:

```css
@media (prefers-color-scheme: dark) {
  :root {
    --color-background: var(--color-charcoal-700);
    --color-text: var(--color-gray-200);
    /* ... outras variáveis */
  }
}
```

## 🔄 Migrando Páginas Existentes

### Antes (Antigo):
```tsx
<div className={styles.container}>
  <h1>Título</h1>
  <button onClick={handleClick}>Clique</button>
</div>
```

### Depois (Novo Design):
```tsx
<DashboardLayout navItems={navItems} onLogout={handleLogout}>
  <div className={styles.pageHeader}>
    <h2 className={styles.pageTitle}>Título</h2>
  </div>
  <Button variant="primary" glow onClick={handleClick}>
    Clique
  </Button>
</DashboardLayout>
```

## 📦 Estrutura de Arquivos

```
src/
├── components/
│   ├── ui/
│   │   ├── Button.tsx
│   │   ├── Button.module.css
│   │   ├── Card.tsx
│   │   ├── Card.module.css
│   │   ├── Input.tsx
│   │   ├── Input.module.css
│   │   ├── StatusBadge.tsx
│   │   ├── StatusBadge.module.css
│   │   └── index.ts
│   ├── LoginScreen.tsx
│   ├── LoginScreen.module.css
│   ├── Sidebar.tsx
│   ├── Sidebar.module.css
│   ├── DashboardHeader.tsx
│   ├── DashboardHeader.module.css
│   ├── DashboardLayout.tsx
│   ├── DashboardLayout.module.css
│   ├── StatCard.tsx
│   ├── StatCard.module.css
│   └── index.ts
└── app/
    └── globals.css (Design System CSS)
```

## 🎯 Próximos Passos

1. **Migrar páginas existentes** para usar o novo design system
2. **Criar componentes adicionais** conforme necessário (Modal, Toast, etc.)
3. **Adicionar testes** para os componentes
4. **Documentar padrões** de uso específicos do projeto

## 💡 Dicas

- Use os componentes do design system sempre que possível
- Mantenha consistência visual em todas as páginas
- Teste em diferentes tamanhos de tela
- Verifique o contraste de cores para acessibilidade
- Use ícones do Font Awesome 6.4.0

## 🐛 Problemas Conhecidos

- Nenhum no momento

## 📚 Referências

- [Perplexity Design System](https://www.perplexity.ai/)
- [Font Awesome Icons](https://fontawesome.com/icons)
- [CSS Variables](https://developer.mozilla.org/en-US/docs/Web/CSS/Using_CSS_custom_properties)
