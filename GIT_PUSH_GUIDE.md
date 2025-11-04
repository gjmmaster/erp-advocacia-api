# Guia de Push - Gestão de Processos (Tasks 1-3)

## 📦 O Que Será Commitado

### Novos Arquivos (13)

**Migrations (6 arquivos):**
- `migrations/004_create_clientes_table.sql`
- `migrations/004_rollback_clientes_table.sql`
- `migrations/005_create_processos_tables.sql`
- `migrations/005_rollback_processos_tables.sql`
- `run_migration_clientes.sh`
- `run_migration_processos.sh`

**Documentação (4 arquivos):**
- `docs/DATABASE_SCHEMA.md`
- `PROGRESSO_GESTAO_PROCESSOS.md`
- `RESUMO_SESSAO_04_11_2025.md`
- `GIT_PUSH_GUIDE.md` (este arquivo)

**Spec (3 arquivos):**
- `.kiro/specs/gestao-processos/requirements.md`
- `.kiro/specs/gestao-processos/design.md`
- `.kiro/specs/gestao-processos/tasks.md`

### Arquivos Modificados (2)

- `src/juridico/api/db/protocols.clj` (adicionados 4 protocols)
- `CHANGELOG.md` (atualizado)

---

## 🚀 Comandos Git

### 1. Verificar Status

```bash
git status
```

Você deve ver 15 arquivos (13 novos + 2 modificados).

### 2. Adicionar Arquivos

```bash
# Adicionar todos os arquivos
git add .

# OU adicionar seletivamente
git add migrations/
git add docs/
git add src/juridico/api/db/protocols.clj
git add .kiro/specs/gestao-processos/
git add PROGRESSO_GESTAO_PROCESSOS.md
git add RESUMO_SESSAO_04_11_2025.md
git add CHANGELOG.md
git add GIT_PUSH_GUIDE.md
git add run_migration_*.sh
```

### 3. Verificar Arquivos Staged

```bash
git status
```

Todos os arquivos devem estar em verde (staged).

### 4. Fazer Commit

```bash
git commit -m "feat: add database schema and protocols for processo management (tasks 1-3)

- Add clientes table migration (004)
- Add processos, processo_documentos, processo_historico tables (005)
- Add 4 new protocols: ProcessoRepository, DocumentoRepository, HistoricoRepository, ClienteRepository
- Add complete database schema documentation
- Add gestao-processos spec (requirements, design, tasks)
- Update CHANGELOG with progress

Tasks completed: 1-3/30 (10%)
Phase: Backend Core - Database & Protocols"
```

### 5. Push para Repositório

```bash
# Push para branch atual
git push

# OU especificar branch
git push origin main
# OU
git push origin master
```

---

## 📋 Checklist Antes do Push

- [ ] Todos os arquivos foram adicionados (`git status` mostra tudo staged)
- [ ] Commit message está clara e descritiva
- [ ] Nenhum arquivo sensível (senhas, tokens) está sendo commitado
- [ ] Scripts de migration têm permissão de execução (`chmod +x`)
- [ ] Documentação está atualizada (CHANGELOG, PROGRESSO, RESUMO)

---

## 🔄 Se Precisar Desfazer

### Desfazer git add (antes do commit)

```bash
# Desfazer todos
git reset

# Desfazer arquivo específico
git reset HEAD <arquivo>
```

### Desfazer commit (antes do push)

```bash
# Desfazer último commit mantendo alterações
git reset --soft HEAD~1

# Desfazer último commit descartando alterações
git reset --hard HEAD~1
```

### Desfazer push (depois do push)

```bash
# Reverter commit específico
git revert <commit-hash>
git push
```

---

## 📊 Resumo do Commit

**Tipo:** Feature (feat)  
**Escopo:** Database + Protocols  
**Tasks:** 1-3 de 30  
**Progresso:** 10%  
**Arquivos:** 15 (13 novos + 2 modificados)  
**Linhas:** ~2000+ linhas adicionadas

**Impacto:**
- ✅ 4 novas tabelas no banco de dados
- ✅ 25 funções de protocol definidas
- ✅ Documentação completa
- ✅ Scripts de migration prontos

---

## 🎯 Próximos Passos Após Push

1. **Aplicar migrations no banco de dados**
   ```bash
   export DATABASE_URL='postgresql://...'
   ./run_migration_clientes.sh
   ./run_migration_processos.sh
   ```

2. **Validar tabelas criadas**
   ```sql
   SHOW TABLES;
   ```

3. **Continuar implementação**
   - Task 4: Implementar repository PostgreSQL
   - Task 5: Implementar repository de documentos
   - Task 6: Implementar repository de histórico

---

## 💡 Dicas

- Use `git diff` para revisar mudanças antes do commit
- Use `git log --oneline` para ver histórico de commits
- Use `git branch` para verificar branch atual
- Faça commits pequenos e frequentes
- Escreva mensagens de commit claras e descritivas

---

**Data:** 04/11/2025  
**Branch:** main/master  
**Commit:** feat: add database schema and protocols for processo management
