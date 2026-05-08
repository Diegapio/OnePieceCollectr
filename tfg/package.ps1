# Script para generar el instalador de OnePieceCollectr
# Ejecutar desde la carpeta tfg/: .\package.ps1

$ErrorActionPreference = "Stop"

Write-Host "=== OnePieceCollectr - Generando .exe ===" -ForegroundColor Cyan

# 1. Build
Write-Host "`n[1/3] Compilando y empaquetando..." -ForegroundColor Yellow
& mvn clean package
if ($LASTEXITCODE -ne 0) { Write-Error "Build fallido."; exit 1 }

# 2. Rutas de JavaFX (desde cache Maven local)
$m2 = "C:\Users\$env:USERNAME\.m2\repository\org\openjfx"
$jfxVer = "17.0.2"
$modulePath = @(
    "$m2\javafx-controls\$jfxVer\javafx-controls-$jfxVer-win.jar",
    "$m2\javafx-fxml\$jfxVer\javafx-fxml-$jfxVer-win.jar",
    "$m2\javafx-graphics\$jfxVer\javafx-graphics-$jfxVer-win.jar",
    "$m2\javafx-base\$jfxVer\javafx-base-$jfxVer-win.jar"
) -join ";"

foreach ($jar in $modulePath -split ";") {
    if (-not (Test-Path $jar)) {
        Write-Error "No se encuentra: $jar`nEjecuta 'mvn javafx:run' una vez para descargar los jars de JavaFX."
        exit 1
    }
}

# 3. jpackage
Write-Host "`n[2/3] Generando imagen de la aplicacion..." -ForegroundColor Yellow
$dest = "target\installer"
if (Test-Path $dest) { Remove-Item $dest -Recurse -Force }

& jpackage `
    --type app-image `
    --name "OnePieceCollectr" `
    --app-version "1.0" `
    --input "target\libs" `
    --main-jar "tfg-1.0-SNAPSHOT.jar" `
    --main-class "com.onepiececollectr.App" `
    --module-path $modulePath `
    --add-modules "javafx.controls,javafx.fxml,java.sql,java.desktop,java.logging,java.naming,java.xml,jdk.crypto.ec,jdk.crypto.cryptoki" `
    --dest $dest

if ($LASTEXITCODE -ne 0) { Write-Error "jpackage fallido."; exit 1 }

Write-Host "`n[3/3] Listo!" -ForegroundColor Green
Write-Host "Ejecutable: $PWD\$dest\OnePieceCollectr\OnePieceCollectr.exe" -ForegroundColor Green
Write-Host "Para entregar: comprime la carpeta '$dest\OnePieceCollectr' en un ZIP." -ForegroundColor Green
