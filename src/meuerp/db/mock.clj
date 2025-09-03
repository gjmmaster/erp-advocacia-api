(ns meuerp.db.mock
  (:require [meuerp.db.protocols :refer [ProcessosRepository]]))

;; --- Banco de Dados Mock ---
;; Um atom global para simular um banco de dados em memória.
;; A estrutura é um mapa onde a chave é o tenant-id.
;; Cada tenant tem um mapa com suas próprias "tabelas", como :processos.
(def db-atom
  (atom
   {"tenant-1"
    {:processos
     {"proc-111" {:id "proc-111"
                  :case_number "0001-2023"
                  :jurisdiction "Tribunal de Justiça de São Paulo"}
      "proc-222" {:id "proc-222"
                  :case_number "0002-2023"
                  :jurisdiction "Tribunal Regional Federal"}}}
    "tenant-2"
    {:processos
     {"proc-333" {:id "proc-333"
                  :case_number "ABC-2024"
                  :jurisdiction "Superior Tribunal de Justiça"}}}}))


;; --- Implementação do Repositório Mock ---
;; Usamos defrecord para criar um "tipo" que carrega o estado necessário:
;; - db: A referência ao nosso banco de dados (o atom).
;; - tenant-id: O identificador do tenant para a requisição atual.
(defrecord MockProcessosRepository [db tenant-id]
  ;; Implementação do protocolo para o nosso record.
  ;; Cada função usa o `tenant-id` que está dentro do próprio record (`this`).
  ProcessosRepository

  (listar-processos [this]
    ;; Acessa o atom, vai direto para os dados do tenant e retorna a lista de processos.
    (-> @(:db this)
        (get (:tenant-id this))
        :processos
        vals
        (or [])))

  (obter-processo-por-id [this id]
    ;; Busca o processo dentro do escopo do tenant.
    (-> @(:db this)
        (get (:tenant-id this))
        (get-in [:processos id])))

  (criar-processo [this processo]
    ;; Cria um novo processo, garantindo que ele seja atribuído ao tenant correto.
    (let [new-id (str "proc-" (rand-int 10000))
          novo-processo-com-id (assoc processo :id new-id)]
      ;; swap! é usado para atualizar o estado do atom de forma atômica e segura.
      (swap! (:db this) assoc-in [(:tenant-id this) :processos new-id] novo-processo-com-id)
      ;; Retorna o processo criado com o novo ID.
      novo-processo-com-id)))

;; Função "construtora" para facilitar a criação de instâncias do nosso repositório.
(defn create-repository [tenant-id]
  (->MockProcessosRepository db-atom tenant-id))
