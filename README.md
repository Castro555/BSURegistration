# BSURegistration

Ferramenta em Java para leitura remota de dados de equipamentos Mikrotik (BSUs e SUs), via API nativa do RouterOS, com o objetivo de apoiar a supervisão de uma rede wireless ponto-multiponto.

## Objetivo

O projeto tem 3 classes principais (cada uma com o seu próprio `main`), correspondentes aos 3 objetivos:

| Objetivo | Classe | Comando executado no Mikrotik |
|---|---|---|
| Ler as interfaces Ethernet/SFP dos BSUs | `LeituraBSUether` | `/interface/ethernet/monitor numbers=0/1 once` |
| Ver o número de CPEs (SU) ligados na interface wireless dos BSUs | `LeituraBSU` | `/interface/wireless/registration-table/print count-only` (com *fallback* para `/interface/w60g/station/print count-only`) |
| Ler dados da interface wireless dos SU | `LeituraSU` | `/interface/wireless/print` |

> ⚠️ **Nota de validação:** o objetivo "ler o sinal na interface wireless dos SU" ainda não está implementado. `LeituraSU.getRegistration()` atualmente só lê `radio-name` e `ssid` a partir de `/interface/wireless/print` — não há leitura de `signal-strength` (ou equivalente). Ver secção [Pontos de atenção](#pontos-de-atenção-encontrados-na-validação).

## Estrutura do projeto

Projeto Eclipse (Java puro, sem Maven/Gradle — gerido por `.project`/`.classpath`).

```
BSURegistration/
├── .gitignore
└── BsuRegistration/
    ├── .classpath / .project     # configuração do projeto Eclipse
    ├── IpList.txt                # lista de IPs a interrogar (input)
    ├── registration.csv          # output de LeituraBSU / LeituraBSUether
    ├── merge.txt                 # output final de LeituraSU
    ├── doc/                      # Javadoc gerado da biblioteca me.legrange.mikrotik
    └── src/
        ├── cv/ucc/bsuregistration/    # código do projeto
        │   ├── Config.java                # host/credenciais por defeito
        │   ├── FileManagement.java        # split/merge de ficheiros para processamento paralelo
        │   ├── MultiThreadsProcessor.java # execução de LeituraSU em pool de threads
        │   ├── LeituraBSU.java            # (main) nº de SUs registados no BSU
        │   ├── LeituraBSUether.java       # (main) estado das interfaces Ethernet/SFP do BSU
        │   └── LeituraSU.java             # (main) dados wireless do SU
        ├── me/legrange/mikrotik/          # biblioteca cliente da API RouterOS (Mikrotik), embutida como fonte
        └── examples/                      # exemplos de uso da biblioteca me.legrange.mikrotik (referência, não faz parte do fluxo do projeto)
```

### Classes principais (`cv.ucc.bsuregistration`)

- **`Config`** — constantes de ligação por defeito (`HOST`, `USERNAME`, `PASSWORD`).
- **`FileManagement`** — divide `IpList.txt` em `numParts` ficheiros (`splitFile`) e depois junta os resultados parciais em `merge.txt` (`mergeFile`), apagando os ficheiros temporários.
- **`MultiThreadsProcessor`** — cria uma *thread pool* e, para cada parte do ficheiro de IPs, corre `LeituraSU` sobre cada linha (IP), escrevendo o resultado em `registration_N.txt`.
- **`LeituraBSU`** — lê `IpList.txt`, liga-se a cada BSU (com lista de passwords a tentar) e regista o número de CPEs (SU) ligados na interface wireless, escrevendo tudo em `registration.csv`.
- **`LeituraBSUether`** — semelhante ao anterior, mas lê o estado das interfaces Ethernet/SFP (nome, status, rate, full-duplex), também escrevendo em `registration.csv`.
- **`LeituraSU`** — usado em conjunto com `FileManagement` e `MultiThreadsProcessor` para percorrer os SUs em paralelo e ler dados da sua interface wireless (atualmente `radio-name` e `ssid`).

### Dependências

- **`me.legrange.mikrotik`** — biblioteca cliente Java para a API do RouterOS (Mikrotik), incluída como código-fonte no próprio projeto (não é uma dependência externa via Maven/Gradle). Liga-se por TCP à porta API por defeito do Mikrotik (`ApiConnection.DEFAULT_PORT`).
- Sem outras dependências externas — usa apenas a biblioteca padrão do Java (`java.io`, `java.nio.file`, `java.util.concurrent`).

## Ficheiros de entrada/saída

| Ficheiro | Descrição |
|---|---|
| `IpList.txt` | Lista de IPs dos BSUs/SUs a interrogar, um por linha (atualmente vazio no repositório) |
| `registration.csv` | Resultado de `LeituraBSU` **ou** `LeituraBSUether` (⚠️ ambas escrevem para o mesmo nome de ficheiro — ver pontos de atenção) |
| `part_N.txt` | Fragmentos temporários de `IpList.txt`, gerados por `FileManagement.splitFile` |
| `registration_N.txt` | Resultado parcial de cada thread de `MultiThreadsProcessor` |
| `merge.txt` | Resultado final consolidado de `LeituraSU`, depois do merge dos `registration_N.txt` |

## Configuração

- `Config.java` define `HOST`, `USERNAME` e `PASSWORD` por defeito (não usados diretamente por todas as classes).
- Cada classe principal (`LeituraBSU`, `LeituraBSUether`, `MultiThreadsProcessor`) mantém no código uma lista de passwords a tentar sequencialmente por equipamento, até uma autenticar com sucesso.

## Como executar

Projeto Eclipse simples (sem build tool). Passos:

1. Importar a pasta `BsuRegistration` no Eclipse como *Existing Java Project*.
2. Preencher `IpList.txt` (na raiz de `BsuRegistration`, junto ao `.classpath`) com um IP por linha.
3. Executar a classe correspondente ao objetivo pretendido:
   - `LeituraBSUether` → estado das portas Ethernet/SFP dos BSUs
   - `LeituraBSU` → nº de SUs registados nos BSUs
   - `LeituraSU` → dados wireless dos SUs (processamento paralelo via `MultiThreadsProcessor`)
4. Consultar o `registration.csv` (para `LeituraBSU`/`LeituraBSUether`) ou `merge.txt` (para `LeituraSU`) gerado na raiz do projeto.

## Pontos de atenção encontrados na validação

- **Leitura de sinal não implementada** — `LeituraSU` não lê atualmente o nível de sinal (RSSI/CCQ) da interface wireless dos SU, apesar de ser um dos objetivos descritos. Seria necessário adicionar, por exemplo, `/interface/wireless/monitor` ou consultar o campo `signal-strength` do resultado.
- **Credenciais em texto simples no código-fonte** — `Config.java` e as listas de passwords em `LeituraBSU`, `LeituraBSUether` e `MultiThreadsProcessor` contêm credenciais reais em texto simples, e não estão excluídas no `.gitignore` — ficam versionadas no Git. Recomenda-se mover para variáveis de ambiente ou um ficheiro de configuração fora do controlo de versão.
- **`registration.csv` partilhado** — `LeituraBSU` e `LeituraBSUether` escrevem ambas para `registration.csv`; correr uma a seguir à outra sobrescreve o resultado da anterior.
- **Risco de `NullPointerException`** — em `LeituraBSU.getRegistration()`, se `/interface/wireless/registration-table/print count-only` não devolver nenhum resultado, `registration` fica `null` e a chamada seguinte `registration.equals("[0]")` lança NPE.
- **Fecho de recursos** — os `BufferedReader`/`BufferedWriter` em `LeituraBSU`, `LeituraBSUether` e `MultiThreadsProcessor` só são fechados após o `while`, fora de um bloco `try-with-resources`; se ocorrer uma exceção a meio do ciclo, os ficheiros não são fechados corretamente.
- **`IpList.txt` vazio** — o ficheiro está atualmente vazio no repositório; é necessário preenchê-lo antes de correr qualquer uma das classes.
- **Paralelismo não aproveitado por omissão** — `LeituraSU.main` chama `FileManagement.splitFile(inputFile, 1)`, ou seja, por defeito corre com `numParts = 1` (sem paralelismo real), apesar de toda a infraestrutura de `MultiThreadsProcessor` já suportar múltiplas threads.
- **Pasta `examples/`** — contém exemplos da biblioteca `me.legrange.mikrotik` não relacionados com a lógica do projeto; útil como referência, mas não faz parte do fluxo de execução.

## Melhorias sugeridas

- Implementar a leitura de sinal (RSSI/CCQ) em `LeituraSU`.
- Externalizar credenciais (variáveis de ambiente, ficheiro `.properties` ignorado pelo Git).
- Unificar as 3 classes principais numa única aplicação com um parâmetro/menu para escolher o modo (Ethernet/BSU/SU), evitando duplicação de código de ligação.
- Nomes de ficheiro de saída distintos por finalidade (ex.: `ethernet.csv`, `bsu_registration.csv`, `su_signal.csv`).
- Migrar para Maven/Gradle para gestão de dependências e build reprodutível.
- Adicionar tratamento de exceções mais granular e `try-with-resources` para garantir o fecho de ficheiros e ligações.

## Autor

Helcilino
