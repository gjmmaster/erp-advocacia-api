#!/bin/bash

# Script para aplicar migration de renomeação da coluna vara_tribunal
# Data: 2025-11-06

echo "=========================================="
echo "Aplicando Migration: Rename vara_tribunal"
echo "=========================================="

# Configuração do banco
DB_HOST="your-cockroachdb-host"
DB_PORT="26257"
DB_NAME="defaultdb"
DB_USER="your-user"

# Arquivo da migration
MIGRATION_FILE="migrations/006_rename_vara_tribunal_to_vara.sql"

echo ""
echo "Conectando ao CockroachDB..."
echo "Host: $DB_HOST"
echo "Database: $DB_NAME"
echo ""

# Executa a migration
cockroach sql \
  --host=$DB_HOST \
  --port=$DB_PORT \
  --database=$DB_NAME \
  --user=$DB_USER \
  --file=$MIGRATION_FILE

if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "✅ Migration aplicada com sucesso!"
    echo "=========================================="
    echo ""
    echo "Coluna renomeada:"
    echo "  - vara_tribunal → vara"
    echo ""
else
    echo ""
    echo "=========================================="
    echo "❌ Erro ao aplicar migration"
    echo "=========================================="
    exit 1
fi
