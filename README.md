# Users API

[![CI](https://github.com/LuisMarangoni/users-api/actions/workflows/ci.yml/badge.svg)](https://github.com/LuisMarangoni/users-api/actions/workflows/ci.yml)

API REST para gerenciamento de usuários e perfis, desenvolvida com Java, Spring Boot e PostgreSQL.

O projeto demonstra cadastro e manutenção de usuários, validação de e-mail, desativação lógica e relacionamento muitos-para-muitos com perfis.

## Tecnologias

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Flyway
- Bean Validation
- OpenAPI e Swagger UI
- JUnit 5
- Mockito
- H2 para testes
- Maven

## Funcionalidades

- Cadastrar usuários
- Listar usuários
- Buscar usuário por ID
- Atualizar nome e e-mail
- Ativar e desativar usuários
- Impedir e-mails duplicados
- Normalizar e-mails para letras minúsculas
- Atribuir perfis aos usuários
- Validar dados de entrada
- Retornar erros padronizados com `ProblemDetail`

## Perfis

A aplicação possui três perfis:

```text
USUARIO
SUPORTE
ADMIN
```

Todo novo usuário recebe automaticamente o perfil `USUARIO`.

Um usuário pode possuir vários perfis, e cada perfil pode pertencer a vários usuários. Esse relacionamento é armazenado pela tabela intermediária `usuarios_perfis`.

Os perfis ainda não controlam autorização de endpoints. A autenticação e autorização com Spring Security e JWT fazem parte da evolução planejada.

## Estrutura

- `Controller`: recebe as requisições HTTP.
- `Service`: executa as regras da aplicação.
- `Repository`: acessa o banco por meio do Spring Data JPA.
- `Entity`: representa os dados persistidos.
- `Request DTOs`: definem e validam os dados recebidos.
- `Response DTOs`: controlam os dados devolvidos pela API.
- `Flyway`: versiona a estrutura e os dados iniciais do banco.
- `ProblemDetail`: padroniza as respostas de erro.

## Pré-requisitos

- Java 21
- PostgreSQL
- Git

Não é necessário instalar o Maven, pois o projeto possui Maven Wrapper.

## Banco de dados

Acesse o PostgreSQL com um usuário administrador e execute:

```sql
CREATE USER users_app WITH PASSWORD 'SUA_SENHA';
CREATE DATABASE users_api OWNER users_app;
```

Não coloque a senha real no código ou em arquivos versionados.

## Configuração

No PowerShell, configure a senha do banco:

```powershell
$senhaSegura = Read-Host "Senha de users_app" -AsSecureString
$ponte = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($senhaSegura)

try {
    $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ponte)
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ponte)
}
```

Valores utilizados por padrão:

```text
Banco: users_api
Usuário: users_app
Servidor: localhost
Porta do PostgreSQL: 5432
Porta da API: 8081
```

Também é possível sobrescrever:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
SERVER_PORT
```

## Executando a aplicação

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

A API ficará disponível em:

```text
http://localhost:8081
```

## Documentação

Com a aplicação em execução:

- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs

## Endpoints

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/usuarios` | Cadastra um usuário |
| `GET` | `/usuarios` | Lista os usuários |
| `GET` | `/usuarios/{id}` | Busca um usuário por ID |
| `PUT` | `/usuarios/{id}` | Atualiza nome e e-mail |
| `PATCH` | `/usuarios/{id}/ativo` | Ativa ou desativa um usuário |
| `PUT` | `/usuarios/{id}/perfis/{nomePerfil}` | Atribui um perfil ao usuário |

## Exemplo de cadastro

```http
POST /usuarios
Content-Type: application/json
```

```json
{
  "nome": "Luis Marangoni",
  "email": "luis@email.com"
}
```

Resposta:

```json
{
  "id": 1,
  "nome": "Luis Marangoni",
  "email": "luis@email.com",
  "ativo": true,
  "dataCriacao": "2026-09-09T10:00:00",
  "perfis": [
    "USUARIO"
  ]
}
```

## Códigos de resposta

| Código | Significado |
|---|---|
| `200 OK` | Consulta ou atualização concluída |
| `201 Created` | Usuário criado |
| `400 Bad Request` | Dados inválidos |
| `404 Not Found` | Usuário não encontrado |
| `409 Conflict` | E-mail já cadastrado |

## Testes

Execute:

```powershell
.\mvnw.cmd test
```

Os testes usam H2 em memória e não precisam da senha do PostgreSQL.

Os testes verificam:

- inicialização do contexto Spring;
- migrations do Flyway;
- normalização de nome e e-mail;
- atribuição do perfil padrão;
- bloqueio de e-mail duplicado;
- interrupção do fluxo antes da persistência em caso de duplicidade.

## Decisões técnicas

- O banco gera os IDs com `GenerationType.IDENTITY`.
- O e-mail possui restrição única no banco e verificação no Service.
- Entidades não são retornadas diretamente pela API.
- Usuários são desativados sem perder seu registro.
- O relacionamento entre usuários e perfis é muitos-para-muitos.
- O Flyway cria os perfis iniciais e migra usuários existentes.
- Operações de escrita usam transações.
- Os testes são isolados do PostgreSQL por meio do H2.