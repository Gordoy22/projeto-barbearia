# BARBER — Sistema de Gestão para Barbearia

Sistema web completo para gerenciamento de barbearia, com cadastros, agendamentos, controle de conflitos de horário e agenda visual.

## Tecnologias

- Java 21 ou superior
- Spring Boot 3.5
- Spring MVC
- Spring Data JPA / Hibernate
- Spring Security
- Thymeleaf
- PostgreSQL
- Maven
- Bootstrap 5 e Bootstrap Icons
- FullCalendar
- HTML5, CSS3 e JavaScript

## Requisitos

- JDK 21+
- Maven 3.9+
- PostgreSQL 16+ (porta 5432)

## Infraestrutura (VM)

Para subir o backend + PostgreSQL automaticamente em uma VM Vagrant/VirtualBox
(sem instalar nada além de Vagrant e VirtualBox no seu PC), veja
[`infra/README.md`](infra/README.md).

## Configuração do PostgreSQL

Crie o banco de dados:

```sql
CREATE DATABASE barbearia;
```

Credenciais padrão em `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/barbearia
spring.datasource.username=postgres
spring.datasource.password=postgres
```

Se a senha do seu PostgreSQL for diferente, altere o arquivo acima.

Durante o desenvolvimento o Hibernate atualiza o esquema automaticamente:

```properties
spring.jpa.hibernate.ddl-auto=update
```

## Execução

Na pasta do projeto:

```bash
mvn clean install
```

```bash
mvn spring-boot:run
```

Se o Maven global não estiver disponível neste terminal, use o wrapper:

```bash
mvnw.cmd spring-boot:run
```

O Maven também foi instalado em `%USERPROFILE%\tools\apache-maven-3.9.11` e adicionado ao PATH do usuário.

Acesse [http://localhost:8080](http://localhost:8080). O sistema redireciona para `/login`.

## Usuário inicial

Os dados de demonstração são criados somente se o banco estiver vazio.

| Campo | Valor |
| --- | --- |
| E-mail | `admin@barbearia.com` |
| Senha | `admin123` |

Também são cadastrados os profissionais Carlos Silva e Marcos Souza, além dos serviços Corte Masculino, Barba e Corte + Barba.

## Funcionalidades

- Login com Spring Security e senha em BCrypt
- Dashboard com resumo do dia
- CRUD de clientes, funcionários e serviços (ativação/desativação lógica)
- Agendamentos com cálculo automático do horário final
- Impedimento de conflito de horário para o mesmo profissional
- Agenda em calendário (dia, semana e mês)
- Layout responsivo com menu lateral recolhível e offcanvas no celular

## Estrutura de diretórios

```text
src/main/java/br/com/barbearia
  config/
  controller/
  dto/
  entity/
  enums/
  exception/
  repository/
  service/
src/main/resources
  templates/
  static/
  application.properties
```
