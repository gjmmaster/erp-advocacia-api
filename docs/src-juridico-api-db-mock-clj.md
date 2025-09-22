# Documentação: `src/juridico/api/db/mock.clj`

## Visão Geral

Este arquivo contém a implementação **concreta** e **em memória** dos protocolos de persistência (`ProcessosRepository` e `AuthRepository`) definidos em `protocols.clj`. Ele funciona como uma camada de banco de dados "fake" ou "mock", permitindo que a aplicação seja desenvolvida e testada sem a necessidade de um banco de dados real como o PostgreSQL.

Toda a lógica de manipulação de dados para a Prova de Conceito (PoC) reside aqui.

---

### **Aviso de Manutenção**

**CRÍTICO:** A implementação mock deve ser mantida em **estrita sincronia** com as interfaces definidas em `protocols.clj`. Qualquer função adicionada ou modificada nos protocolos **deve** ser imediatamente refletida neste arquivo.

A falha em manter a paridade entre a implementação mock e a implementação real (`postgres.clj`) pode levar a testes que passam com sucesso em ambiente de desenvolvimento, mas quebram em produção com erros como `AbstractMethodError`.

---

## Detalhamento do Código

### O Banco de Dados em Memória (`db-atom`)

A variável `db-atom` é o coração deste mock. É um `atom` do Clojure, que é um tipo de referência que garante atualizações atômicas e seguras em um ambiente com múltiplas threads. O `atom` envolve um grande mapa aninhado que serve como nosso banco de dados.

A estrutura do mapa é a seguinte:

```clojure
{
  "tenant-id-1" {:dados {...} :users {...} :processos {...}},
  "tenant-id-2" {:dados {...} :users {...} :processos {...}}
}
```

- As chaves do mapa principal são os IDs dos *tenants*.
- Cada *tenant* tem seu próprio mapa contendo seus dados (`:dados`), seus usuários (`:users`) e seus processos (`:processos`).
- Esta estrutura simula o isolamento de dados da arquitetura multi-tenant.

### O Repositório (`defrecord MockRepository`)

O `defrecord` define um tipo de "objeto" ou "classe" chamado `MockRepository`. Uma instância deste record representa uma conexão com o banco de dados. Ele possui dois campos:

- `db`: Uma referência ao `db-atom` global.
- `tenant-id`: O ID do *tenant* para o qual este repositório está escopado. **Este campo pode ser `nil`** para repositórios "públicos".

É dentro da definição deste `defrecord` que os protocolos são efetivamente implementados.

### Implementação do `ProcessosRepository`

Esta implementação demonstra o isolamento de *tenant*. Todas as funções aqui utilizam o campo `tenant-id` da instância do `MockRepository` para operar apenas na fatia correta do `db-atom`.

- `(listar-processos [this])`: Acessa `@(:db this)` para obter o mapa do banco de dados, usa `(get (:tenant-id this))` para pegar apenas os dados do *tenant* atual e retorna a lista de seus processos.
- `(obter-processo-por-id [this id])`: Usa `get-in` para descer diretamente na estrutura de dados do *tenant* atual (`[tenant-id :processos id]`), garantindo que não possa acessar um processo de outro *tenant*.
- `(criar-processo [this processo])`: Usa `swap!` e `assoc-in` para adicionar um novo processo diretamente no mapa de processos do *tenant* atual.

### Implementação do `AuthRepository`

Esta implementação lida com operações globais.

- `(encontrar-tenant-por-subdominio [this subdominio])`: **Ignora** o campo `tenant-id`. Em vez disso, ele varre os valores de todos os *tenants* no `db-atom` para encontrar aquele cujo subdomínio corresponda.
- `(encontrar-usuario-por-email [this tenant-id email])`: Recebe o `tenant-id` como argumento e o usa para pesquisar na lista de usuários do *tenant* correto.
- `(criar-tenant-e-usuario-master [this ...])`: Realiza a operação mais complexa, usando `swap!` e `assoc` para adicionar uma nova chave de primeiro nível (o novo `tenant-id`) ao `db-atom`.

### A Função Construtora (`create-repository`)

Esta é uma função auxiliar que facilita a criação de instâncias do `MockRepository`. É esta função que os `middlewares` chamam. Ela possui duas "aridades" (versões com diferentes números de argumentos):

- `(create-repository)`: Chamada sem argumentos. Retorna um `MockRepository` onde o campo `tenant-id` é `nil`. Este é o repositório **público/global**.
- `(create-repository tenant-id)`: Chamada com um argumento. Retorna um `MockRepository` onde o campo `tenant-id` está definido. Este é o repositório **isolado/escopado**.
