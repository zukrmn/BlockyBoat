Teste# BlockyBoat

BlockyBoat é um plugin para o servidor BlockyCRAFT que adiciona inventário acessível aos barcos (boats) no Minecraft Beta 1.7.3. Permite que jogadores armazenem e transportem itens em barcos, criando uma nova dinâmica logística no servidor.

## Funcionalidades

- Inventário customizado nos barcos, com tamanho e título configuráveis.
- Inventário acessível via **Shift + Clique Direito** no barco.
- Persistência dos itens armazenados, sobrevivendo a reinícios de servidor (persistidos em data.yml).
- Drop automático dos itens do inventário quando o barco é destruído.
- Compatível com Uberbukkit/CraftBukkit 1060 (Java 8).
- Configuração do plugin via `config.yml`: tamanho, título do inventário, intervalo de auto-save.
- Auto-save dos dados a cada X minutos (configurável).

## Como Funciona

1. O jogador posiciona um barco.
2. Segure **Shift** e clique com o botão direito do mouse no barco para abrir o inventário.
3. Armazene os itens desejados.
4. Se o barco for destruído, todos os itens armazenados são automaticamente largados no solo.

## Configuração

O arquivo `config.yml` permite definir:

- `auto-save-interval`: intervalo (minutos) para salvar os dados dos inventários.
- `inventory-size`: tamanho do inventário do barco (9, 18, 27...).
- `inventory-title`: nome exibido ao abrir o inventário.

## Arquitetura

- **BlockyBoat.java**: classe principal, controla ciclo do plugin, configuração e listeners.
- **listeners/**: contém os listeners para eventos de interação e destruição dos barcos.
- **storage/**: gerenciadores de persistência dos dados, inventário e serialização.
- **util/**: utilitário para gerar identificadores únicos de barcos.
- **resources/**: arquivos de configuração e plugin definition.

## Reportar bugs ou requisitar features

Reporte bugs ou sugestões na seção [Issues](https://github.com/andradecore/BlockyBoat/issues) do projeto. do projeto.

## Contato

- Discord: https://discord.gg/tthPMHrP