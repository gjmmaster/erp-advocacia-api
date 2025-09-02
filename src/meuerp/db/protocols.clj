(ns meuerp.db.protocols)

(defprotocol ProcessosRepository
  "Define o contrato para o repositório de processos jurídicos.
  A implementação deste protocolo deve gerenciar internamente o tenant-id,
  garantindo que todas as operações sejam isoladas por tenant."

  (listar-processos [this]
    "Retorna uma lista de todos os processos para o tenant atual.")

  (obter-processo-por-id [this id]
    "Busca um processo específico pelo seu ID para o tenant atual.
     Retorna o processo se encontrado, ou nil caso contrário.")

  (criar-processo [this processo]
    "Cria um novo processo para o tenant atual.
     `processo` é um mapa com os dados do novo processo.
     Retorna o processo recém-criado, possivelmente com o ID adicionado."))
