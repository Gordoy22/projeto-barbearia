<#
.SYNOPSIS
    Sobe a infraestrutura do Projeto Barbearia do ZERO: destroi qualquer VM
    existente, recria com Vagrant e provisiona tudo (Java, Maven, PostgreSQL,
    build da aplicação, systemd) via Ansible rodando dentro da própria VM.

.DESCRIPTION
    Equivale a rodar "vagrant destroy -f" seguido de "vagrant up" na pasta
    infra/, mas com verificação de pré-requisitos e saída mais amigável.
    Só precisa de VirtualBox + Vagrant instalados no Windows — o Ansible é
    instalado e executado dentro da VM (ansible_local), nada precisa ser
    instalado no host além disso.

.EXAMPLE
    .\run.ps1
#>
[CmdletBinding()]
param()

# Propositalmente NÃO usamos $ErrorActionPreference = "Stop": no PowerShell
# 5.1, qualquer linha que o vagrant.exe escreva em stderr (inclusive avisos
# normais, não só erros de verdade) é convertida em erro terminante e mata o
# script na hora, escondendo a mensagem real. Preferimos checar $LASTEXITCODE
# manualmente depois de cada comando, como já fazíamos abaixo.
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

Write-Host "==> Destruindo qualquer VM existente (recomeco do zero)..." -ForegroundColor Cyan
vagrant destroy -f
if ($LASTEXITCODE -ne 0) {
    Write-Error "'vagrant destroy' falhou (codigo $LASTEXITCODE) - veja o log acima."
    exit $LASTEXITCODE
}

Write-Host "==> Subindo a VM e provisionando com Ansible (pode levar varios minutos na primeira vez)..." -ForegroundColor Cyan
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
Write-Host "    vagrant provision          -> reprovisionar (rodar o Ansible de novo) sem recriar a VM"
Write-Host "    vagrant halt               -> desligar a VM"
Write-Host "    vagrant destroy -f         -> destruir a VM"
