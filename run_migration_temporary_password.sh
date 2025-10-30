#!/bin/bash

# Script to run the temporary_password migration
# Usage: ./run_migration_temporary_password.sh [environment]
# environment: local, staging, production (default: local)

set -e  # Exit on error

ENVIRONMENT=${1:-local}

echo "========================================="
echo "Running temporary_password migration"
echo "Environment: $ENVIRONMENT"
echo "========================================="
echo ""

# Check if DATABASE_URL is set
if [ -z "$DATABASE_URL" ]; then
    echo "❌ ERROR: DATABASE_URL environment variable is not set"
    echo ""
    echo "Please set it with:"
    echo "  export DATABASE_URL='postgresql://user:pass@host:port/dbname'"
    echo ""
    exit 1
fi

# Show database connection (hide password)
DB_INFO=$(echo $DATABASE_URL | sed 's/:\/\/[^:]*:[^@]*@/:\/\/***:***@/')
echo "📊 Database: $DB_INFO"
echo ""

# Confirm before running in production
if [ "$ENVIRONMENT" = "production" ]; then
    echo "⚠️  WARNING: You are about to run migration in PRODUCTION!"
    echo ""
    read -p "Are you sure you want to continue? (yes/no): " confirm
    if [ "$confirm" != "yes" ]; then
        echo "❌ Migration cancelled"
        exit 0
    fi
    echo ""
fi

# Backup reminder
echo "📋 REMINDER: Make sure you have a recent database backup!"
if [ "$ENVIRONMENT" = "production" ] || [ "$ENVIRONMENT" = "staging" ]; then
    read -p "Do you have a recent backup? (yes/no): " backup_confirm
    if [ "$backup_confirm" != "yes" ]; then
        echo "❌ Please create a backup first"
        exit 0
    fi
    echo ""
fi

# Run migration
echo "🚀 Running migration..."
echo ""

psql "$DATABASE_URL" -f add_temporary_password_column.sql

# Check result
if [ $? -eq 0 ]; then
    echo ""
    echo "========================================="
    echo "✅ Migration completed successfully!"
    echo "========================================="
    echo ""
    echo "Next steps:"
    echo "1. Verify the column was created:"
    echo "   psql \$DATABASE_URL -c \"\\d users\""
    echo ""
    echo "2. Check the index:"
    echo "   psql \$DATABASE_URL -c \"SELECT indexname, indexdef FROM pg_indexes WHERE tablename = 'users' AND indexname = 'idx_users_temporary_password';\""
    echo ""
    echo "3. Continue with Task 2: Update Provision Tenant Handler"
    echo ""
else
    echo ""
    echo "========================================="
    echo "❌ Migration failed!"
    echo "========================================="
    echo ""
    echo "Please check the error messages above"
    exit 1
fi
