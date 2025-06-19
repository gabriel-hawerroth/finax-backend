# Finax Backend

![Java](https://img.shields.io/badge/Java-24-blue?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0-brightgreen?logo=springboot)
![Build](https://img.shields.io/badge/build-passing-brightgreen)

Finax é um sistema de controle financeiro que auxilia no gerenciamento de receitas, despesas e investimentos.

> **Este repositório refere-se apenas ao backend do projeto.**

## 🚀 Tecnologias Utilizadas

- Java 24
- Spring Boot 3
- JPA/Hibernate
- PostgreSQL
- Flyway (migração de banco)
- Maven

## 📦 Como rodar o projeto localmente

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/seu-usuario/finax-backend.git
   cd finax-backend
   ```
2. **Configure o banco de dados:**
   - Crie um banco PostgreSQL local
   - Atualize as variáveis de ambiente conforme descrito no arquivo `docs/development.MD`
3. **Inicie a aplicação:**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

## 🧪 Testes

Para rodar os testes unitários:

```bash
./mvnw test
```

## 📁 Estrutura do Projeto

```
src/
  main/
    java/br/finax/           # Código-fonte principal
    resources/               # Configurações e recursos
  test/
    java/br/finax/           # Testes unitários
```

## 🌐 Documentação da API

Em desenvolvimento. Endpoints RESTful documentados via Swagger/OpenAPI (quando disponível).

## 👤 Autor

- Gabriel ([@gabriel-hawerroth](https://github.com/gabriel-hawerroth))

## 📄 Licença

Este projeto é de propriedade exclusiva do autor. É permitido rodar localmente para fins de avaliação e testes pessoais.

**Não é permitida** a distribuição, publicação, modificação ou uso comercial sem autorização prévia do autor.
