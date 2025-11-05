# 3 Passos Simples para Deploy

**Tempo total:** 30 minutos

---

## 1️⃣ Commit e Push (5 min)

```bash
git add .
git commit -m "feat: complete backend for processo management (tasks 4-11)"
git push
```

✅ Pronto! O Render vai fazer deploy automático.

---

## 2️⃣ Aplicar Migrations (10 min)

### Via Console Web (MAIS FÁCIL):

1. Acesse: https://cockroachlabs.cloud/
2. Faça login
3. Clique em "SQL Shell"
4. Copie e cole o conteúdo de `migrations/004_create_clientes_table.sql`
5. Execute
6. Copie e cole o conteúdo de `migrations/005_create_processos_tables.sql`
7. Execute

✅ Pronto! Tabelas criadas.

---

## 3️⃣ Testar (5 min)

```bash
# Fazer login para obter token
curl -X POST https://seu-backend.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "seu-email", "password": "sua-senha"}'

# Copie o token da resposta

# Testar endpoint de clientes
curl https://seu-backend.onrender.com/api/tenant/clientes \
  -H "Authorization: Bearer SEU_TOKEN"

# Deve retornar: {"clientes":[],"total":0,"page":1,"per-page":20}
```

✅ Pronto! Backend funcionando!

---

## 🎉 Parabéns!

Seu backend está no ar e funcionando!

**Próximo:** Criar o frontend para consumir estes endpoints.

---

**Dúvidas?** Veja os guias completos:
- `PRONTO_PARA_DEPLOY.md`
- `COMANDOS_DEPLOY.md`
- `GUIA_APLICAR_MIGRATIONS.md`

