# Documentação: `src/juridico/api/db/protocols.clj`

## Visão Geral

Este arquivo é um dos pilares da arquitetura da aplicação. Ele utiliza o recurso `defprotocol` do Clojure para definir **contratos** para a camada de persistência de dados. Um protocolo é como uma interface em outras linguagens: ele define um conjunto de funções que devem existir, mas não as implementa.

O objetivo desta abordagem é criar uma **abstração poderosa** que desacopla completamente a lógica de negócio (nos `handlers`) da implementação concreta do banco de dados. Os `handlers` não sabem se os dados estão em um banco de dados em memória, em um PostgreSQL ou em um arquivo de texto. Eles apenas sabem que o objeto de repositório (`db-repo`) que recebem irá satisfazer as funções definidas nestes protocolos.

Este arquivo define dois contratos distintos: um para operações de negócio dentro de um *tenant* e outro para operações globais de autenticação e provisionamento.

---

## Detalhamento dos Protocolos

### `(defprotocol ProcessosRepository ...)`

Este protocolo define o contrato para todas as operações de dados que são **sensíveis ao contexto do tenant**. A ideia principal é que qualquer implementação deste protocolo já deve ter sido configurada com um `tenant-id` específico (isso é feito no `middleware`). Portanto, as funções aqui não precisam receber um `tenant-id` como argumento; o isolamento é uma responsabilidade implícita da implementação.

- **`listar-processos [this]`**
    - **Contrato:** Deve retornar uma lista de todos os processos jurídicos pertencentes ao *tenant* atual.
- **`obter-processo-por-id [this id]`**
    - **Contrato:** Deve buscar e retornar um único processo pelo seu `id`, mas apenas se ele pertencer ao *tenant* atual. Se o processo com aquele `id` existir mas pertencer a outro *tenant*, deve retornar `nil` (como se não existisse).
- **`criar-processo [this processo]`**
    - **Contrato:** Deve criar um novo processo no banco de dados, associando-o automaticamente ao *tenant* atual. O mapa `processo` contém os dados a serem inseridos.

### `(defprotocol AuthRepository ...)`

Este protocolo define o contrato para operações que são **globais** ou que atravessam os limites dos *tenants*. É usado principalmente para os fluxos de login e de criação de novas contas. Uma implementação deste protocolo geralmente não terá o escopo de um único *tenant*.

- **`encontrar-tenant-por-subdominio [this subdominio]`**
    - **Contrato:** Deve buscar e retornar os dados de um *tenant* a partir do seu subdomínio único. Essencial para o processo de login.
- **`encontrar-usuario-por-email [this tenant-id email]`**
    - **Contrato:** Deve buscar um usuário pelo seu e-mail, mas **explicitamente dentro do escopo de um `tenant-id` fornecido**. Este design é intencional: o `login-handler` primeiro encontra o *tenant* pelo subdomínio e depois usa o ID do *tenant* para encontrar o usuário, garantindo que o e-mail `admin@empresa.com` do *Tenant A* não seja confundido com o do *Tenant B*.
- **`criar-tenant-e-usuario-master [this dados-provisionamento]`**
    - **Contrato:** Uma função de alto nível para orquestrar a criação de um novo *tenant* e seu primeiro usuário administrador (master). O mapa `dados-provisionamento` contém as informações necessárias, como `:company_name` e `:email`.
