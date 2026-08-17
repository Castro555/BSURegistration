# BSURegistration

Ferramenta em Java para leitura remota de dados de equipamentos Mikrotik (BSUs e SUs), via API nativa do RouterOS, com o objetivo de apoiar a supervisão de uma rede wireless ponto-multiponto.

## Objetivo

O projeto tem 3 classes principais (cada uma com o seu próprio `main`), correspondentes aos 3 objetivos:

| Objetivo | Classe | Comando executado no Mikrotik |
|---|---|---|
| Ler as interfaces Ethernet/SFP dos BSUs | `LeituraBSUether` | `/interface/ethernet/monitor numbers=0/1 once` |
| Ver o número de CPEs (SU) ligados na interface wireless dos BSUs | `LeituraBSU` | `/interface/wireless/registration-table/print count-only` (com *fallback* para `/interface/w60g/station/print count-only`) |
| Ler dados da interface wireless dos SU | `LeituraSU` | `/interface/wireless/print` |

> ⚠️ **Nota:** a leitura do nível de sinal (RSSI/CCQ) na interface wireless dos SU ainda não está implementada — `LeituraSU.getRegistration()` só lê `radio-name` e `ssid`. É um desenvolvimento futuro já identificado.

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
        │   ├── Config.java                # host/credenciais/lista de passwords, partilhados por todas as classes
        │   ├── FileManagement.java        # contagem de linhas, split/merge de ficheiros para processamento paralelo
        │   ├── MultiThreadsProcessor.java # execução de LeituraSU em pool de threads
        │   ├── LeituraBSU.java            # (main) nº de SUs registados no BSU
        │   ├── LeituraBSUether.java       # (main) estado das interfaces Ethernet/SFP do BSU
        │   └── LeituraSU.java             # (main) dados wireless do SU
        ├── me/legrange/mikrotik/          # biblioteca cliente da API RouterOS (Mikrotik), embutida como fonte
        └── examples/                      # exemplos de uso da biblioteca me.legrange.mikrotik (referência, não faz parte do fluxo do projeto)
