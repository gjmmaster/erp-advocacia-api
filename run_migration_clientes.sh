#!/bin/bash

# Script para executar migration da tabela clientes
# Usage: ./run_migration_clientes.sh

set -e

echo "🚀 Executando migration: Create clientes table"
echo "================================================"
echo ""

# Verificar se DATABASE_URL está definida
if [ -z "$DATABASE_URL" ]; then
    echo "❌ Erro: DATABASE_URL não está definida"
    echo "Configure a variável de ambiente DATABASE_URL antes de executar este script"
    echo ""
    echo "Exemplo:"
    echo "export DATABASE_URL='postgresql://user:password@host:port/database'"
    exit 1
fi

echo "📊 Database: $DATABASE_URL"
echo ""

# Executar migration
echo "⏳ Executando migration..."
psql "$DATABASE_URL" -f migrations/004_create_clientes_table.sql

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Migration executada com sucesso!"
    echo ""
    echo "📋 Tabela criada:"
    echo "  - clientes (com soft delete)"
    echo ""
    echo "📊 Índices criados:"
    echo "  - idx_clientes_tenant (tenant_id)"
    echo "  - idx_clientes_cpf_tenant (tenant_id, cpf_cnpj) UNIQUE"
    echo "  - idx_clientes_deleted (deleted_at)"
    echo ""
    echo "🔄 Para reverter esta migration, execute:"
    echo "  psql \$DATABASE_URL -f migrations/004_rollback_clientes_table.sql"
else
    echo ""
    echo "❌ Erro ao executar migration"
    exit 1
fi
