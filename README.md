# OpenMedia SDK

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![Version](https://img.shields.io/badge/Version-0.1.0-orange.svg)](#roadmap)

**OpenMedia SDK** é uma biblioteca Android open source modular, simples, desacoplada e extensível para reprodução e gestão de conteúdos multimédia (áudio e vídeo).

O SDK foi projectado desde a sua concepção para servir de fundação sólida para aplicações modernas de reprodução de media (como o futuro **OpenPlayer**), fornecendo contratos fundamentais, modelos imutáveis, normalização de estados e eventos, gestão de filas e integração com múltiplos providers de conteúdos.

---

## 1. Objectivo

O **OpenMedia SDK não é uma aplicação de media player**.

É uma biblioteca Android que fornece os contratos e abstrações essenciais para:

- **Representar conteúdos multimédia** de forma uniforme (`MediaItem`, `MediaType`, `MediaSource`).
- **Controlar reprodução** através de uma interface agnóstica (`Player`) com implementação pronta para **Media3/ExoPlayer** (`Media3Player`).
- **Gerir filas de reprodução sequenciais** com navegação e reordenação (`Queue`).
- **Organizar colecções e listas de faixas** (`Playlist`).
- **Pesquisar conteúdos através de múltiplos providers externos** (`MediaProvider`, `MediaSearchManager`).
- **Observar eventos e estados de reprodução em tempo real** com `StateFlow` e `Flow` (`PlaybackState`, `PlaybackEvent`).
- **Isolar a aplicação de complexidades de baixo nível** convertendo erros proprietários para erros padronizados (`MediaError`).

---

## 2. Arquitectura

O SDK segue os princípios de **Clean Architecture**, **SOLID** e separação estrita de responsabilidades:

```text
┌─────────────────────────────────────────────────────────┐
│                    OpenMedia Core                       │
│  (Contratos, Modelos, Filas, Playlists, Providers, Flow) │
│       * Não conhece ExoPlayer / Media3 / UI *          │
└───────────────────────────┬─────────────────────────────┘
                            │ implementa / estende
┌───────────────────────────▼─────────────────────────────┐
│                   OpenMedia Player                      │
│        (Player Interface, Controlo de Reprodução)       │
└───────────────────────────┬─────────────────────────────┘
                            │ adaptação
┌───────────────────────────▼─────────────────────────────┐
│                 Media3Player (AndroidX)                 │
│         (Normalização de Estados, Eventos e Erros)      │
└─────────────────────────────────────────────────────────┘
```

### Princípios Arquitecturais:
1. **Zero UI Coupling:** O SDK não utiliza Jetpack Compose nem Views Android em seu interior.
2. **Engine Decoupled:** O core do SDK não depende de bibliotecas externas de reprodução.
3. **Normalized Engine:** As aplicações interagem apenas com `Player` e `PlaybackState`, sem expor o ExoPlayer directamente.
4. **Reatividade Idiomática:** Exposição de estados e eventos com `StateFlow` e `SharedFlow`.

---

## 3. Estrutura do Projecto

```text
openmedia-sdk/
│
├── src/main/java/org/openmedia/sdk/
│   │
│   ├── core/
│   │   ├── MediaItem.kt            # Abstração universal de media (áudio/vídeo)
│   │   ├── MediaType.kt            # AUDIO e VIDEO (extensível)
│   │   ├── MediaSource.kt          # Origem, URI, MIME type e qualidade
│   │   ├── Playlist.kt             # Colecção ordenada e gestão de faixas
│   │   ├── Queue.kt                # Fila de reprodução activa com navegação
│   │   ├── MediaProvider.kt        # Contrato para provedores externos
│   │   ├── ProviderCapability.kt   # SEARCH, STREAM, DOWNLOAD
│   │   ├── MediaSearchManager.kt   # Pesquisa unificada em múltiplos providers
│   │   ├── PlaybackState.kt        # IDLE, LOADING, PLAYING, PAUSED, BUFFERING, COMPLETED, ERROR
│   │   ├── PlaybackEvent.kt        # Eventos reativos de ciclo de vida
│   │   └── MediaError.kt           # Erros normalizados do SDK
│   │
│   └── player/
│       ├── Player.kt               # Interface principal de reprodução
│       └── Media3Player.kt         # Implementação concreta com AndroidX Media3
│
├── src/test/java/org/openmedia/sdk/
│   ├── core/
│   │   ├── MediaItemTest.kt        # Testes de modelos e validações
│   │   ├── QueueTest.kt            # Testes de operações de fila (add, remove, move, next, prev)
│   │   ├── PlaylistTest.kt         # Testes de listas e reordenação
│   │   ├── MediaErrorTest.kt       # Testes de categorização de erros
│   │   └── MediaSearchManagerTest.kt # Testes de busca multi-provider e tolerância a falhas
│   └── player/
│       └── PlaybackStateTest.kt    # Testes de estados normalizados
│
└── README.md
```

---

## 4. Funcionalidades da Versão Inicial (v0.1)

- [x] **MediaItem & MediaSource:** Suporte a conteúdos de áudio e vídeo com metadados extensíveis.
- [x] **Player Abstraction:** Interface unificada (`play`, `pause`, `resume`, `stop`, `seekTo`, `setMedia`, `getCurrentPosition`, `getDuration`).
- [x] **Media3Player:** Implementação de alta performance baseada em `androidx.media3:media3-exoplayer`.
- [x] **PlaybackState Normalizado:** 7 estados universais (`IDLE`, `LOADING`, `PLAYING`, `PAUSED`, `BUFFERING`, `COMPLETED`, `ERROR`).
- [x] **Queue Reativa:** Adicionar, remover, mover, avançar (`next`), retroceder (`previous`), limpar e observar via `StateFlow`.
- [x] **Playlist:** Gestão ordered de faixas e metadados.
- [x] **MediaProvider & ProviderCapability:** Contrato para busca e streaming (`SEARCH`, `STREAM`, `DOWNLOAD`).
- [x] **MediaSearchManager:** Agregação de buscas assíncronas concorrentes com tratamento de falhas por provider.
- [x] **Eventos Reativos via Flow:** `OnPlay`, `OnPause`, `OnBuffering`, `OnCompleted`, `OnError`, `OnPositionChanged`.
- [x] **MediaError Padronizado:** Conversão de falhas técnicas para `MEDIA_NOT_FOUND`, `SOURCE_UNAVAILABLE`, `UNSUPPORTED_FORMAT`, `PLAYBACK_ERROR`, `NETWORK_ERROR`.

---

## 5. Instalação

Adicione o módulo `openmedia-sdk` às dependências do seu projecto Android:

```kotlin
// settings.gradle.kts
include(":openmedia-sdk")
```

No ficheiro `build.gradle.kts` da sua aplicação:

```kotlin
dependencies {
    implementation(project(":openmedia-sdk"))

    // Coroutines para observação de Flow
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}
```

---

## 6. Exemplo de Utilização

### 6.1 Criar Media e Reproduzir com Media3Player

```kotlin
import org.openmedia.sdk.core.MediaItem
import org.openmedia.sdk.core.MediaSource
import org.openmedia.sdk.core.MediaType
import org.openmedia.sdk.player.Player
import org.openmedia.sdk.player.Media3Player

// 1. Instanciar o player (passando o Context do Android)
val player: Player = Media3Player(context)

// 2. Definir o item de media
val media = MediaItem(
    id = "song_001",
    title = "Bohemian Rhapsody",
    type = MediaType.AUDIO,
    source = MediaSource(
        uri = "https://example.com/audio/bohemian_rhapsody.mp3",
        mimeType = "audio/mpeg",
        quality = "320kbps"
    ),
    duration = 354000L,
    metadata = mapOf(
        MediaItem.METADATA_ARTIST to "Queen",
        MediaItem.METADATA_ALBUM to "A Night at the Opera"
    )
)

// 3. Carregar e reproduzir
player.setMedia(media)
player.play()
```

### 6.2 Observar Estados e Eventos com Flow

```kotlin
lifecycleScope.launch {
    // Observar estado reativo
    player.state.collect { state ->
        when (state) {
            PlaybackState.IDLE -> println("Player pronto ou parado")
            PlaybackState.LOADING -> println("A carregar faixa...")
            PlaybackState.PLAYING -> println("A reproduzir")
            PlaybackState.PAUSED -> println("Em pausa")
            PlaybackState.BUFFERING -> println("Buffer em curso...")
            PlaybackState.COMPLETED -> println("Faixa concluída!")
            PlaybackState.ERROR -> println("Ocorreu um erro")
        }
    }
}

lifecycleScope.launch {
    // Observar eventos específicos
    player.events.collect { event ->
        when (event) {
            is PlaybackEvent.OnPositionChanged -> {
                println("Posição: ${event.positionMs}ms de ${event.durationMs}ms")
            }
            is PlaybackEvent.OnError -> {
                println("Erro padronizado: ${event.error.code} - ${event.error.message}")
            }
            else -> {}
        }
    }
}
```

### 6.3 Gestão da Fila de Reprodução (Queue)

```kotlin
val queue = Queue()

// Adicionar faixas
queue.add(trackA)
queue.addAll(listOf(trackB, trackC))

// Obter faixa actual
val current = queue.current()

// Avançar ou retroceder
val nextTrack = queue.next()
val prevTrack = queue.previous()

// Mover elementos na fila (ex: mover do índice 2 para o topo no índice 0)
queue.move(fromIndex = 2, toIndex = 0)

// Observar alterações na fila
lifecycleScope.launch {
    queue.itemsFlow.collect { items ->
        println("Fila actualizada: ${items.size} faixas")
    }
}
```

---

## 7. Como Criar um MediaProvider

Qualquer fonte de conteúdos pode ser integrada implementando a interface `MediaProvider`:

```kotlin
import org.openmedia.sdk.core.MediaItem
import org.openmedia.sdk.core.MediaProvider
import org.openmedia.sdk.core.MediaSource
import org.openmedia.sdk.core.MediaType
import org.openmedia.sdk.core.ProviderCapability

class LocalMusicProvider : MediaProvider {
    override val id: String = "local_music"
    override val name: String = "Músicas Locais"

    override val capabilities: Set<ProviderCapability> = setOf(
        ProviderCapability.SEARCH,
        ProviderCapability.STREAM
    )

    override suspend fun search(query: String): List<MediaItem> {
        // Exemplo: Consultar o MediaStore do Android ou API remota
        return listOf(
            MediaItem(
                id = "local_1",
                title = "Resultado para: $query",
                type = MediaType.AUDIO,
                source = MediaSource(uri = "file:///storage/emulated/0/Music/track.mp3")
            )
        )
    }
}
```

### Executar Pesquisa Unificada

```kotlin
val searchManager = MediaSearchManager(
    initialProviders = listOf(LocalMusicProvider(), PodcastProvider())
)

// Pesquisa agregada em todos os provedores registrados
val results = searchManager.search("Imagine Dragons")
```

---

## 8. Como Implementar um Player Customizado

Se pretender utilizar outro backend de reprodução (por exemplo, `MediaPlayer` nativo do Android ou um player C++ customizado):

```kotlin
class CustomNativePlayer : Player {
    private val _state = MutableStateFlow(PlaybackState.IDLE)
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PlaybackEvent>()
    override val events: Flow<PlaybackEvent> = _events.asSharedFlow()

    private val _currentMedia = MutableStateFlow<MediaItem?>(null)
    override val currentMedia: StateFlow<MediaItem?> = _currentMedia.asStateFlow()

    override fun setMedia(media: MediaItem) {
        _currentMedia.value = media
        _state.value = PlaybackState.LOADING
        // Configuração do motor proprietário...
    }

    override fun play() { /* ... */ }
    override fun pause() { /* ... */ }
    override fun resume() { /* ... */ }
    override fun stop() { /* ... */ }
    override fun seekTo(positionMs: Long) { /* ... */ }
    override fun getCurrentPosition(): Long = 0L
    override fun getDuration(): Long = 0L
    override fun getState(): PlaybackState = _state.value
    override fun release() { /* libertar recursos */ }
}
```

---

## 9. Tratamento de Erros Normalizado

O SDK intercepta falhas técnicas subjacentes (códigos HTTP, timeouts, codecs inválidos do ExoPlayer) e traduz para `MediaError`:

```kotlin
when (mediaError.code) {
    MediaErrorCode.MEDIA_NOT_FOUND -> mostrarAviso("Conteúdo não encontrado.")
    MediaErrorCode.SOURCE_UNAVAILABLE -> mostrarAviso("Stream indisponível ou inacessível.")
    MediaErrorCode.UNSUPPORTED_FORMAT -> mostrarAviso("Formato de áudio/vídeo não suportado.")
    MediaErrorCode.NETWORK_ERROR -> mostrarAviso("Falha de conexão com a rede.")
    MediaErrorCode.PLAYBACK_ERROR -> mostrarAviso("Falha ao inicializar descodificador.")
}
```

---

## 10. Roadmap

### V0.1 (Versão Atual)
- [x] Contratos centrais (`MediaItem`, `MediaType`, `MediaSource`)
- [x] Interface `Player` e implementação `Media3Player`
- [x] Normalização de estados (`PlaybackState`) e erros (`MediaError`)
- [x] Gestão de filas (`Queue`) e listas (`Playlist`)
- [x] Contrato `MediaProvider` e `MediaSearchManager`
- [x] Emissão reativa com Kotlin Coroutines e `Flow`
- [x] Cobertura de testes unitários fundamentais

### V0.2
- [ ] Enriquecimento de metadados avançados (letras embebidas, chapters)
- [ ] Histórico de reprodução e favoritos
- [ ] Cache local inteligente para streaming
- [ ] Mais tipos de media (Live HLS/DASH streams, Rádios online)
- [ ] Suporte a equalizador e controle de velocidade de reprodução

### V0.3+
Módulos externos independentes mantidos pela comunidade:
- `openmedia-youtube`: Provider de conteúdos e busca YouTube
- `openmedia-podcast`: Parser de RSS de podcasts e subscrições
- `openmedia-radio`: Directório de estações de rádio Icecast/Shoutcast
- `openmedia-download`: Motor de downloads e reprodução offline
- `openmedia-lyrics`: Sincronização de letras LRC

---

## 11. Contribuição

Contribuições da comunidade são muito bem-vindas!

1. Faça um Fork do repositório
2. Crie uma branch para a sua feature (`git checkout -b feature/minha-feature`)
3. Adicione testes para cobrir suas alterações
4. Garanta que `./gradlew test` compila e passa 100% verde
5. Submeta um Pull Request detalhado

---

## 12. Licença

Distribuído sob a licença **Apache 2.0**. Consulte `LICENSE` para mais detalhes.
