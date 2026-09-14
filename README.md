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

A autenticação é feita por JWT. Cadastro, login e documentação Swagger são públicos. O perfil USUARIO pode autenticar, mas não acessa os endpoints de administração de usuários. SUPORTE e ADMIN podem consultar, atualizar e ativar ou desativar usuários. Apenas ADMIN pode atribuir perfis.


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

Execute os comandos na raiz do projeto. São obrigatórios `DB_PASSWORD` (senha do usuário PostgreSQL `users_app`) e `JWT_SECRET` (segredo usado para assinar os tokens). Nenhum deles é a senha de login de um usuário cadastrado na API.

### Opção 1: configuração local persistente

Crie um arquivo `.env` na raiz, ignorado pelo Git:

```properties
DB_PASSWORD=substitua_pela_senha_do_banco
JWT_SECRET=substitua_por_um_segredo_aleatorio
```

Substitua os exemplos antes de iniciar a aplicação. Esse arquivo é carregado como Java Properties por `spring.config.import=optional:file:.env[.properties]`: não use aspas delimitadoras como faria no PowerShell, pois elas passam a fazer parte do valor. Barras invertidas têm significado de escape nesse formato; uma barra literal deve ser escrita como `\\`.

Para gerar um segredo aleatório de 32 bytes, representado por 64 caracteres hexadecimais, execute localmente no PowerShell:

```powershell
$bytesJwt = New-Object byte[] 32
$geradorJwt = [System.Security.Cryptography.RandomNumberGenerator]::Create()
try {
    $geradorJwt.GetBytes($bytesJwt)
    [BitConverter]::ToString($bytesJwt).Replace('-', '')
} finally {
    $geradorJwt.Dispose()
}
```

Copie o resultado apenas para `JWT_SECRET` no arquivo local. Não envie esse valor em prints, mensagens, commits ou logs. Mantenha o mesmo segredo entre reinícios; trocá-lo invalida a assinatura dos tokens anteriores. A implementação usa os bytes UTF-8 do valor configurado como chave HS256, sem decodificação de hexadecimal ou Base64.

### Opção 2: variáveis na sessão do PowerShell

Como alternativa ao arquivo, configure a senha do banco:

```powershell
$senhaSegura = Read-Host "Senha de users_app" -AsSecureString
$ponte = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($senhaSegura)

try {
    $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ponte)
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ponte)
}
```

Para configurar o segredo JWT existente sem exibi-lo, no mesmo terminal:

```powershell
$segredoSeguro = Read-Host "JWT_SECRET" -AsSecureString
$ponteJwt = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($segredoSeguro)
try {
    $env:JWT_SECRET = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ponteJwt)
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ponteJwt)
}
```

Variáveis de ambiente têm precedência sobre o arquivo. Evite manter valores antigos na sessão ao editar o `.env`. As variáveis definidas com `$env:` valem para esse terminal e os processos iniciados por ele; não são uma configuração persistente.

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
JWT_SECRET
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

| Método  | Endpoint                             | Descrição                                                                                                 |
|---------|--------------------------------------|-----------------------------------------------------------------------------------------------------------|
| `POST`  | `/usuarios`                          | Cadastra um usuário                                                                                       |
| `GET`   | `/usuarios`                          | Lista usuários com paginação (`page`, `size`, `sort`) e filtros opcionais por `nome`, `email` e `ativo`   |
| `GET`   | `/usuarios/{id}`                     | Busca um usuário por ID                                                                                   |
| `PUT`   | `/usuarios/{id}`                     | Atualiza nome e e-mail                                                                                    |
| `PATCH` | `/usuarios/{id}/ativo`               | Ativa ou desativa um usuário                                                                              |
| `PUT`   | `/usuarios/{id}/perfis/{nomePerfil}` | Atribui um perfil ao usuário                                                                              |
| `POST`  | `/auth/login`                        | Autentica e obtém um token                                                                                |
| `GET`   | `/usuarios/me`                       | Retorna os dados do usuário autenticado pelo JWT                                                          |
| `PUT`   | `/usuarios/me`                       | Atualiza nome e e-mail do usuário autenticado                                                             |


## Exemplo de cadastro

```http
POST /usuarios
Content-Type: application/json
```

