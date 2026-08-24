#!/usr/bin/env bash
# =============================================================================
# provision.sh — Provisiona a VM do Projeto Barbearia de forma IDEMPOTENTE.
# -----------------------------------------------------------------------------
# Pode ser executado quantas vezes forem necessárias (vagrant up, vagrant
# provision, vagrant reload --provision) sem duplicar configuração, sem
# recriar recursos existentes e sem falhar.
#
# Variáveis de entrada (injetadas pelo Vagrantfile a partir de config.yml):
#   DB_NAME, DB_USER, DB_PASSWORD, DB_PORT, APP_PORT, APP_JAR, APP_HOME,
#   APP_USER, VM_NETWORK
# =============================================================================
set -euo pipefail

DB_NAME="${DB_NAME:-barbearia}"
DB_USER="${DB_USER:-postgres}"
DB_PASSWORD="${DB_PASSWORD:-postgres}"
DB_PORT="${DB_PORT:-5432}"
APP_PORT="${APP_PORT:-8080}"
APP_JAR="${APP_JAR:-barbearia-1.0.0.jar}"
APP_HOME="${APP_HOME:-/opt/barbearia}"
APP_USER="${APP_USER:-barbearia}"
VM_NETWORK="${VM_NETWORK:-192.168.56.0/24}"

PROJECT_SRC="/vagrant/app"      # synced folder com o código-fonte (pom.xml, src/...)
INFRA_DIR="/vagrant"            # pasta infra/ (onde este script e os templates vivem)
TEMPLATES_DIR="${INFRA_DIR}/templates"

log() { echo -e "\n\033[1;32m[provision]\033[0m $*"; }

# -----------------------------------------------------------------------------
# Helper idempotente: só instala o pacote se ele ainda não estiver presente
# -----------------------------------------------------------------------------
apt_install_if_missing() {
  local pkg="$1"
  if dpkg -s "$pkg" >/dev/null 2>&1; then
    log "Pacote '${pkg}' já instalado, pulando."
  else
    log "Instalando pacote '${pkg}'..."
    apt-get install -y "$pkg"
  fi
}

# -----------------------------------------------------------------------------
# 1) Pacotes base
# -----------------------------------------------------------------------------
log "Atualizando índice de pacotes (apt-get update)"
apt-get update -y

apt_install_if_missing curl
apt_install_if_missing ca-certificates
apt_install_if_missing gnupg
apt_install_if_missing gettext-base   # fornece o 'envsubst' usado para preencher os templates

UBUNTU_CODENAME="$(awk -F= '/^VERSION_CODENAME/{print $2}' /etc/os-release)"

# -----------------------------------------------------------------------------
# 2) Java 21 — repositório oficial Eclipse Temurin (garante JDK 21 real,
#    independente de qual versão o repo padrão do Ubuntu tiver disponível)
# -----------------------------------------------------------------------------
mkdir -p /etc/apt/keyrings

if [ ! -f /etc/apt/keyrings/adoptium.gpg ]; then
  log "Adicionando repositório Adoptium (Eclipse Temurin JDK)"
  curl -fsSL https://packages.adoptium.net/artifactory/api/gpg/key/public \
    | gpg --dearmor -o /etc/apt/keyrings/adoptium.gpg
else
  log "Repositório Adoptium já configurado, pulando."
fi

ADOPTIUM_LIST="/etc/apt/sources.list.d/adoptium.list"
if [ ! -f "$ADOPTIUM_LIST" ]; then
  echo "deb [signed-by=/etc/apt/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb ${UBUNTU_CODENAME} main" \
    > "$ADOPTIUM_LIST"
  apt-get update -y
fi

apt_install_if_missing temurin-21-jdk

# -----------------------------------------------------------------------------
# 3) Maven
# -----------------------------------------------------------------------------
apt_install_if_missing maven

