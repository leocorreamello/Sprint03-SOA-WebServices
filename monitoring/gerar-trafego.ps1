$ErrorActionPreference = 'Stop'
$base = 'http://localhost:18080'

$analista = Invoke-RestMethod "$base/api/v1/auth/login" -Method Post -ContentType 'application/json' -Body '{"email":"analista@autointel.com","senha":"Analista@123"}'
$headers = @{ Authorization = "Bearer $($analista.token)" }

for ($i = 1; $i -le 30; $i++) {
    Invoke-WebRequest "$base/api/v1/veiculos" -Headers $headers -SkipHttpErrorCheck | Out-Null
    if ($i % 3 -eq 0) {
        Invoke-WebRequest "$base/api/v1/veiculos" -SkipHttpErrorCheck | Out-Null
    }
    if ($i % 10 -eq 0) {
        Invoke-WebRequest "$base/api/v1/veiculos" -Method Post -Headers $headers -ContentType 'application/json' -Body '{}' -SkipHttpErrorCheck | Out-Null
    }
    Start-Sleep -Milliseconds 800
}

$statuses = @(1..6 | ForEach-Object {
    (Invoke-WebRequest "$base/api/v1/auth/login" -Method Post -ContentType 'application/json' -Body '{"email":"naoexiste@exemplo.invalid","senha":"incorreta"}' -SkipHttpErrorCheck).StatusCode
})
Write-Output "Tráfego gerado. Falhas de login: $($statuses -join ', ')"
