# Tessera Browser — Antigravity Agent Guidelines

Este repositório é o **Tessera Browser**, um navegador Android focado em produtividade, personalização dinâmica e design moderno em Jetpack Compose.

---

## 🛠️ Ambiente de Desenvolvimento & Compilação

Para garantir builds consistentes no ambiente local do Windows:
- **Java Home (JDK 17):** `C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot` (ou `C:\Users\kenne\.gemini\antigravity\scratch\jdk-17\jdk-17.0.12+7`)
- **Android SDK:** `C:\Users\kenne\AppData\Local\Android\Sdk` (ou `C:\Users\kenne\.gemini\antigravity\scratch\android-sdk`)
- **Compile SDK:** 35
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 35
- **Build Tool:** Gradle (`.\gradlew.bat`)

---

## ⚡ Comandos e Actions Disponíveis

### 1. Slash Command / Skill de Validação Rápida (`/check`)
Sempre que fizer alterações em telas Compose, componentes ou dependências:
- Execute a skill `check` em `.agents/skills/check/SKILL.md`
- Ou rode diretamente o script:
  ```powershell
  & ".\.agents\skills\check\scripts\check.ps1"
  ```
- Comando direto:
  ```powershell
  .\gradlew.bat compileDebugKotlin --daemon
  ```

### 2. Slash Command / Skill de Release (`/release-apk`)
Sempre que solicitado para gerar release ou novos APKs:
- Execute a skill `release-apk` em `.agents/skills/release-apk/SKILL.md`
- Ou execute:
  ```powershell
  & ".\.agents\skills\release-apk\scripts\release.ps1" -Version "1.0.1"
  ```

---

## 📱 Diretrizes de Código e UI Jetpack Compose

1. **Performance e Recomposição:**
   - Utilize `remember` e `derivedStateOf` para estados mutáveis no QuickSettings e Start Page.
   - Evite recalcular transformações de papéis de parede ou temas durante recomposição.
2. **Design System & Glassmorphism:**
   - Manter consistência com a identidade visual do ecossistema Tessera (cantos arredondados, pílulas de seleção, blur suave e temas Light/Dark).
