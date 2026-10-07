# API de Portfólio de Projetos

API REST para gerenciar o portfólio de projetos de uma empresa: ciclo de vida
(da análise à finalização), equipe, orçamento e risco.

## Tecnologias

Java 17 · Spring Boot 4.1.1 · Spring Data JPA/Hibernate · PostgreSQL · Flyway ·
Spring Security · springdoc-openapi (Swagger) · JUnit 5 · Mockito · AssertJ · JaCoCo

## Como executar

**Pré-requisitos:** JDK 17 e PostgreSQL.

1. Crie o banco: `CREATE DATABASE spring_boot_api;`
2. Ajuste `src/main/resources/application.properties` (URL, usuário e senha do banco).
3. Suba a aplicação: `./mvnw spring-boot:run`
4. As migrations do Flyway criam as tabelas automaticamente.

- Swagger UI: http://localhost:8080/swagger-ui.html
- Credenciais (HTTP Basic): usuário `admin`, senha `admin123`
  (sobrescreva com as variáveis `APP_USER` e `APP_PASSWORD`).

## Arquitetura

MVC em camadas: `controller` → `service` → `repository`, com `model` (entidades e enums),
`dto` (entrada e saída), `mapper`, `client` (integração externa), `mock` (API externa
simulada), `config` e `exception`. O Flyway versiona o schema; o Hibernate valida
(`ddl-auto=validate`) e persiste os dados.

## Principais endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST/GET/PUT/DELETE | `/projetos`, `/projetos/{id}` | CRUD de projetos |
| GET | `/projetos` | Listagem paginada com filtros (nome, status, gerenteId, orcamentoMin, orcamentoMax) |
| PATCH | `/projetos/{id}/status` | Transição de status |
| POST/DELETE | `/projetos/{id}/membros[/{membroId}]` | Alocar e remover membros |
| POST/GET | `/membros`, `/membros/{id}` | Cadastro (via API externa) e consulta |
| GET | `/relatorios/portfolio` | Relatório resumido do portfólio |

## Decisões de negócio

O enunciado deixa alguns pontos em aberto. As decisões tomadas:

**Risco** (calculado a cada leitura, não persistido)
- Vale o pior critério: orçamento acima de R$ 500.000 **ou** prazo acima de 6 meses → alto;
  senão, orçamento acima de R$ 100.000 **ou** prazo acima de 3 meses → médio; senão, baixo.
- O prazo é a distância entre o início e a previsão de término, comparada em meses
  de calendário (exatamente 3 meses ainda é baixo; 3 meses e 1 dia é médio).

**Status**
- Sequência fixa, sem pular etapas; `CANCELADO` pode ser aplicado a qualquer status ativo.
- `ENCERRADO` e `CANCELADO` são estados finais.
- Só o `PATCH /projetos/{id}/status` altera o status; o `PUT` atualiza apenas os dados.
- Ao encerrar sem data real de término informada, o sistema usa a data de hoje.
- Projetos `INICIADO`, `EM_ANDAMENTO` ou `ENCERRADO` não podem ser excluídos.
- Um projeto não pode ser iniciado sem ao menos 1 membro alocado.

**Membros e alocação**
- Membros não são cadastrados diretamente: o `POST /membros` os cria na API externa
  (mockada) e guarda uma cópia local para os relacionamentos. A API externa é a fonte da verdade.
- Só membros com atribuição "funcionário" (sem diferenciar acento ou maiúsculas) podem ser
  alocados; a atribuição é conferida na API externa.
- Mínimo de 1 e máximo de 10 membros por projeto; um membro pode estar em até 3 projetos ativos
  (status diferente de encerrado/cancelado).
- O gerente é independente da equipe: não precisa estar nela, não conta entre os 10 e não conta
  no limite de 3 projetos. Qualquer membro pode ser gerente.
- A equipe de projetos encerrados ou cancelados não pode ser alterada.

**Relatório**
- Quantidade e total orçado por status (todos os status aparecem, com zero quando vazios).
- Média de duração, em dias, dos projetos encerrados com data real de término
  (`null` quando não há nenhum).
- Membros únicos alocados: quem está na equipe de qualquer projeto.

**Listagem**
- Não há filtro por risco, pois ele é calculado e não existe como coluna.

## Segurança

HTTP Basic com usuário em memória, sem sessão (stateless); por isso o CSRF está desabilitado.
A rota `/api-externa/**` (mock) é pública, pois simula um sistema externo.

## Testes

`./mvnw test` executa os testes e gera o relatório JaCoCo em `target/site/jacoco/index.html`.
Cobertura das regras de negócio (`service`, `model`, `mapper`): 82%.
Os testes de repository usam o PostgreSQL configurado.

## Teste manual da API

Após iniciar a aplicação, a API pode ser testada pelo Swagger UI:

http://localhost:8080/swagger-ui.html

A autenticação utiliza HTTP Basic:

- Usuário: `admin`
- Senha: `admin123`

Antes de testar os endpoints que dependem de membros, consulte os membros disponíveis:

`GET /membros?page=0&size=10`

Utilize os IDs retornados nessa consulta nos próximos endpoints. Não assuma IDs fixos, pois os dados do mock externo são mantidos em memória.

## Limitações conhecidas

- Os dados do mock ficam em memória: os ids reiniciam a cada execução, podendo conflitar
  com cópias locais antigas (`id_externo`).
- Alocações simultâneas do mesmo membro podem ultrapassar o limite de 3 projetos.
- Falhas da API externa resultam em erro 500 genérico.
