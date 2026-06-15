$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Join-Path $root 'tool-web-backend'
$frontendDir = Join-Path $root 'tool-web-frontend'

function Test-Command {
    param([string]$Name)
    return $null -ne (Get-Command $Name -ErrorAction SilentlyContinue)
}

function Start-ToolWindow {
    param(
        [string]$Title,
        [string]$WorkingDirectory,
        [string]$Command
    )

    $fullCommand = "$host.UI.RawUI.WindowTitle = '$Title'`r`n$Command"
    $encoded = [Convert]::ToBase64String([Text.Encoding]::Unicode.GetBytes($fullCommand))
    Start-Process powershell -ArgumentList @(
        '-NoExit',
        '-EncodedCommand',
        $encoded
    ) -WorkingDirectory $WorkingDirectory -WindowStyle Normal
}

if (-not (Test-Path $backendDir)) {
    throw "Backend directory not found: $backendDir"
}

if (-not (Test-Path $frontendDir)) {
    throw "Frontend directory not found: $frontendDir"
}

if (-not (Test-Command 'java')) {
    throw 'Java is not installed or not available in PATH.'
}

if (-not (Test-Command 'mvn')) {
    throw 'Maven is not installed or not available in PATH.'
}

if (-not (Test-Command 'node')) {
    throw 'Node.js is not installed or not available in PATH.'
}

if (-not (Test-Command 'npm')) {
    throw 'npm is not installed or not available in PATH.'
}

$backendCommand = @"
Set-Location '$backendDir'
Write-Host 'Starting backend on http://localhost:8080' -ForegroundColor Cyan
mvn spring-boot:run
"@

$frontendCommand = @"
Set-Location '$frontendDir'
Write-Host 'Starting frontend on http://localhost:3000' -ForegroundColor Cyan
npm run dev
"@

Start-ToolWindow -Title 'ToolWeb Backend' -WorkingDirectory $backendDir -Command $backendCommand
Start-Sleep -Seconds 2
Start-ToolWindow -Title 'ToolWeb Frontend' -WorkingDirectory $frontendDir -Command $frontendCommand

Write-Host 'Backend window started: http://localhost:8080' -ForegroundColor Green
Write-Host 'Frontend window started: http://localhost:3000' -ForegroundColor Green
Write-Host 'Keep the two new PowerShell windows open while using the project.' -ForegroundColor Yellow
