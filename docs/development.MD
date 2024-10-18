# Envs necessárias

Definir as envs no sistema ou configurar na própria IDE.

- AWS_IAM_ACCESS_KEY
- AWS_IAM_SECRET_KEY
- FINAX_DATABASE
- FINAX_DATABASE_USERNAME
- FINAX_DATABASE_PASSWORD
- HUNTER_IO_API_KEY

# Database

É necessário instalar o PostgreSQL v16.4 e criar um novo database.  
De preferência fazer a instação em um WSL com ubuntu 24 (se estiver usando windows).

# Java e maven

Para rodar o projeto é necessário ter o Java e o maven instalados nas versões:

- Java 22
- Maven 3.9.9

# Git

Trabalhar sempre com base na branch `develop` e criar uma nova branch a partir dela para desenvolver uma nova feature.  
Ao terminar a feature abrir um pull request para a branch `develop` e solicitar a revisão de outro membro do time.

# Patterns

- Injeção de dependência usando `@RequiredArgsConstructor` do lombok sempre que possível
- Criar DTOs utilizando `records` do Java
- Funções e variáveis privadas sempre que possível
- Utilizar `final` sempre que possível
- Utilizar `Optional` ao invés de null
 