# -----------------------------------------------------------------------------
# 4) PostgreSQL 16 — repositório oficial PGDG (Ubuntu 22.04 "jammy" traz
#    PostgreSQL 14 por padrão; o repo do postgresql.org garante a versão 16)
# -----------------------------------------------------------------------------
if [ ! -f /etc/apt/keyrings/postgresql.gpg ]; then
  log "Adicionando repositório oficial do PostgreSQL (PGDG)"
  curl -fsSL https://www.postgresql.org/media/keys/ACCC4CF8.asc \
    | gpg --dearmor -o /etc/apt/keyrings/postgresql.gpg
else
  log "Repositório PGDG já configurado, pulando."
fi

PGDG_LIST="/etc/apt/sources.list.d/pgdg.list"
if [ ! -f "$PGDG_LIST" ]; then
  echo "deb [signed-by=/etc/apt/keyrings/postgresql.gpg] http://apt.postgresql.org/pub/repos/apt ${UBUNTU_CODENAME}-pgdg main" \
    > "$PGDG_LIST"
  apt-get update -y
fi

apt_install_if_missing postgresql-16

log "Garantindo que o serviço PostgreSQL está habilitado e rodando"
systemctl enable postgresql >/dev/null 2>&1 || true
systemctl start postgresql

for i in $(seq 1 30); do
  if sudo -u postgres pg_isready >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

# Permite conexões dentro da rede privada da VM (útil para acesso externo
# de ferramentas de administração de banco, ex: DBeaver, a partir do host)
PG_CONF_DIR="$(sudo -u postgres psql -tAc "SHOW config_file" | xargs dirname)"
PG_CONF="${PG_CONF_DIR}/postgresql.conf"
PG_HBA="${PG_CONF_DIR}/pg_hba.conf"

if ! grep -q "^listen_addresses" "$PG_CONF" 2>/dev/null; then
  log "Habilitando listen_addresses='*' no PostgreSQL"
  echo "listen_addresses = '*'" >> "$PG_CONF"
fi

if ! grep -qF "$VM_NETWORK" "$PG_HBA" 2>/dev/null; then
  log "Autorizando conexões da rede privada (${VM_NETWORK}) no pg_hba.conf"
  echo "host    all             all             ${VM_NETWORK}         md5" >> "$PG_HBA"
fi

systemctl restart postgresql

# -----------------------------------------------------------------------------
# 5) Usuário e banco de dados da aplicação (idempotente: sempre checa antes
#    de criar; nunca falha se já existir)
# -----------------------------------------------------------------------------
log "Garantindo usuário de banco '${DB_USER}'"
USER_EXISTS="$(sudo -u postgres psql -tAc "SELECT 1 FROM pg_roles WHERE rolname='${DB_USER}'")"
if [ "$USER_EXISTS" = "1" ]; then
  log "Usuário '${DB_USER}' já existe — sincronizando senha com config.yml."
  sudo -u postgres psql -c "ALTER USER \"${DB_USER}\" WITH PASSWORD '${DB_PASSWORD}';" >/dev/null
else
  log "Criando usuário '${DB_USER}'."
  sudo -u postgres psql -c "CREATE USER \"${DB_USER}\" WITH PASSWORD '${DB_PASSWORD}';" >/dev/null
fi

log "Garantindo banco de dados '${DB_NAME}'"
DB_EXISTS="$(sudo -u postgres psql -tAc "SELECT 1 FROM pg_database WHERE datname='${DB_NAME}'")"
if [ "$DB_EXISTS" = "1" ]; then
  log "Banco '${DB_NAME}' já existe, pulando criação."
else
  sudo -u postgres psql -c "CREATE DATABASE \"${DB_NAME}\" OWNER \"${DB_USER}\";" >/dev/null
fi
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE \"${DB_NAME}\" TO \"${DB_USER}\";" >/dev/null
# PostgreSQL 15+ revoga CREATE no schema "public" do PUBLIC por padrão — garante
# explicitamente que o usuário da aplicação pode criar tabelas (Hibernate ddl-auto=update)
sudo -u postgres psql -d "${DB_NAME}" -c "GRANT ALL ON SCHEMA public TO \"${DB_USER}\";" >/dev/null