```

### Classes principais (`cv.ucc.bsuregistration`)

- **`Config`** — constantes de ligação por defeito (`HOST`, `USERNAME`, `PASSWORD`) e a lista única de passwords (`PASSWORDS`) usada por todas as classes ao autenticar nos equipamentos.
- **`FileManagement`** — conta as linhas de `IpList.txt` (`countLines`), divide-o em `numParts` ficheiros (`splitFile`) e depois junta os resultados parciais em `merge.txt` (`mergeFile`), apagando os ficheiros temporários.
- **`MultiThreadsProcessor`** — cria uma *thread pool* e, para cada parte do ficheiro de IPs, corre `LeituraSU` sobre cada linha (IP), escrevendo o resultado em `registration_N.txt`.
- **`LeituraBSU`** — lê `IpList.txt`, liga-se a cada BSU (tentando as passwords de `Config.PASSWORDS`) e regista o número de CPEs (SU) ligados na interface wireless, escrevendo tudo em `registration.csv`.
- **`LeituraBSUether`** — semelhante ao anterior, mas lê o estado das interfaces Ethernet/SFP (nome, status, rate, full-duplex), também escrevendo em `registration.csv`.
- **`LeituraSU`** — usado em conjunto com `FileManagement` e `MultiThreadsProcessor` para percorrer os SUs em paralelo e ler dados da sua interface wireless (atualmente `radio-name` e `ssid`).

### Dependências

- **`me.legrange.mikrotik`** — biblioteca cliente Java para a API do RouterOS (Mikrotik), incluída como código-fonte no próprio projeto (não é uma dependência externa via Maven/Gradle). Liga-se por TCP à porta API por defeito do Mikrotik (`ApiConnection.DEFAULT_PORT`).
- Sem outras dependências externas — usa apenas a biblioteca padrão do Java (`java.io`, `java.nio.file`, `java.util.concurrent`, `java.util.stream`).

## Ficheiros de entrada/saída

| Ficheiro | Descrição |
|---|---|
| `IpList.txt` | Lista de IPs dos BSUs/SUs a interrogar, um por linha |
| `registration.csv` | Resultado de `LeituraBSU` **ou** `LeituraBSUether` — as duas classes escrevem para o mesmo nome de ficheiro; como são scripts corridos localmente (um de cada vez), isto é intencional e não um problema |
| `part_N.txt` | Fragmentos temporários de `IpList.txt`, gerados por `FileManagement.splitFile` |
| `registration_N.txt` | Resultado parcial de cada thread de `MultiThreadsProcessor` |
| `merge.txt` | Resultado final consolidado de `LeituraSU`, depois do merge dos `registration_N.txt` |

## Configuração

- `Config.java` define `HOST`, `USERNAME`, `PASSWORD` por defeito e a lista `PASSWORDS` (`TRM@Unitel@123`, `admin`, vazio), tentada sequencialmente por `LeituraBSU`, `LeituraBSUether` e `MultiThreadsProcessor` ao autenticar em cada equipamento.
- Como os scripts correm sempre localmente (não são distribuídos para outra máquina), as credenciais ficam em `Config.java`; o ficheiro `IpList.txt` é limpo antes de cada commit para não versionar dados sensíveis.

## Como executar

Projeto Eclipse simples (sem build tool). Passos:

1. Importar a pasta `BsuRegistration` no Eclipse como *Existing Java Project*.
2. Preencher `IpList.txt` (na raiz de `BsuRegistration`, junto ao `.classpath`) com um IP por linha.
3. Executar a classe correspondente ao objetivo pretendido:
   - `LeituraBSUether` → estado das portas Ethernet/SFP dos BSUs
   - `LeituraBSU` → nº de SUs registados nos BSUs
   - `LeituraSU` → dados wireless dos SUs (processamento paralelo via `MultiThreadsProcessor`; `numParts` é calculado automaticamente a partir do total de IPs em `IpList.txt`, dividido em blocos de 100)
4. Consultar o `registration.csv` (para `LeituraBSU`/`LeituraBSUether`) ou `merge.txt` (para `LeituraSU`) gerado na raiz do projeto.

## Histórico de correções

Pontos identificados numa primeira validação do código e já corrigidos:

- ✅ **Lista de passwords centralizada** — `Config.PASSWORDS` substitui as listas duplicadas que existiam em `LeituraBSU`, `LeituraBSUether` e `MultiThreadsProcessor`.
- ✅ **`registration-table` vazia já não gera `NullPointerException`** — `LeituraBSU.getRegistration()` passa a devolver `"0"` quando não há resultados (tanto na `registration-table` como no `w60g/station`), em vez de arriscar NPE ao comparar `registration.equals("[0]")` com `registration == null`.
- ✅ **`numParts` calculado dinamicamente** — `LeituraSU.main` usa `FileManagement.countLines` (via `Files.lines().count()`, sem carregar o ficheiro todo para uma `List`) para saber o total de IPs em `IpList.txt` e definir `numParts` como esse total a dividir por 100 (mínimo 1), em vez de um valor fixo de 1.

Decisões conscientes (não são bugs):
- `registration.csv` partilhado entre `LeituraBSU` e `LeituraBSUether` — os scripts correm localmente, um de cada vez, sem necessidade de outputs separados por agora.
- Credenciais em `Config.java` — aceitável no contexto atual (scripts locais); `IpList.txt` é limpo antes de cada commit.

## Pontos de atenção ainda em aberto

- **Leitura de sinal por implementar** — `LeituraSU` ainda não lê o nível de sinal (RSSI/CCQ) da interface wireless dos SU. A avaliar futuramente (ex.: `/interface/wireless/monitor` ou campo `signal-strength`).
- **Fecho de recursos** — os `BufferedReader`/`BufferedWriter` em `LeituraBSU`, `LeituraBSUether` e `MultiThreadsProcessor` só são fechados após o `while`, fora de um bloco `try-with-resources`; se ocorrer uma exceção a meio do ciclo, os ficheiros podem não ser fechados corretamente.
- **Pasta `examples/`** — contém exemplos da biblioteca `me.legrange.mikrotik` não relacionados com a lógica do projeto; útil como referência, mas não faz parte do fluxo de execução.

## Melhorias sugeridas

- Implementar a leitura de sinal (RSSI/CCQ) em `LeituraSU`.
- Unificar as 3 classes principais numa única aplicação com um parâmetro/menu para escolher o modo (Ethernet/BSU/SU), evitando duplicação de código de ligação.
- Adicionar tratamento de exceções mais granular e `try-with-resources` para garantir o fecho de ficheiros e ligações.

## Autor

Helcilino