```json
{
  "nome": "Luis Marangoni",
  "email": "luis@email.com",
  "senha": "SenhaTeste123"
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


## Autenticação JWT

```http
POST /auth/login
Content-Type: application/json
```

```json
{
  "email": "luis@email.com",
  "senha": "SenhaTeste123"
}
```

Resposta:

```json
{
  "tipo": "Bearer",
  "token": "<token-jwt>"
}
```
As rotas protegidas recebem `Authorization: Bearer <token-jwt>`. Cadastro e login são públicos; as demais rotas de usuários exigem token e as permissões correspondentes.

No Swagger, execute o login e cole apenas o valor de `token` em **Authorize**, sem acrescentar `Bearer`. Para validar o acesso da própria conta, execute `GET /usuarios/me`.

Os exemplos de e-mail e senha deste README não criam contas automaticamente. Cadastre a conta com `POST /usuarios` antes de fazer login. Usuários gerados nos testes H2 não existem automaticamente no PostgreSQL local.

### Configuração inicial de ADMIN ou SUPORTE

As migrations criam os perfis, mas não criam uma conta administradora. Todo cadastro público recebe apenas `USUARIO`. Para preparar o primeiro acesso privilegiado no ambiente local:

1. Com PostgreSQL e API rodando, cadastre a conta desejada em `POST /usuarios` e anote o ID retornado.
2. Conecte-se ao banco **`users_api`**, usando `users_app` e sua senha de banco. Não utilize o banco `sistema_chamados`.
3. Confira a conta pelo e-mail e confirme seu ID e estado ativo:

```sql
SELECT id, email, ativo
FROM usuarios
WHERE email = 'substitua_pelo_email_cadastrado';
```

4. Para criar o primeiro administrador, abra uma transação e substitua **o ID ilustrativo `123` e o e-mail** pelos valores conferidos:

```sql
BEGIN;

INSERT INTO usuarios_perfis (usuario_id, perfil_id)
SELECT u.id, p.id
FROM usuarios u
CROSS JOIN perfis p
WHERE u.id = 123
  AND u.email = 'substitua_pelo_email_cadastrado'
  AND u.ativo = true
  AND p.nome = 'ADMIN'
ON CONFLICT DO NOTHING;

SELECT u.id, u.email, p.nome AS perfil
FROM usuarios u
JOIN usuarios_perfis up ON up.usuario_id = u.id
JOIN perfis p ON p.id = up.perfil_id
WHERE u.id = 123
ORDER BY p.nome;
```

5. Se a conta e o perfil estiverem corretos, execute `COMMIT;`. Se estiverem incorretos ou ocorrer algum erro, execute `ROLLBACK;` e confira os dados antes de tentar novamente. `INSERT 0 0` significa que nenhuma associação foi adicionada: verifique se o perfil já existia ou se o ID/e-mail não correspondeu.
6. Faça novo login na API para obter um token com o perfil atualizado. O token anterior não ganha permissões automaticamente.

Para preparar apenas a conta de integração com o Sistema de Chamados, use `SUPORTE` em vez de `ADMIN` no procedimento inicial. Esse perfil é suficiente para consultar solicitantes, mas também permite atualizar e ativar/desativar usuários; não é um perfil somente de leitura. Evite conceder `ADMIN` à integração.

Depois de existir um administrador, use seu token em `PUT /usuarios/{id}/perfis/{nomePerfil}` para atribuir perfis pela API. O procedimento SQL é uma configuração administrativa inicial do ambiente local, não um endpoint público nem uma migration com credenciais fixas.

Na integração, `USERS_API_EMAIL` e `USERS_API_PASSWORD` são configurados no **Sistema de Chamados** e correspondem ao login dessa conta, não a `DB_PASSWORD` ou `JWT_SECRET`.


## Códigos de resposta

| Código | Significado                                      |
|---|--------------------------------------------------|
| `200 OK` | Consulta ou atualização concluída                |
| `201 Created` | Usuário criado                                   |
| `400 Bad Request` | Dados inválidos                                  |
| `404 Not Found` | Usuário não encontrado                           |
| `409 Conflict` | E-mail já cadastrado                             |
| `401 Unauthorized` | Token ausente/inválido ou credenciais incorretas |
| `403 Forbidden` | Usuário autenticado sem o perfil necessário |

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
- testes de login e acesso a rota protegida com e sem token.

## Decisões técnicas

- O banco gera os IDs com `GenerationType.IDENTITY`.
- O e-mail possui restrição única no banco e verificação no Service.
- Entidades não são retornadas diretamente pela API.
- Usuários são desativados sem perder seu registro.
- O relacionamento entre usuários e perfis é muitos-para-muitos.
- O Flyway cria os perfis iniciais e migra usuários existentes.
- Operações de escrita usam transações.
- Os testes são isolados do PostgreSQL por meio do H2.

## Escopo e limitações desta versão

- Projeto de portfólio para execução e demonstração local; não deve ser exposto publicamente como está.
- Os tokens expiram em uma hora. Não há refresh token nem revogação individual imediata; desativar uma conta impede novos logins, mas um JWT já emitido pode continuar autorizando requisições até expirar.
- A autenticação JWT desta API não protege automaticamente os endpoints do Sistema de Chamados, que nesta versão não exigem autenticação própria.
- Não há fluxo público de recuperação de senha ou limitação de tentativas de login. Uma publicação real exige revisar esses controles, além de HTTPS, gestão de segredos e acesso de rede.
- Arquivos locais de diagnóstico podem conter dados sensíveis. Não inclua `.env`, tokens ou arquivos `erro-*.txt` em commits.
