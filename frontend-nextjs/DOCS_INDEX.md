# 📚 Índice de Documentação - Next.js BFF

## 🎯 Para Começar

1. **[README.md](./README.md)** - Visão geral do projeto
2. **[QUICK_START.md](./QUICK_START.md)** - Início rápido (5 minutos)
3. **[COMMANDS.md](./COMMANDS.md)** - Comandos úteis

## 📖 Documentação Completa

### Migração
- **[MIGRATION_COMPLETE.md](./MIGRATION_COMPLETE.md)** - Visão geral da migração
- **[IMPLEMENTATION_STATUS.md](./IMPLEMENTATION_STATUS.md)** - Status detalhado (71%)
- **[../NEXTJS_MIGRATION_SUMMARY.md](../NEXTJS_MIGRATION_SUMMARY.md)** - Resumo executivo

### Testes
- **[MANUAL_TESTING_GUIDE.md](./MANUAL_TESTING_GUIDE.md)** - 15 cenários de teste
- **[jest.config.js](./jest.config.js)** - Configuração de testes
- **[jest.setup.js](./jest.setup.js)** - Setup de testes

### Especificações Técnicas
- **[.kiro/specs/nextjs-bff-migration/README.md](../.kiro/specs/nextjs-bff-migration/README.md)** - Resumo das specs
- **[.kiro/specs/nextjs-bff-migration/requirements.md](../.kiro/specs/nextjs-bff-migration/requirements.md)** - Requisitos
- **[.kiro/specs/nextjs-bff-migration/design.md](../.kiro/specs/nextjs-bff-migration/design.md)** - Design (Parte 1)
- **[.kiro/specs/nextjs-bff-migration/design-part2.md](../.kiro/specs/nextjs-bff-migration/design-part2.md)** - Design (Parte 2)
- **[.kiro/specs/nextjs-bff-migration/tasks.md](../.kiro/specs/nextjs-bff-migration/tasks.md)** - Tasks de implementação

## 🔍 Por Tópico

### Autenticação
- `src/lib/auth.ts` - Implementação JWT
- `src/middleware.ts` - Proteção de rotas
- `src/app/api/auth/` - API routes de auth

### Componentes
- `src/app/super-admin/login/` - Página de login
- `src/app/super-admin/dashboard/` - Dashboard
- `src/components/` - Componentes reutilizáveis

### API (BFF)
- `src/app/api/auth/` - Autenticação
- `src/app/api/admin/tenants/` - CRUD de tenants
- `src/lib/api.ts` - Cliente HTTP

### Tipos
- `src/types/tenant.ts` - Tipos de tenant
- `src/types/user.ts` - Tipos de usuário
- `src/types/api.ts` - Tipos de API

### Configuração
- `package.json` - Dependências
- `tsconfig.json` - TypeScript
- `next.config.js` - Next.js
- `.env.example` - Variáveis de ambiente

## 📊 Fluxo de Leitura Recomendado

### Para Desenvolvedores Novos
1. README.md
2. QUICK_START.md
3. MIGRATION_COMPLETE.md
4. Código fonte

### Para Testers
1. MANUAL_TESTING_GUIDE.md
2. COMMANDS.md
3. Testes unitários

### Para Arquitetos
1. .kiro/specs/nextjs-bff-migration/requirements.md
2. .kiro/specs/nextjs-bff-migration/design.md
3. IMPLEMENTATION_STATUS.md

### Para DevOps
1. package.json
2. next.config.js
3. .env.example
4. COMMANDS.md

## 🎓 Recursos Externos

- [Next.js Documentation](https://nextjs.org/docs)
- [React Documentation](https://react.dev)
- [TypeScript Documentation](https://www.typescriptlang.org/docs)
- [Jest Documentation](https://jestjs.io/docs/getting-started)

## 📞 Suporte

Se você não encontrou o que procura:
1. Verifique este índice novamente
2. Use a busca do editor (Ctrl+Shift+F)
3. Consulte as specs em `.kiro/specs/`
4. Abra uma issue no repositório
