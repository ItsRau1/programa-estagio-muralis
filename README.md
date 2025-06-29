# Minhas Finanças

Sistema de gerenciamento de finanças pessoais desenvolvido com Spring Boot e Java.

## Descrição

O projeto Minhas Finanças é uma aplicação web que permite aos usuários:

- Gerenciar suas finanças pessoais
- Registrar receitas e despesas
- Visualizar relatórios financeiros
- Gerenciar categorias de gastos
- Realizar autenticação segura via JWT

## Tecnologias Utilizadas

- Spring Boot 2.2.2
- Java 8
- Spring Security
- Spring Data JPA
- PostgreSQL
- H2 Database (para testes)
- Lombok
- JWT (JSON Web Token)
- Spring Boot Actuator
- Spring Boot DevTools

## Configuração do Ambiente

### Pré-requisitos

- Java 8 ou superior
- Maven 3.6.0 ou superior
- PostgreSQL (opcional - H2 Database pode ser usado para desenvolvimento)

### Instalação

1. Siga as instruções de [contribuição](#contribuição) para configurar o ambiente de desenvolvimento.

2. Configure as variáveis de ambiente:

   - `SPRING_DATASOURCE_URL`: URL do banco de dados PostgreSQL;
   - `SPRING_DATASOURCE_USERNAME`: Usuário do banco de dados;
   - `SPRING_DATASOURCE_PASSWORD`: Senha do banco de dados;
   - `SPRING_JPA_HIBERNATE_DDL_AUTO`: Estratégia de inicialização do banco de dados.

3. Execute a aplicação:

   ```bash
   mvn spring-boot:run
   ```

## Estrutura do Projeto

```bash
src/main/java/com/dsousa/minhasfinancas/
├── api/ # Controllers REST
├── config/ # Configurações do Spring Boot
├── exception/ # Classes de exceção personalizadas
├── model/ # Classes de domínio
└── service/ # Classes de serviço
```

## Banco de Dados

O projeto suporta dois bancos de dados:

- PostgreSQL (produção)
- H2 Database (desenvolvimento/testes)

## Segurança

O projeto utiliza uma camada de segurança com as seguintes características:

1. **Autenticação JWT**

   - Token JWT gerado após autenticação bem-sucedida
   - Validação de tokens em cada requisição
   - Token expira após 30 dias (configurável)
   - Informações do token: email, ID do usuário, nome e hora de expiração

2. **Validação de Tokens**
   - Verificação da assinatura do token
   - Verificação da data de expiração
   - Validação do usuário associado ao token

## Testes

O projeto utiliza Spring Boot Starter Test para testes unitários e de integração. A execução dos testes acontece ao executar o comando para criar o executável do projeto com o `mvn clean install`, mas para executar somente os testes use o comando:

```bash
mvn test
```

### Tipos de Testes Implementados

1. **Testes de Serviço**
2. **Testes de Repositório**
3. **Testes de API**

### Configuração dos Testes

- Banco de dados: H2 Database (em memória)
- Spring Boot Test: suporte para testes de componentes Spring
- Spring Security Test: suporte para testes de segurança
- Testes baseados em JUnit

## API

A API REST do projeto oferece endpoints para gerenciar usuários e lançamentos financeiros.

### Recursos de Usuários (`/api/usuarios`)

1. **Autenticação**

   - `POST /autenticar`: Autenticação de usuário
     - Parâmetros: email, senha
     - Retorna: Token JWT e nome do usuário

2. **Cadastro de Usuário**

   - `POST /`: Criação de novo usuário
     - Parâmetros: nome, email, senha
     - Retorna: Usuário criado

3. **Consulta de Saldo**
   - `GET /{id}/saldo`: Obtém saldo do usuário
     - Parâmetros: ID do usuário
     - Retorna: Valor do saldo

### Recursos de Lançamentos (`/api/lancamentos`)

1. **Listagem de Lançamentos**

   - `GET /`: Busca lançamentos
     - Parâmetros: descricao (opcional), mes (opcional), ano (opcional), usuario
     - Retorna: Lista de lançamentos filtrados

2. **Detalhes do Lançamento**

   - `GET /{id}`: Obtém um lançamento específico
     - Parâmetros: ID do lançamento
     - Retorna: Detalhes do lançamento

3. **Criação de Lançamento**

   - `POST /`: Cria novo lançamento
     - Parâmetros: descricao, valor, mes, ano, tipo, status, usuario
     - Retorna: Lançamento criado

4. **Atualização de Lançamento**

   - `PUT /{id}`: Atualiza um lançamento
     - Parâmetros: ID do lançamento, campos a serem atualizados
     - Retorna: Lançamento atualizado

5. **Atualização de Status**

   - `PUT /{id}/atualiza-status`: Atualiza status do lançamento
     - Parâmetros: ID do lançamento, novo status
     - Retorna: Lançamento com status atualizado

6. **Exclusão de Lançamento**
   - `DELETE /{id}`: Remove um lançamento
     - Parâmetros: ID do lançamento
     - Retorna: Status 204 (No Content) se bem-sucedido

## Docker

O projeto utiliza Docker para facilitar o desenvolvimento e deploy. O arquivo docker-compose.yml configura os serviços necessários.

### Como Usar o Docker

```bash
# Iniciar o container
docker-compose up

# Parar o container
docker-compose down
```

## Arquivos Importantes

- [.gitignore](cci:7://file:///c:/Users/User/Documents/Programa%C3%A7%C3%A3o/Muralis%20-%20Projeto%20Programa%20de%20Estagio/programa-estagio-muralis/.gitignore:0:0-0:0): Configura arquivos e diretórios ignorados pelo Git
- [pom.xml](cci:7://file:///c:/Users/User/Documents/Programa%C3%A7%C3%A3o/Muralis%20-%20Projeto%20Programa%20de%20Estagio/programa-estagio-muralis/pom.xml:0:0-0:0): Configuração do Maven e dependências
- [docker-compose.yml](cci:7://file:///c:/Users/User/Documents/Programa%C3%A7%C3%A3o/Muralis%20-%20Projeto%20Programa%20de%20Estagio/programa-estagio-muralis/docker-compose.yml:0:0-0:0): Configuração dos serviços Docker

## Contribuição

1. **Fork do Projeto**

   - Faça um fork do projeto no GitHub
   - Clone o seu fork localmente

2. **Criação de Branch**

   - Crie uma branch para sua feature com base na branch main:

   ```bash
   git checkout -b feature/AmazingFeature
   ```

3. **Desenvolvimento**

   - Faça suas alterações;
   - Siga as convenções de código do projeto;
   - Adicione testes para novas funcionalidades;
   - Mantenha os testes já implementados anteriormente funcionando.

4. **Commit e Push**

   - Commit suas mudanças:

   ```bash
   git commit -m 'Feat: alguma funcionalidade incrível'
   ```

   - Push para a branch:

   ```bash
   git push origin feature/AmazingFeature
   ```

5. **Pull Request**
   - Abra um Pull Request no GitHub
   - Descreva as mudanças realizadas
   - Aguarde revisão e feedback
