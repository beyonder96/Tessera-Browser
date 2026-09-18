# 🌐 Tessera Browser

Um navegador Android minimalista, imersivo e elegante focado em produtividade e estética orgânica escura (*Aero-Dark / Glassmorphism*), construído com **Kotlin**, **Jetpack Compose** e **Android System WebView**.

---

## ✨ Características Principais

* **Tela Inicial Nativa & Discagem Rápida:**
  * Papel de parede orgânico de seda chocolate escuro com vinheta translúcida.
  * Barra de busca central estilizada (*"Pesquisar na Web"*) integrada ao DuckDuckGo e Google.
  * Grade de atalhos rápidos com suporte a adicionar novos sites personalizados.
* **Tessera AirBar Flutuante:**
  * Barra inferior oval flutuante em vidro acrílico translúcido com raio `32.dp`.
  * Recolhimento e expansão automáticos via scroll inteligente (`NestedScrollConnection`).
  * Indicador linear de progresso ciano vibrante (`#64B5F6`).
  * Ações de navegação rápida: Voltar, Início (Home), Recarregar e Configuração fácil.
* **Painel de Configuração Fácil:**
  * Alternador de Ambiente Claro / Escuro.
  * Suporte autêntico a **Forçar páginas escuras** via `androidx.webkit` (`AlgorithmicDarkening`).
  * Galeria horizontal de papéis de parede selecionáveis (Seda Chocolate, Ondas Esmeralda, Dunas Ciano e Obsidiana Pura).
  * Barra de favoritos e barra lateral configuráveis.
  * Opções dedicadas para **Tessera AI**.
* **Design & Layout:**
  * Suporte completo a **Edge-to-Edge** com barras do sistema transparentes.
  * Microinterações fluidas e responsivas sem bloquear a UI thread.

---

## 🛠️ Tecnologias & Arquitetura

* **Linguagem:** Kotlin 2.1.0
* **UI Toolkit:** Jetpack Compose (BOM 2024.12.01) + Material 3
* **Arquitetura:** MVVM (Model-View-ViewModel) com `StateFlow`
* **Web Engine:** `android.webkit.WebView` + `androidx.webkit:webkit:1.12.1`
* **Build System:** Gradle com Version Catalogs (`libs.versions.toml`)
* **SDK:** Min SDK 26 (Android 8.0) | Target/Compile SDK 35 (Android 15)

---

## 🚀 Como Compilar e Rodar

1. Clone este repositório:
   ```bash
   git clone https://github.com/beyonder96/Tessera-Browser.git
   ```
2. Abra o projeto no Android Studio (Ladybug ou superior) ou compile via terminal:
   ```bash
   .\gradlew.bat assembleDebug
   ```
3. O APK gerado estará em `app/build/outputs/apk/debug/app-debug.apk`.
