# Documentação para `src/juridico/api/config.clj`

## Visão Geral

Este arquivo é responsável por centralizar as configurações críticas da aplicação, garantindo que valores sensíveis ou que variam entre ambientes (desenvolvimento, produção) sejam geridos a partir de uma única fonte de verdade.

## Conteúdo

### `(def jwt-secret ...)`

Esta variável define a chave secreta usada para assinar e verificar os tokens JWT (JSON Web Tokens) em toda a aplicação.

O valor é obtido da seguinte forma, em ordem de prioridade:
1.  **Variável de Ambiente `JWT_SECRET`**: A aplicação primeiro tenta ler a chave da variável de ambiente `JWT_SECRET`. Este é o método preferido para ambientes de produção, permitindo que a chave seja injetada de forma segura sem ser escrita diretamente no código.
2.  **Chave Padrão**: Se a variável de ambiente não estiver definida, a aplicação utiliza uma chave padrão (`"chave-padrao-para-desenvolvimento-segura"`).
    - **AVISO**: Esta chave padrão é **insegura** e destina-se **apenas para uso em ambiente de desenvolvimento local**. Nunca deve ser usada em produção.

A centralização desta chave neste arquivo resolveu um problema crítico onde diferentes partes do código (criação de token e verificação de token) poderiam usar chaves diferentes, resultando em erros de "Token inválido". Agora, toda a aplicação compartilha esta única definição.
