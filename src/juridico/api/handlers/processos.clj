(ns juridico.api.handlers.processos
  (:require [juridico.api.db.protocols :as p]
            [clojure.spec.alpha :as s]
            [ring.util.response :as response]
            [clojure.walk :as walk]))

;; ============================================
;; Helper Functions
;; ============================================

(defn remove-namespaces
  "Remove namespaces das chaves de um mapa ou coleção de mapas.
   Converte IDs grandes para strings para evitar perda de precisão no JavaScript.
   Exemplo: {:clientes/id 1 :clientes/nome 'João'} -> {:id \"1\" :nome 'João'}"
  [data]
  (walk/postwalk
    (fn [x]
      (if (map? x)
        (into {} (map (fn [[k v]]
                       (let [key-name (if (keyword? k) (keyword (name k)) k)
                             ;; Converter IDs grandes para strings
                             value (if (and (or (= key-name :id)
                                              (= key-name :cliente_id)
                                              (= key-name :processo_id)
                                              (= key-name :tenant_id)
                                              (= key-name :user_id))
                                          (number? v))
                                    (str v)
                                    v)]
                         [key-name value]))
                     x))
        x))
    data))

;; ============================================
;; Handlers de Processos
;; ============================================

(defn list-processos-handler
  "Lista processos com paginação e filtros.
   Query params: page, per-page, status, tipo, cliente-id, search"
  [{:keys [db-repo identity query-params]}]
  (let [tenant-id (:tenant-id identity)
        opts {:page (Integer/parseInt (get query-params "page" "1"))
              :per-page (Integer/parseInt (get query-params "per-page" "20"))
              :status (get query-params "status")
              :tipo (get query-params "tipo")
              :cliente-id (when-let [cid (get query-params "cliente-id")]
                           (Long/parseLong cid))
              :search (get query-params "search")}
        result (p/find-all-processos db-repo tenant-id opts)]
    {:status 200
     :body result}))

(defn get-processo-handler
  "Retorna detalhes de um processo específico."
  [{:keys [db-repo identity path-params]}]
  (let [tenant-id (:tenant-id identity)
        processo-id (Long/parseLong (:id path-params))]
    (if-let [processo (p/find-processo-by-id db-repo tenant-id processo-id)]
      {:status 200
       :body processo}
      {:status 404
       :body {:error "Processo não encontrado"}})))

(defn create-processo-handler
  "Cria novo processo."
  [{:keys [db-repo identity body-params]}]
  (println "=== [HANDLER] create-processo-handler INICIADO ===")
  (println "[HANDLER] body-params recebido:" body-params)
  
  (let [tenant-id (:tenant-id identity)
        user-id (:user-id identity)
        
        ;; Validar campos obrigatórios
        {:keys [numero_processo cliente_id tipo]} body-params]
    
    (println "[HANDLER] numero_processo:" numero_processo)
    (println "[HANDLER] cliente_id ORIGINAL:" cliente_id "tipo:" (type cliente_id))
    (println "[HANDLER] tipo:" tipo)
    
    (cond
      (nil? numero_processo)
      {:status 400 :body {:error "Número do processo é obrigatório"}}
      
      (nil? cliente_id)
      {:status 400 :body {:error "Cliente é obrigatório"}}
      
      (nil? tipo)
      {:status 400 :body {:error "Tipo é obrigatório"}}
      
      ;; Verificar se número já existe
      (p/find-processo-by-numero db-repo tenant-id numero_processo)
      {:status 409 :body {:error "Número de processo já cadastrado"}}
      
      :else
      (let [;; Converter cliente_id para Long se vier como string
            cliente-id-long (if (string? cliente_id)
                             (Long/parseLong cliente_id)
                             cliente_id)
            _ (println "[HANDLER] cliente_id CONVERTIDO:" cliente-id-long "tipo:" (type cliente-id-long))
            
            processo-data (-> body-params
                            (assoc :tenant_id tenant-id
                                   :created_by user-id
                                   :cliente_id cliente-id-long
                                   :status (or (:status body-params) "Em Andamento")))
            _ (println "[HANDLER] processo-data preparado:" processo-data)
            
            result (p/create-processo! db-repo processo-data)
            cleaned-result (remove-namespaces result)]
        (println "[HANDLER] ✅ Processo criado com sucesso!")
        {:status 201
         :body cleaned-result}))))

(defn update-processo-handler
  "Atualiza processo existente."
  [{:keys [db-repo identity path-params body-params]}]
  (let [tenant-id (:tenant-id identity)
        user-id (:user-id identity)
        processo-id (Long/parseLong (:id path-params))]
    
    ;; Verificar se processo existe
    (if-let [processo (p/find-processo-by-id db-repo tenant-id processo-id)]
      (let [result (p/update-processo! db-repo tenant-id processo-id body-params user-id)]
        {:status 200
         :body {:message "Processo atualizado com sucesso"}})
      {:status 404
       :body {:error "Processo não encontrado"}})))

(defn delete-processo-handler
  "Soft delete de processo."
  [{:keys [db-repo identity path-params]}]
  (let [tenant-id (:tenant-id identity)
        user-id (:user-id identity)
        processo-id (Long/parseLong (:id path-params))]
    
    (if (p/soft-delete-processo! db-repo tenant-id processo-id user-id)
      {:status 204}
      {:status 404
       :body {:error "Processo não encontrado"}})))

(defn search-processos-handler
  "Busca processos por termo."
  [{:keys [db-repo identity query-params]}]
  (let [tenant-id (:tenant-id identity)
        query (get query-params "q" "")
        opts {:page (Integer/parseInt (get query-params "page" "1"))
              :per-page (Integer/parseInt (get query-params "per-page" "20"))}]
    
    (if (< (count query) 2)
      {:status 400
       :body {:error "Termo de busca deve ter pelo menos 2 caracteres"}}
      (let [result (p/search-processos db-repo tenant-id query opts)]
        {:status 200
         :body result}))))

;; ============================================
;; Handlers de Documentos
;; ============================================

(defn list-documentos-handler
  "Lista documentos de um processo."
  [{:keys [db-repo identity path-params]}]
  (let [tenant-id (:tenant-id identity)
        processo-id (Long/parseLong (:processo-id path-params))]
    
    ;; Verificar se processo existe e pertence ao tenant
    (if-let [processo (p/find-processo-by-id db-repo tenant-id processo-id)]
      (let [documentos (p/find-documentos-by-processo db-repo processo-id)]
        {:status 200
         :body documentos})
      {:status 404
       :body {:error "Processo não encontrado"}})))

(defn create-documento-handler
  "Registra novo documento (metadados).
   O upload do arquivo deve ser feito separadamente."
  [{:keys [db-repo identity path-params body-params]}]
  (let [tenant-id (:tenant-id identity)
        user-id (:user-id identity)
        processo-id (Long/parseLong (:processo-id path-params))]
    
    ;; Verificar se processo existe
    (if-let [processo (p/find-processo-by-id db-repo tenant-id processo-id)]
      (let [{:keys [nome_arquivo tipo_arquivo tamanho_bytes caminho_storage]} body-params]
        
        (cond
          (nil? nome_arquivo)
          {:status 400 :body {:error "Nome do arquivo é obrigatório"}}
          
          (nil? caminho_storage)
          {:status 400 :body {:error "Caminho de storage é obrigatório"}}
          
          :else
          (let [documento-data {:processo_id processo-id
                               :nome_arquivo nome_arquivo
                               :tipo_arquivo tipo_arquivo
                               :tamanho_bytes tamanho_bytes
                               :caminho_storage caminho_storage
                               :uploaded_by user-id}
                result (p/create-documento! db-repo documento-data)]
            {:status 201
             :body result})))
      {:status 404
       :body {:error "Processo não encontrado"}})))

(defn delete-documento-handler
  "Soft delete de documento."
  [{:keys [db-repo identity path-params]}]
  (let [documento-id (Long/parseLong (:documento-id path-params))]
    
    ;; TODO: Validar que documento pertence a processo do tenant
    (if (p/soft-delete-documento! db-repo documento-id)
      {:status 204}
      {:status 404
       :body {:error "Documento não encontrado"}})))

;; ============================================
;; Handlers de Histórico
;; ============================================

(defn get-historico-handler
  "Retorna histórico de alterações de um processo."
  [{:keys [db-repo identity path-params query-params]}]
  (let [tenant-id (:tenant-id identity)
        processo-id (Long/parseLong (:processo-id path-params))
        opts {:limit (Integer/parseInt (get query-params "limit" "50"))
              :offset (Integer/parseInt (get query-params "offset" "0"))}]
    
    ;; Verificar se processo existe
    (if-let [processo (p/find-processo-by-id db-repo tenant-id processo-id)]
      (let [historico (p/find-historico-by-processo db-repo processo-id opts)
            total (p/count-historico-by-processo db-repo processo-id)]
        {:status 200
         :body {:historico historico
                :total total}})
      {:status 404
       :body {:error "Processo não encontrado"}})))

;; ============================================
;; Handlers de Clientes
;; ============================================

(defn list-clientes-handler
  "Lista clientes com paginação."
  [{:keys [db-repo identity query-params]}]
  (println "=== [HANDLER] list-clientes-handler INICIADO ===")
  (println "[HANDLER] identity:" identity)
  (println "[HANDLER] query-params:" query-params)
  
  (let [tenant-id (:tenant-id identity)
        opts {:page (Integer/parseInt (get query-params "page" "1"))
              :per-page (Integer/parseInt (get query-params "per-page" "20"))
              :search (get query-params "search")}]
    
    (println "[HANDLER] tenant-id:" tenant-id)
    (println "[HANDLER] opts:" opts)
    
    (try
      (let [result (p/find-all-clientes db-repo tenant-id opts)
            cleaned-result (update result :clientes #(map remove-namespaces %))]
        (println "[HANDLER] ✅ Clientes listados com sucesso")
        (println "[HANDLER] Total encontrado:" (:total result))
        (println "[HANDLER] Clientes após remover namespaces:" (:clientes cleaned-result))
        {:status 200
         :body cleaned-result})
      (catch Exception e
        (println "[HANDLER] ❌ EXCEÇÃO ao listar clientes:")
        (println "[HANDLER] Mensagem:" (.getMessage e))
        (.printStackTrace e)
        {:status 500
         :body {:error "Erro ao listar clientes"
                :message (.getMessage e)}}))))

(defn get-cliente-handler
  "Retorna detalhes de um cliente."
  [{:keys [db-repo identity path-params]}]
  (let [tenant-id (:tenant-id identity)
        cliente-id (Long/parseLong (:id path-params))]
    (if-let [cliente (p/find-cliente-by-id db-repo tenant-id cliente-id)]
      {:status 200
       :body (remove-namespaces cliente)}
      {:status 404
       :body {:error "Cliente não encontrado"}})))

(defn create-cliente-handler
  "Cria novo cliente."
  [{:keys [db-repo identity body-params]}]
  (println "=== [HANDLER] create-cliente-handler INICIADO ===")
  (println "[HANDLER] identity:" identity)
  (println "[HANDLER] body-params:" body-params)
  
  (let [tenant-id (:tenant-id identity)
        {:keys [nome cpf_cnpj]} body-params]
    
    (println "[HANDLER] tenant-id extraído:" tenant-id)
    (println "[HANDLER] nome:" nome)
    (println "[HANDLER] cpf_cnpj:" cpf_cnpj)
    
    (cond
      (nil? nome)
      (do
        (println "[HANDLER] ❌ ERRO: Nome é obrigatório")
        {:status 400 :body {:error "Nome é obrigatório"}})
      
      ;; Verificar se CPF/CNPJ já existe (se fornecido)
      (and cpf_cnpj (p/find-cliente-by-cpf-cnpj db-repo tenant-id cpf_cnpj))
      (do
        (println "[HANDLER] ❌ ERRO: CPF/CNPJ já cadastrado")
        {:status 409 :body {:error "CPF/CNPJ já cadastrado"}})
      
      :else
      (do
        (println "[HANDLER] ✅ Validações OK, criando cliente...")
        (let [cliente-data (assoc body-params :tenant_id tenant-id)]
          (println "[HANDLER] cliente-data preparado:" cliente-data)
          (try
            (let [result (p/create-cliente! db-repo cliente-data)
                  cleaned-result (remove-namespaces result)]
              (println "[HANDLER] ✅ Cliente criado com sucesso!")
              (println "[HANDLER] result original:" result)
              (println "[HANDLER] result limpo:" cleaned-result)
              {:status 201
               :body cleaned-result})
            (catch Exception e
              (println "[HANDLER] ❌ EXCEÇÃO ao criar cliente:")
              (println "[HANDLER] Mensagem:" (.getMessage e))
              (println "[HANDLER] Stack trace:")
              (.printStackTrace e)
              {:status 500
               :body {:error "Erro ao criar cliente"
                      :message (.getMessage e)}})))))))

(defn update-cliente-handler
  "Atualiza cliente existente."
  [{:keys [db-repo identity path-params body-params]}]
  (let [tenant-id (:tenant-id identity)
        cliente-id (Long/parseLong (:id path-params))]
    
    (if-let [cliente (p/find-cliente-by-id db-repo tenant-id cliente-id)]
      (let [result (p/update-cliente! db-repo tenant-id cliente-id body-params)]
        {:status 200
         :body {:message "Cliente atualizado com sucesso"}})
      {:status 404
       :body {:error "Cliente não encontrado"}})))

(defn delete-cliente-handler
  "Soft delete de cliente."
  [{:keys [db-repo identity path-params]}]
  (let [tenant-id (:tenant-id identity)
        cliente-id (Long/parseLong (:id path-params))]
    
    ;; Verificar se cliente tem processos
    (let [processos-count (p/count-processos-by-cliente db-repo cliente-id)]
      (if (pos? processos-count)
        {:status 409
         :body {:error (str "Cliente possui " processos-count " processo(s) vinculado(s)")}}
        
        (if (p/soft-delete-cliente! db-repo tenant-id cliente-id)
          {:status 204}
          {:status 404
           :body {:error "Cliente não encontrado"}})))))

(defn search-clientes-handler
  "Busca clientes por termo."
  [{:keys [db-repo identity query-params]}]
  (let [tenant-id (:tenant-id identity)
        query (get query-params "q" "")
        opts {:page (Integer/parseInt (get query-params "page" "1"))
              :per-page (Integer/parseInt (get query-params "per-page" "20"))}]
    
    (if (< (count query) 2)
      {:status 400
       :body {:error "Termo de busca deve ter pelo menos 2 caracteres"}}
      (let [result (p/search-clientes db-repo tenant-id query opts)]
        {:status 200
         :body result}))))
