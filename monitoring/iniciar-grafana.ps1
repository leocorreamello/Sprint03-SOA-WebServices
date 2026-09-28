$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot

if (-not (Test-Path -LiteralPath '.env')) {
    $jwt = [Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
    $grafana = [Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
    @("JWT_SECRET=$jwt", "GRAFANA_ADMIN_PASSWORD=$grafana") | Set-Content -LiteralPath '.env' -Encoding utf8
}

docker compose up -d --build
if ($LASTEXITCODE -ne 0) { throw 'Falha ao iniciar a stack de monitoramento.' }
Write-Output 'Grafana: http://localhost:13000/d/autointel-security/autointel-seguranca-api'
Write-Output 'Prometheus: http://localhost:19090'
Write-Output 'API de demonstração: http://localhost:18080'
