# ServeRest: testes de API com RestAssured

[![API tests](https://github.com/erkmnasc/ServeRest-RestAssured/actions/workflows/ci.yml/badge.svg)](https://github.com/erkmnasc/ServeRest-RestAssured/actions/workflows/ci.yml)

Testes da API REST da [ServeRest](https://serverest.dev) em Java, com
RestAssured, validação de contrato por JSON Schema, JUnit e cenários BDD em
Cucumber. Projeto pessoal de estudo.

## Stack

Java 17 · RestAssured 6 · JSON Schema Validator · JUnit 6 · Cucumber 8 ·
Datafaker · Maven · GitHub Actions

## O que é testado

**`POST /login`** (`LoginTest`)
- login válido com o contrato da resposta
- senha incorreta e email não cadastrado (401 com a mesma mensagem)
- validação dos campos (teste parametrizado): email inválido, sem email, sem
  senha, corpo vazio

**`/usuarios`** (`UsuariosTest`)
- cadastro conferido com GET por id e JSON Schema
- listagem filtrada por email
- alteração e exclusão, conferidas com nova consulta
- exclusão de id inexistente (200, "Nenhum registro excluído")
- email repetido, campos obrigatórios e valor inválido de `administrador`

**`/produtos`** (`ProdutosTest`)
- cadastro pelo administrador, conferido com GET por id
- sem token (401) e usuário comum (403)
- nome repetido, campos obrigatórios e preço inválido (negativo, zero,
  decimal e texto)
- exclusão conferida com nova consulta

**Cucumber** (`features/cadastro_usuario.feature`, em português)
- cadastro de administrador, email repetido e um Esquema do Cenário para cada
  campo obrigatório

Cada teste cria a própria massa pela API (Datafaker, com email e nome de
produto únicos) e remove o que criou no fim.

## Estrutura

```
src/test/java/br/com/erkmnasc/serverest/
  api/       testes JUnit por recurso (login, usuários, produtos)
  bdd/       runner e passos do Cucumber
  client/    chamadas HTTP de cada recurso, sem validação
  model/     records usados no corpo das requisições
  support/   configuração, geração de massa e preparação de estado
src/test/resources/
  features/  cenários em Gherkin
  schemas/   JSON Schemas das respostas
```

Os clients só fazem a requisição e devolvem a resposta. Status, mensagens e
contrato são conferidos nos testes, o que deixa os cenários de erro tão
simples de escrever quanto os de sucesso.

## Como rodar

Precisa de Java 17 e Maven, e de um ServeRest rodando. O jeito mais simples é
subir o ServeRest local na porta 3000:

```bash
npx serverest@3.5.0
# ou: docker run -p 3000:3000 paulogoncalvesbh/serverest:3.5.0
```

Em outro terminal:

```bash
mvn test
```

Para apontar para outro endereço, use `API_URL` ou `-Dapi.url`:

```bash
mvn test -Dapi.url=https://serverest.dev
```

O relatório do Cucumber fica em `target/cucumber-report.html`.

## CI

O GitHub Actions sobe o ServeRest 3.5.0 num service container, roda
`mvn test` contra ele e guarda o relatório do Cucumber e os relatórios do
Surefire como artefatos (`.github/workflows/ci.yml`). Também roda toda segunda-feira, para
avisar se algo mudar na API.
