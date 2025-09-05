# Documentação: `.gitignore`

## Visão Geral

O arquivo `.gitignore` é um arquivo de configuração para o sistema de controle de versão Git. Sua função é instruir o Git a ignorar certos arquivos e diretórios, impedindo que eles sejam acidentalmente adicionados ao histórico de commits do projeto (o repositório).

Isso é fundamental para manter o repositório limpo, focando apenas no código-fonte e nos arquivos essenciais, e excluindo:

-   **Artefatos de Build:** Arquivos gerados automaticamente pelo processo de compilação (ex: classes compiladas, pacotes).
-   **Dependências:** Bibliotecas de terceiros que são baixadas e gerenciadas pela ferramenta de build.
-   **Arquivos de Configuração do Editor/IDE:** Configurações específicas do ambiente de desenvolvimento de um programador, que não devem ser compartilhadas.
-   **Arquivos Temporários e de Cache:** Arquivos criados por ferramentas ou pelo sistema operacional que não têm relevância para o projeto.
-   **Arquivos de Credenciais:** Arquivos contendo senhas, chaves de API ou outras informações sensíveis.

---

## Detalhamento do Conteúdo

O `.gitignore` deste projeto está dividido em seções lógicas:

### Seção `# Leiningen`

Esta seção lista os arquivos e diretórios gerados pelo **Leiningen**, a ferramenta de automação e gerenciamento de dependências para projetos Clojure.

-   `/target`: O diretório padrão onde o Leiningen coloca todos os artefatos de compilação, como arquivos `.class`, o pacote `.jar` final, etc. Este diretório pode ser completamente recriado a partir do código-fonte, por isso não deve ser versionado.
-   `/pom.xml`: O Leiningen pode interoperar com o Maven (outra ferramenta de build) e gera este arquivo para compatibilidade.
-   Arquivos com prefixo `.lein-`: São arquivos de cache, histórico ou estado interno do Leiningen.

### Seção `# Clojure`

-   `/.nrepl-port`: Quando um processo REPL (Read-Eval-Print Loop) do Clojure é iniciado, ele pode criar este arquivo para armazenar o número da porta em que está rodando, permitindo que editores de texto e outras ferramentas se conectem a ele.
-   `/classes/`: Um diretório que pode ser usado para armazenar arquivos de classe Java compilados, caso o projeto inclua código Java.

### Seção `# Editor`

Esta seção contém padrões para ignorar arquivos de configuração e de estado de editores de código e IDEs (Ambientes de Desenvolvimento Integrado) populares.

-   `.idea/` e `*.iml`: Diretórios e arquivos de configuração específicos do IntelliJ IDEA.
-   `.ensime_cache/`: Cache do ENSIME, um ambiente de desenvolvimento para Scala e Java em editores como Emacs e Atom.
-   `*~`, `*#`, `.#*`: Padrões comuns para arquivos de backup e temporários criados por editores como Vim e Emacs.

Manter esses arquivos fora do Git garante que a configuração pessoal de um desenvolvedor não entre em conflito com a de outro.
