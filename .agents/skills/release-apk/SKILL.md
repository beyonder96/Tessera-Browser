---
name: release-apk
description: >-
  Build, package, sign and release Android APKs for Tessera Browser, create Git tags, and publish GitHub Releases with attached APKs.
---

# Release APK & GitHub Publishing Workflow — Tessera Browser

Esta skill / slash command automatiza o ciclo completo de build, geração de APKs (Release & Debug), versionamento e publicação para o **Tessera Browser**.

## Quando Usar
- Quando o usuário pedir para gerar release, gerar APKs, subir nova versão do navegador ou executar `/release-apk`.
- Quando o usuário executar `/release-apk` no chat do Antigravity.

## Passos de Execução

### 1. Verificar Versão Atual e Mudanças Pendentes
- Inspecione `app/build.gradle.kts` para ler `versionCode` e `versionName`.
- Se o usuário especificou uma nova versão (ex: `1.0.1`), atualize `versionCode = versionCode + 1` e `versionName = "x.y.z"` no `app/build.gradle.kts`.

### 2. Configurar Ambiente Local
Certifique-se de que o JDK 17 e Android SDK estão no PATH da sessão:
```powershell
$env:JAVA_HOME = if (Test-Path "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot") { "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot" } else { "C:\Users\kenne\.gemini\antigravity\scratch\jdk-17\jdk-17.0.12+7" }
$env:ANDROID_HOME = if (Test-Path "C:\Users\kenne\AppData\Local\Android\Sdk") { "C:\Users\kenne\AppData\Local\Android\Sdk" } else { "C:\Users\kenne\.gemini\antigravity\scratch\android-sdk" }
$env:PATH = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:PATH"
```

### 3. Compilar APKs (Release e Debug)
Execute:
```powershell
.\gradlew.bat assembleRelease assembleDebug --stacktrace
```

Copie os binários gerados para a pasta `apk/`:
```powershell
New-Item -ItemType Directory -Force -Path "apk"
Copy-Item "app/build/outputs/apk/release/app-release.apk" -Destination "apk/tessera-browser-release.apk" -Force
Copy-Item "app/build/outputs/apk/debug/app-debug.apk" -Destination "apk/tessera-browser-debug.apk" -Force
```

### 4. Atalho Rápido via Script de Automação
```powershell
& ".\.agents\skills\release-apk\scripts\release.ps1" -Version "1.0.1"
```