# -----------------------------------------------------------------------------
# 6) Usuário de sistema e diretórios da aplicação
# -----------------------------------------------------------------------------
if id -u "$APP_USER" >/dev/null 2>&1; then
  log "Usuário de sistema '${APP_USER}' já existe, pulando criação."
else
  log "Criando usuário de sistema '${APP_USER}'"
  useradd --system --home "${APP_HOME}" --create-home --shell /usr/sbin/nologin "${APP_USER}"
fi

mkdir -p "${APP_HOME}/config"
chown -R "${APP_USER}:${APP_USER}" "${APP_HOME}"

# -----------------------------------------------------------------------------
# 7) Build do Projeto Spring Boot (código já disponível via synced folder)
# -----------------------------------------------------------------------------
log "Compilando o projeto com Maven (mvn clean package -DskipTests)"
cd "$PROJECT_SRC"
mvn -q -DskipTests clean package

BUILT_JAR="$(find target -maxdepth 1 -name "*.jar" ! -name "*sources*" | head -n1)"
if [ -z "$BUILT_JAR" ]; then
  echo "ERRO: nenhum .jar encontrado em ${PROJECT_SRC}/target — build falhou." >&2
  exit 1
fi

cp "$BUILT_JAR" "${APP_HOME}/${APP_JAR}"
chown "${APP_USER}:${APP_USER}" "${APP_HOME}/${APP_JAR}"

# -----------------------------------------------------------------------------
# 8) Gera application.properties a partir do template (infra/templates/*.j2)
# -----------------------------------------------------------------------------
log "Gerando application.properties a partir do template"
export DB_NAME DB_USER DB_PASSWORD DB_PORT APP_PORT
envsubst < "${TEMPLATES_DIR}/application.properties.j2" \
  > "${APP_HOME}/config/application.properties"
chown "${APP_USER}:${APP_USER}" "${APP_HOME}/config/application.properties"
chmod 640 "${APP_HOME}/config/application.properties"

# -----------------------------------------------------------------------------
# 9) Serviço systemd a partir do template — cria/atualiza e reinicia
# -----------------------------------------------------------------------------
log "Configurando o serviço systemd 'barbearia'"
export APP_HOME APP_USER APP_JAR
envsubst < "${TEMPLATES_DIR}/barbearia.service.j2" \
  > /etc/systemd/system/barbearia.service

systemctl daemon-reload
systemctl enable barbearia >/dev/null 2>&1
systemctl restart barbearia

# -----------------------------------------------------------------------------
# 10) SSH externo por chave pública (opcional, sem senha) — só roda se o
#     Vagrantfile encontrou uma chave pública sua no host e a copiou.
# -----------------------------------------------------------------------------
if [ -f /tmp/host_external_key.pub ]; then
  log "Autorizando chave pública SSH do host para acesso externo (fora do 'vagrant ssh')"
  install -d -m 700 -o vagrant -g vagrant /home/vagrant/.ssh
  touch /home/vagrant/.ssh/authorized_keys
  if ! grep -qxFf /tmp/host_external_key.pub /home/vagrant/.ssh/authorized_keys 2>/dev/null; then
    cat /tmp/host_external_key.pub >> /home/vagrant/.ssh/authorized_keys
  fi
  chmod 600 /home/vagrant/.ssh/authorized_keys
  chown vagrant:vagrant /home/vagrant/.ssh/authorized_keys
  rm -f /tmp/host_external_key.pub
fi

# -----------------------------------------------------------------------------
# 11) Espera a aplicação responder antes de encerrar o provisionamento
# -----------------------------------------------------------------------------
log "Aguardando a aplicação subir em http://localhost:${APP_PORT} ..."
for i in $(seq 1 60); do
  if curl -fsS "http://localhost:${APP_PORT}" -o /dev/null 2>/dev/null; then
    log "Aplicação respondendo!"
    break
  fi
  sleep 2
done

log "Provisionamento concluído. Acesse: http://<IP-da-VM>:${APP_PORT}"
