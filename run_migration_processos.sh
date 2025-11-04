#!/bin/bash

# Script para executar migration das tabelas de processos
# Usage: ./run_migration_processos.sh

set -e

echo "🚀 Executando migration: Create processos tables"
echo "=================================================="
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

# Verificar se tabela clientes existe
echo "🔍 Verificando pré-requisitos..."
CLIENTES_EXISTS=$(psql "$DATABASE_URL" -tAc "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'clientes');")

if [ "$CLIENTES_EXISTS" != "t" ]; then
    echo "❌ Erro: Tabela 'clientes' não existe"
    echo "Execute primeiro: ./run_migration_clientes.sh"
    exit 1
fi

echo "✅ Tabela 'clientes' encontrada"
echo ""

# Executar migration
echo "⏳ Executando migration..."
psql "$DATABASE_URL" -f migrations/005_create_processos_tables.sql

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Migration executada com sucesso!"
    echo ""
    echo "📋 Tabelas criadas:"
    echo "  - processos (processos jurídicos)"
    echo "  - processo_documentos (documentos anexados)"
    echo "  - processo_historico (auditoria de alterações)"
    echo ""
    echo "📊 Índices criados:"
    echo "  Processos:"
    echo "    - idx_processos_tenant (tenant_id)"
    echo "    - idx_processos_cliente (cliente_id)"
    echo "    - idx_processos_status (status)"
    echo "    - idx_processos_numero (numero_processo)"
    echo "    - idx_processos_deleted (deleted_at) PARTIAL"
    echo "    - idx_processos_created_at (created_at DESC)"
    echo ""
    echo "  Documentos:"
    echo "    - idx_processo_docs_processo (processo_id)"
    echo "    - idx_processo_docs_deleted (deleted_at) PARTIAL"
    echo ""
    echo "  Histórico:"
    echo "    - idx_processo_hist_processo (processo_id)"
    echo "    - idx_processo_hist_created (created_at DESC)"
    echo ""
    echo "🔒 Constraints:"
    echo "  - unique_processo_tenant (tenant_id, numero_processo) UNIQUE"
    echo "  - Foreign keys com CASCADE para tenants e clientes"
    echo ""
    echo "🔄 Para reverter esta migration, execute:"
    echo "  psql \$DATABASE_URL -f migrations/005_rollback_processos_tables.sql"
else
    echo ""
    echo "❌ Erro ao executar migration"
    exit 1
fi
