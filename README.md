# BlockyBoat

BlockyBoat é um plugin para servidor Minecraft Beta 1.7.3 (Uberbukkit/CraftBukkit 1060) que adiciona inventário customizado e **persistente** aos barcos (boats) usando **SQLite**. Permite aos jogadores armazenar e transportar itens em barcos, criando uma nova dinâmica logística no servidor. Os itens do barco sobrevivem a reinícios e crashes do servidor.

## Funcionalidades

- Inventário customizado nos barcos, com tamanho e título configuráveis (`config.yml`).
- Inventário persistido em banco SQLite: dados permanecem salvos mesmo após reiniciar/crash.
- Inventário acessível via **Shift + Clique Direito** no barco.
- Drop automático dos itens do inventário do barco ao ser destruído (juntamente com itens padrão do Minecraft).
- Compatível com Uberbukkit/CraftBukkit 1060 (Java 8).
- Auto-save dos dados dos barcos a cada X minutos (configurável).

## Como Funciona

1. O jogador posiciona um barco.
2. Segure **Shift** e clique com o botão direito do mouse no barco para abrir o inventário.
3. Armazene os itens desejados.
4. Se o barco for destruído, todos os itens armazenados são automaticamente largados junto aos drops padrão.

## Configuração

O arquivo `config.yml` permite definir:

- `auto-save-interval`: intervalo (minutos) para salvar os dados dos inventários.
- `inventory-size`: tamanho do inventário do barco (9, 18, 27...).
- `inventory-title`: nome exibido ao abrir o inventário.

## Arquitetura

- `BlockyBoat.java`: classe principal, controla ciclo do plugin e inicialização do SQLite.
- `storage/BlockyBoatDatabase.java`: banco SQLite, gerencia persistência dos inventários.
- `storage/StorageManager.java`: gerencia cache de inventários por barco.
- `util/BoatIdentifier.java`: utilitário para gerar identificadores únicos de barcos.
- `listeners/BoatBreakListener.java`: dropa o inventário do barco ao quebrar.
- `listeners/BoatInteractListener.java`: abre inventário ao Shift + Clique Direito.
- `resources/config.yml`: configuração.
- `resources/plugin.yml`: metadados do plugin.

## Requisitos

- Java 8
- CraftBukkit/Uberbukkit 1.7.3 (build 1060)
- Driver JDBC SQLite (deve estar disponível no Java SE por padrão)

## Instalação

1. Baixe e coloque o plugin em `plugins/`.
2. Inicie o servidor. O banco `blockyboat.db` será criado na pasta de dados do plugin.

## Reportar bugs ou requisitar features

Reporte bugs ou sugestões na seção [Issues](https://github.com/andradecore/BlockyBoat/issues) do projeto. do projeto.

## Contato

- Discord: https://discord.gg/tthPMHrP