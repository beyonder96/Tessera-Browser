[CmdletBinding()]
param (
    [string]$Version,
    [switch]$SkipBuild,
    [switch]$SkipPublish,
    [string]$ReleaseNotes
)

$ErrorActionPreference = "Stop"

$ProjectRoot = (Resolve-Path "$PSScriptRoot\..\..\..\..").Path
Set-Location $ProjectRoot

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " Tessera Browser — Release Automation     " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. Configuração de Ambiente
$DefaultJdk = "C:\Users\kenne\.gemini\antigravity\scratch\jdk-17\jdk-17.0.12+7"
$SysJdk = "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"
$DefaultSdk = "C:\Users\kenne\.gemini\antigravity\scratch\android-sdk"
$UserSdk = "C:\Users\kenne\AppData\Local\Android\Sdk"

if (Test-Path $SysJdk) {
    $env:JAVA_HOME = $SysJdk
} elseif (Test-Path $DefaultJdk) {
    $env:JAVA_HOME = $DefaultJdk
}

if (Test-Path $UserSdk) {
    $env:ANDROID_HOME = $UserSdk
} elseif (Test-Path $DefaultSdk) {
    $env:ANDROID_HOME = $DefaultSdk
}

$env:PATH = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"

Write-Host " Java Home:    $env:JAVA_HOME" -ForegroundColor Gray
Write-Host " Android Home: $env:ANDROID_HOME" -ForegroundColor Gray

# 2. Resolução da Versão
$GradleFile = Join-Path $ProjectRoot "app\build.gradle.kts"
$GradleContent = Get-Content $GradleFile -Raw

$CurrentVersion = ""
if ($GradleContent -match 'versionName\s*=\s*"([^"]+)"') {
    $CurrentVersion = $Matches[1]
}
$CurrentCode = 0
if ($GradleContent -match 'versionCode\s*=\s*(\d+)') {
    $CurrentCode = [int]$Matches[1]
}

Write-Host " Versao Atual: v$CurrentVersion (Code: $CurrentCode)" -ForegroundColor Yellow

if ($Version) {
    $TargetVersion = $Version.TrimStart("v")
    $NewCode = $CurrentCode + 1
    Write-Host " Atualizando para v$TargetVersion (Code: $NewCode)..." -ForegroundColor Green
    
    $NewGradleContent = $GradleContent -replace 'versionCode\s*=\s*\d+', "versionCode = $NewCode"
    $NewGradleContent = $NewGradleContent -replace 'versionName\s*=\s*"[^"]+"', "versionName = `"$TargetVersion`""
    Set-Content -Path $GradleFile -Value $NewGradleContent -NoNewline
} else {
    $TargetVersion = $CurrentVersion
}

# 3. Compilação dos APKs
if (-not $SkipBuild) {
    Write-Host "`n Compilando APKs..." -ForegroundColor Cyan
    & ".\gradlew.bat" assembleRelease assembleDebug --stacktrace
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Falha ao compilar APKs com Gradle."
        exit $LASTEXITCODE
    }

    $ApkDir = Join-Path $ProjectRoot "apk"
    if (-not (Test-Path $ApkDir)) {
        New-Item -ItemType Directory -Path $ApkDir | Out-Null
    }

    $ReleaseSrc = Join-Path $ProjectRoot "app\build\outputs\apk\release\app-release-unsigned.apk"
    if (-not (Test-Path $ReleaseSrc)) {
        $ReleaseSrc = Join-Path $ProjectRoot "app\build\outputs\apk\release\app-release.apk"
    }
    $DebugSrc = Join-Path $ProjectRoot "app\build\outputs\apk\debug\app-debug.apk"

    if (Test-Path $ReleaseSrc) {
        Copy-Item $ReleaseSrc -Destination "$ApkDir\tessera-browser-release.apk" -Force
    }
    if (Test-Path $DebugSrc) {
        Copy-Item $DebugSrc -Destination "$ApkDir\tessera-browser-debug.apk" -Force
    }

    Write-Host " APKs gerados com sucesso na pasta /apk:" -ForegroundColor Green
    Get-ChildItem $ApkDir -Filter "*.apk" | ForEach-Object {
        Write-Host "   - $($_.Name) ($([math]::Round($_.Length / 1MB, 2)) MB)" -ForegroundColor Gray
    }
}

Write-Host "`n Processo concluido para o Tessera Browser!" -ForegroundColor Cyan
