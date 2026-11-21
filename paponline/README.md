# PAP Online — aplicação monolítica

Este diretório contém a aplicação Spring Boot e seu frontend estático em `src/main/resources/static/`. O navegador usa a mesma origem para páginas e API; o deploy público é feito como aplicação no Render, não como site estático.

## Stack

Java 21, Spring Boot 4.1, Spring MVC, Spring Data JPA, Hibernate, H2 (local/teste), PostgreSQL (produção), BCrypt, Gradle e Docker.

## Executar localmente

```powershell
$env:SPRING_PROFILES_ACTIVE = "local"
.\gradlew.bat bootRun
```

O perfil local usa banco H2 em arquivo em `data/`. Dados de demonstração e console H2 vêm desligados; para habilitá-los explicitamente, use `PAP_DEMO_SEED=true` ou `PAP_H2_CONSOLE_ENABLED=true`. Nunca habilite o seed no perfil de produção.

## Testes e pacote

```powershell
.\gradlew.bat clean test
.\gradlew.bat bootJar
```

O teste usa H2 em memória no perfil `test`. Os testes de e-mail usam HTTP simulado e não acessam o Resend real.

## Configuração produtiva

Ative `SPRING_PROFILES_ACTIVE=prod`. São necessárias `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `RESEND_API_KEY`, `PAP_EMAIL_FROM`, `PAP_UPLOAD_DIR` e `PAP_CONSENT_VERSION`. A aplicação não escolhe H2 como fallback se o PostgreSQL estiver ausente. `PAP_DDL_AUTO` usa `update` por padrão no perfil `prod` para evitar remoções automáticas; ainda falta uma baseline versionada de migrations, que deve ser resolvida antes de operação com dados.

Uploads são escritos no caminho `PAP_UPLOAD_DIR`; o disco efêmero padrão do Render não é persistente. Configure disco persistente ou integre um serviço de objetos antes de aceitar documentos reais. Não coloque segredo em arquivos versionados.

O health check seguro está em `/health`. O `Dockerfile` executa `clean test bootJar`; ele não ignora a suíte.

Consulte [`CONTRATO-BACKEND.md`](CONTRATO-BACKEND.md) para rotas, regras de autorização e matriz dos requisitos.
