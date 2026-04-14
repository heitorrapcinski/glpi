# ─────────────────────────────────────────────────────────────────────────────
# GLPI API Gateway — Login and List Users (curl)
#
# Usage:
#   .\scripts\test-api.ps1
#   .\scripts\test-api.ps1 -BaseUrl http://localhost:8080
#   .\scripts\test-api.ps1 -Username tech -Password tech
# ─────────────────────────────────────────────────────────────────────────────

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Username = "glpi",
    [string]$Password = "glpi"
)

$ErrorActionPreference = "Stop"

Write-Host "`n=== GLPI API Test ===" -ForegroundColor Cyan
Write-Host "Gateway: $BaseUrl"
Write-Host "User:    $Username`n"

# ─── Login ────────────────────────────────────────────────────────────────
Write-Host "[1/3] Authenticating..." -ForegroundColor Yellow

$loginJson = "{`"username`":`"$Username`",`"password`":`"$Password`"}"

$loginResponse = curl -s -X POST "$BaseUrl/auth/login" `
    -H "Content-Type: application/json" `
    -d $loginJson

$token = ($loginResponse | ConvertFrom-Json).accessToken

if (-not $token) {
    Write-Host "  FAILED — response: $loginResponse" -ForegroundColor Red
    exit 1
}

Write-Host "  OK — token: $($token.Substring(0, 20))..." -ForegroundColor Green

# ─── List Users (external) ───────────────────────────────────────────────
Write-Host "[2/3] Listing users (external — host to gateway)..." -ForegroundColor Yellow
Write-Host "  curl command:`n" -ForegroundColor DarkGray
Write-Host "  curl -s -X GET `"$BaseUrl/users`" -H `"Authorization: Bearer $token`"`n"

$usersResponse = curl -s -X GET "$BaseUrl/users" `
    -H "Authorization: Bearer $token"

Write-Host "  Response:`n" -ForegroundColor Green
$usersResponse | ConvertFrom-Json | ConvertTo-Json -Depth 5 | Write-Host

# ─── List Users (internal) ───────────────────────────────────────────────
Write-Host "`n[3/3] Listing users (internal — inside gateway container)..." -ForegroundColor Yellow

$distro = docker compose exec api-gateway sh -c "cat /etc/os-release 2>/dev/null | head -2"
Write-Host "  Container OS: $distro" -ForegroundColor DarkGray

Write-Host "  curl command:`n" -ForegroundColor DarkGray
Write-Host "  docker compose exec api-gateway sh -c `"wget -qO- --header='Authorization: Bearer $token' http://localhost:8080/users`"`n"

$internalResponse = docker compose exec api-gateway sh -c "wget -qO- --header='Authorization: Bearer $token' http://localhost:8080/users"

Write-Host "  Response:`n" -ForegroundColor Green
$internalResponse | ConvertFrom-Json | ConvertTo-Json -Depth 5 | Write-Host

Write-Host "`n=== Done ===" -ForegroundColor Cyan
