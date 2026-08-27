<#
.SYNOPSIS
    Sobe a infraestrutura do Projeto Barbearia SEM apagar nada existente.

.DESCRIPTION
    Equivale a rodar "vagrant up" direto: se a VM já existe, só liga ela
    (rápido, não reinstala nada). Se a VM ainda não existe, cria e
    provisiona do zero (mesmo efeito do primeiro "vagrant up").

    Use este script no dia a dia / na apresentação, quando você só quer
    ligar o que já está pronto. Para recomeçar do absoluto zero (apagando a
    VM existente e reinstalando tudo), use ".\run.ps1" em vez deste.

.EXAMPLE
    .\start.ps1
#>
[CmdletBinding()]
param()

# Propositalmente NÃO usamos $ErrorActionPreference = "Stop": no PowerShell
# 5.1, qualquer linha que o vagrant.exe escreva em stderr (inclusive avisos
# normais, não só erros de verdade) é convertida em erro terminante e mata o
# script na hora, escondendo a mensagem real. Preferimos checar $LASTEXITCODE
# manualmente depois de cada comando.
$ErrorActionPreference = "Continue"

# Garante que rodamos a partir da pasta onde este script está (infra/),
# não importa de onde foi chamado.
Set-Location -Path $PSScriptRoot

function Test-CommandExists {
    param([string]$Name)
    return [bool](Get-Command $Name -ErrorAction SilentlyContinue)
}

Write-Host "==> Verificando pre-requisitos..." -ForegroundColor Cyan

if (-not (Test-CommandExists "vagrant")) {
    Write-Error "Vagrant nao encontrado no PATH. Instale em https://developer.hashicorp.com/vagrant/downloads e abra um novo terminal."
    exit 1
}

if (-not (Test-CommandExists "VBoxManage")) {
    Write-Warning "VBoxManage nao encontrado no PATH. Confirme se o VirtualBox esta instalado corretamente (https://www.virtualbox.org/wiki/Downloads)."
}

Write-Host "==> Ligando a VM (cria do zero se ainda nao existir; so liga se ja existir)..." -ForegroundColor Cyan
vagrant up
if ($LASTEXITCODE -ne 0) {
    Write-Error "'vagrant up' falhou (codigo $LASTEXITCODE) - veja o log acima. Dicas: infra\README.md, secao 'Solucao de problemas'."
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "==> Pronto! Aplicacao disponivel em:" -ForegroundColor Green
Write-Host "    http://192.168.56.10:8080"
Write-Host "    http://localhost:8080"
Write-Host ""
Write-Host "Outros comandos uteis:" -ForegroundColor DarkGray
Write-Host "    vagrant ssh                -> entrar na VM"
Write-Host "    vagrant halt               -> desligar a VM (mantem tudo, liga rapido depois)"
Write-Host "    .\run.ps1                  -> apagar tudo e recomecar do zero"
