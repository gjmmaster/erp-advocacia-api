(ns juridico.api.db.seed
  (:require [buddy.hashers :as hashers]
            [clojure.string :as str])
  (:import [java.time Instant]
           [java.time.temporal ChronoUnit]))

(defn- days-ago [n]
  "Retorna um Instant de N dias atrás."
  (.minus (Instant/now) n ChronoUnit/DAYS))

(defn- hours-ago [n]
  "Retorna um Instant de N horas atrás."
  (.minus (Instant/now) n ChronoUnit/HOURS))

(defn generate-seed-data []
  "Gera estrutura completa de dados de seed para desenvolvimento.
   Inclui tenant demo, usuários, clientes, processos, documentos e histórico."
  
  (let [now (Instant/now)
        tenant-id 1
        super-admin-id 1
        master-id 2
        operador1-id 3
        operador2-id 4
        cliente1-id 1
        cliente2-id 2
        cliente3-id 3]
    
    {:tenants
     {tenant-id
      {:id tenant-id
       :company_name "Demo Company"
       :subdomain "demo"
       :operator_limit 10
       :is_active true
       :created_at (days-ago 180)
       :updated_at (days-ago 180)}}
     
     :users
     {super-admin-id
      {:id super-admin-id
       :tenant_id nil  ; Super admin não tem tenant
       :email "admin@demo.com"
       :password_hash (hashers/encrypt "admin123")
       :full_name "Super Administrador"
       :role "super-admin"
       :temporary_password false
       :created_at (days-ago 180)
       :updated_at (days-ago 180)}
      
      master-id
      {:id master-id
       :tenant_id tenant-id
       :email "master@demo.com"
       :password_hash (hashers/encrypt "master123")
       :full_name "Master User"
       :role "master"
       :temporary_password false
       :created_at (days-ago 180)
       :updated_at (days-ago 180)}
      
      operador1-id
      {:id operador1-id
       :tenant_id tenant-id
       :email "operador1@demo.com"
       :password_hash (hashers/encrypt "operador123")
       :full_name "Operador Um"
       :role "operador"
       :temporary_password false
       :created_at (days-ago 90)
       :updated_at (days-ago 90)}
      
      operador2-id
      {:id operador2-id
       :tenant_id tenant-id
       :email "operador2@demo.com"
       :password_hash (hashers/encrypt "operador123")
       :full_name "Operador Dois"
       :role "operador"
       :temporary_password false
       :created_at (days-ago 60)
       :updated_at (days-ago 60)}}
     
     :clientes
     {cliente1-id
      {:id cliente1-id
       :tenant_id tenant-id
       :nome "João Silva"
       :cpf_cnpj "123.456.789-00"
       :email "joao.silva@email.com"
       :telefone "(11) 98765-4321"
       :endereco "Rua das Flores, 123 - São Paulo, SP"
       :created_at (days-ago 120)
       :updated_at (days-ago 120)
       :deleted_at nil}
      
      cliente2-id
      {:id cliente2-id
       :tenant_id tenant-id
       :nome "Maria Santos"
       :cpf_cnpj "987.654.321-00"
       :email "maria.santos@email.com"
       :telefone "(11) 91234-5678"
       :endereco "Av. Paulista, 1000 - São Paulo, SP"
       :created_at (days-ago 90)
       :updated_at (days-ago 90)
       :deleted_at nil}
      
      cliente3-id
      {:id cliente3-id
       :tenant_id tenant-id
       :nome "Empresa XYZ Ltda"
       :cpf_cnpj "12.345.678/0001-90"
       :email "contato@empresaxyz.com.br"
       :telefone "(11) 3456-7890"
       :endereco "Rua Comercial, 500 - São Paulo, SP"
       :created_at (days-ago 150)
       :updated_at (days-ago 150)
       :deleted_at nil}}
     
     :processos
     {1 {:id 1
         :tenant_id tenant-id
         :cliente_id cliente1-id
         :numero_processo "0001234-56.2024.8.26.0100"
         :tipo "civel"
         :status "ativo"
         :vara "1ª Vara Cível"
         :descricao "Ação de cobrança de valores não pagos"
         :valor_causa 50000.00
         :data_distribuicao (days-ago 100)
         :created_by master-id
         :updated_by master-id
         :created_at (days-ago 100)
         :updated_at (days-ago 50)
         :deleted_at nil
         :deleted_by nil}
      
      2 {:id 2
         :tenant_id tenant-id
         :cliente_id cliente1-id
         :numero_processo "0007890-12.2024.5.02.0001"
         :tipo "trabalhista"
         :status "em_andamento"
         :vara "2ª Vara do Trabalho"
         :descricao "Reclamação trabalhista - horas extras"
         :valor_causa 25000.00
         :data_distribuicao (days-ago 80)
         :created_by operador1-id
         :updated_by operador1-id
         :created_at (days-ago 80)
         :updated_at (days-ago 30)
         :deleted_at nil
         :deleted_by nil}
      
      3 {:id 3
         :tenant_id tenant-id
         :cliente_id cliente2-id
         :numero_processo "0002345-67.2024.8.26.0200"
         :tipo "familia"
         :status "ativo"
         :vara "Vara de Família e Sucessões"
         :descricao "Ação de divórcio consensual"
         :valor_causa 0.0
         :data_distribuicao (days-ago 60)
         :created_by master-id
         :updated_by master-id
         :created_at (days-ago 60)
         :updated_at (days-ago 20)
         :deleted_at nil
         :deleted_by nil}
      
      4 {:id 4
         :tenant_id tenant-id
         :cliente_id cliente3-id
         :numero_processo "0003456-78.2024.8.26.0300"
         :tipo "civel"
         :status "arquivado"
         :vara "3ª Vara Cível"
         :descricao "Ação de rescisão contratual - FINALIZADO"
         :valor_causa 100000.00
         :data_distribuicao (days-ago 150)
         :created_by operador2-id
         :updated_by operador2-id
         :created_at (days-ago 150)
         :updated_at (days-ago 10)
         :deleted_at nil
         :deleted_by nil}
      
      5 {:id 5
         :tenant_id tenant-id
         :cliente_id cliente3-id
         :numero_processo "0004567-89.2024.8.26.0400"
         :tipo "tributario"
         :status "em_andamento"
         :vara "Vara da Fazenda Pública"
         :descricao "Mandado de segurança - ICMS"
         :valor_causa 500000.00
         :data_distribuicao (days-ago 45)
         :created_by master-id
         :updated_by operador1-id
         :created_at (days-ago 45)
         :updated_at (days-ago 5)
         :deleted_at nil
         :deleted_by nil}}
     
     :processo_documentos
     {1 {:id 1
         :processo_id 1
         :nome_arquivo "peticao_inicial.pdf"
         :tipo_arquivo "application/pdf"
         :tamanho_bytes 245678
         :caminho_storage "mock://documentos/processo-1/peticao_inicial.pdf"
         :uploaded_by master-id
         :created_at (days-ago 100)
         :deleted_at nil}
      
      2 {:id 2
         :processo_id 1
         :nome_arquivo "contrato.pdf"
         :tipo_arquivo "application/pdf"
         :tamanho_bytes 189234
         :caminho_storage "mock://documentos/processo-1/contrato.pdf"
         :uploaded_by master-id
         :created_at (days-ago 99)
         :deleted_at nil}
      
      3 {:id 3
         :processo_id 2
         :nome_arquivo "reclamacao_trabalhista.pdf"
         :tipo_arquivo "application/pdf"
         :tamanho_bytes 312456
         :caminho_storage "mock://documentos/processo-2/reclamacao_trabalhista.pdf"
         :uploaded_by operador1-id
         :created_at (days-ago 80)
         :deleted_at nil}
      
      4 {:id 4
         :processo_id 2
         :nome_arquivo "holerites.pdf"
         :tipo_arquivo "application/pdf"
         :tamanho_bytes 456789
         :caminho_storage "mock://documentos/processo-2/holerites.pdf"
         :uploaded_by operador1-id
         :created_at (days-ago 79)
         :deleted_at nil}
      
      5 {:id 5
         :processo_id 3
         :nome_arquivo "certidao_casamento.pdf"
         :tipo_arquivo "application/pdf"
         :tamanho_bytes 123456
         :caminho_storage "mock://documentos/processo-3/certidao_casamento.pdf"
         :uploaded_by master-id
         :created_at (days-ago 60)
         :deleted_at nil}
      
      6 {:id 6
         :processo_id 4
         :nome_arquivo "contrato_rescindido.pdf"
         :tipo_arquivo "application/pdf"
         :tamanho_bytes 567890
         :caminho_storage "mock://documentos/processo-4/contrato_rescindido.pdf"
         :uploaded_by operador2-id
         :created_at (days-ago 150)
         :deleted_at nil}
      
      7 {:id 7
         :processo_id 4
         :nome_arquivo "sentenca.pdf"
         :tipo_arquivo "application/pdf"
         :tamanho_bytes 234567
         :caminho_storage "mock://documentos/processo-4/sentenca.pdf"
         :uploaded_by operador2-id
         :created_at (days-ago 11)
         :deleted_at nil}
      
      8 {:id 8
         :processo_id 5
         :nome_arquivo "mandado_seguranca.pdf"
         :tipo_arquivo "application/pdf"
         :tamanho_bytes 345678
         :caminho_storage "mock://documentos/processo-5/mandado_seguranca.pdf"
         :uploaded_by master-id
         :created_at (days-ago 45)
         :deleted_at nil}}
     
     :processo_historico
     {1 {:id 1
         :processo_id 1
         :user_id master-id
         :acao "criacao"
         :campo_alterado nil
         :valor_anterior nil
         :valor_novo "Processo criado"
         :created_at (days-ago 100)}
      
      2 {:id 2
         :processo_id 1
         :user_id master-id
         :acao "edicao"
         :campo_alterado "status"
         :valor_anterior "em_andamento"
         :valor_novo "ativo"
         :created_at (days-ago 50)}
      
      3 {:id 3
         :processo_id 2
         :user_id operador1-id
         :acao "criacao"
         :campo_alterado nil
         :valor_anterior nil
         :valor_novo "Processo criado"
         :created_at (days-ago 80)}
      
      4 {:id 4
         :processo_id 2
         :user_id operador1-id
         :acao "edicao"
         :campo_alterado "descricao"
         :valor_anterior "Reclamação trabalhista"
         :valor_novo "Reclamação trabalhista - horas extras"
         :created_at (days-ago 30)}
      
      5 {:id 5
         :processo_id 3
         :user_id master-id
         :acao "criacao"
         :campo_alterado nil
         :valor_anterior nil
         :valor_novo "Processo criado"
         :created_at (days-ago 60)}
      
      6 {:id 6
         :processo_id 3
         :user_id master-id
         :acao "edicao"
         :campo_alterado "vara"
         :valor_anterior "Vara de Família"
         :valor_novo "Vara de Família e Sucessões"
         :created_at (days-ago 20)}
      
      7 {:id 7
         :processo_id 4
         :user_id operador2-id
         :acao "criacao"
         :campo_alterado nil
         :valor_anterior nil
         :valor_novo "Processo criado"
         :created_at (days-ago 150)}
      
      8 {:id 8
         :processo_id 4
         :user_id operador2-id
         :acao "edicao"
         :campo_alterado "status"
         :valor_anterior "ativo"
         :valor_novo "arquivado"
         :created_at (days-ago 10)}
      
      9 {:id 9
         :processo_id 5
         :user_id master-id
         :acao "criacao"
         :campo_alterado nil
         :valor_anterior nil
         :valor_novo "Processo criado"
         :created_at (days-ago 45)}
      
      10 {:id 10
          :processo_id 5
          :user_id operador1-id
          :acao "edicao"
          :campo_alterado "valor_causa"
          :valor_anterior "450000.00"
          :valor_novo "500000.00"
          :created_at (days-ago 5)}}
     
     ;; Contadores para auto-increment de IDs
     :counters
     {:tenant-id 1
      :user-id 4
      :cliente-id 3
      :processo-id 5
      :documento-id 8
      :historico-id 10}}))
