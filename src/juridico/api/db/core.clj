(ns juridico.api.db.core
  "Factory module para criação de repositórios.
   Decide entre MockRepository (dev) e PostgresRepository (produção)
   baseado na variável de ambiente DEV_MODE."
  (:require [juridico.api.db.postgres :as pg]
            [juridico.api.db.mock :as mock]
            [environ.core :refer [env]]))

(defn create-repository
  "Factory function que retorna a implementação apropriada do repositório.
   
   Se DEV_MODE=true, retorna MockRepository (in-memory com atom).
   Caso contrário, retorna PostgresRepository (banco de dados real).
   
   Parâmetros:
   - tenant-id (opcional): ID do tenant para PostgresRepository"
  ([]
   (create-repository nil))
  ([tenant-id]
   (if (= "true" (env :dev-mode))
     ;; Modo de desenvolvimento - usa repositório mock
     (do
       (println "")
       (println "========================================")
       (println "🔧 MODO DE DESENVOLVIMENTO ATIVADO")
       (println "========================================")
       (println "📦 Usando Repositório MOCK (in-memory)")
       (println "⚡ Dados não persistem entre reinícios")
       (println "🚀 Ideal para desenvolvimento rápido")
       (println "========================================")
       (mock/create-mock-repository))
     
     ;; Modo de produção - usa repositório PostgreSQL
     (do
       (println "")
       (println "========================================")
       (println "🚀 MODO DE PRODUÇÃO")
       (println "========================================")
       (println "🗄️  Usando Repositório PostgreSQL")
       (println "💾 Dados persistem no banco de dados")
       (println "🔒 Ambiente de produção")
       (println "========================================")
       (println "")
       (pg/->PostgresRepository @pg/datasource tenant-id)))))

(def repository
  "Instância global do repositório (lazy-loaded).
   Será criada apenas quando acessada pela primeira vez."
  (delay (create-repository)))

(defn get-repository
  "Retorna a instância do repositório.
   Se tenant-id for fornecido, cria uma nova instância com esse tenant.
   Caso contrário, retorna a instância global."
  ([]
   @repository)
  ([tenant-id]
   (create-repository tenant-id)))
