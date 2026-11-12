# Contrato de integração — PAP Online

> **Status:** backend implementado como monolito modular em Spring Boot + Spring Web, com banco H2 em memória para desenvolvimento. Frontend integrado consumindo a API. Decisões de produção (Spring Security/JWT, CSRF, banco definitivo, provedor de e-mail e armazenamento de anexos) ainda em aberto.

## 1. Objetivo e arquitetura

O PAP Online foi construído como **monolito modular**, com frontend estático (HTML/CSS/JS) servido pelo próprio backend a partir de `src/main/resources/static/`.

**Stack implementada:**

- **Java 17+** — linguagem do backend.
- **Spring Boot 3.x** — configuração e bootstrap.
- **Spring Web (MVC)** — endpoints HTTP/REST.
- **Spring Data JPA** — persistência.
- **H2 Database (em memória)** — banco de desenvolvimento, criado automaticamente pelo Hibernate (`ddl-auto=update`).
- **Lombok** — geração de boilerplate.
- **Bean Validation** — validação de entrada (parcial, em evolução).

**Não implementados ainda (decisão de produção pendente):**
- Spring Security / JWT.
- Proteção CSRF.
- Banco relacional definitivo (MySQL/PostgreSQL).
- Provedor de e-mail e SMS.
- Armazenamento de anexos.

**Fluxo atual:**

`Navegador → Frontend estático (servido por Spring) → fetch → Controllers REST → Services → Repositories → H2`

### Responsabilidades

**Frontend**
- Exibir as telas e coletar dados.
- Enviar requisições HTTP + JSON conforme este contrato.
- Exibir estados de carregamento, sucesso, erro e ausência de dados.
- Manter somente estado de interface; **não** decidir permissões reais.

**Backend**
- Autenticar via `/auth/login` e manter a sessão em `HttpSession` (cookie `JSESSIONID`).
- Determinar o papel autorizado a partir do tipo do usuário na sessão, **nunca** do `requestedRole` enviado pelo frontend.
- Validar dados de entrada (Bean Validation em evolução).
- Aplicar regras de negócio em cada endpoint.
- Persistir dados e retornar respostas no formato acordado.
- **Garantir que nenhuma operação sensível dependa apenas do `requestedRole` do cliente.**

## 2. Configuração da API

A URL-base fica em `js/api-config.js`:

```javascript
window.PAP_CONFIG = Object.freeze({
  API_BASE_URL: '',          // vazio = mesmo host (Spring serve o frontend)
  ENABLE_PREVIEW: false
});