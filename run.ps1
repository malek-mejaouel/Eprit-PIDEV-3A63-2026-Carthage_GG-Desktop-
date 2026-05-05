# Script PowerShell pour lancer l'application CarthageGG

# Configuration du JAVA_HOME
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

# Vérification que JAVA_HOME est configuré
if (-not (Test-Path $env:JAVA_HOME)) {
    Write-Host "❌ Erreur: JAVA_HOME n'existe pas à: $($env:JAVA_HOME)" -ForegroundColor Red
    exit 1
}

Write-Host "✅ JAVA_HOME configuré à: $($env:JAVA_HOME)" -ForegroundColor Green

# Change vers le répertoire du projet
$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectDir

Write-Host "📂 Répertoire du projet: $projectDir" -ForegroundColor Cyan
Write-Host "🚀 Lancement de CarthageGG..." -ForegroundColor Yellow

# Lance l'application Maven
& ".\mvnw.cmd" javafx:run

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Application lancée avec succès!" -ForegroundColor Green
} else {
    Write-Host "❌ Erreur lors du lancement de l'application" -ForegroundColor Red
    Read-Host "Appuyez sur Entrée pour fermer"
}

