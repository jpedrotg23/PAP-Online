# PAP Online

Sistema web monolítico do Programa de Apoio Psicopedagógico (PAP) da Universidade de Fortaleza. O backend Java/Spring Boot também serve o frontend HTML, CSS e JavaScript incluído no mesmo artefato.

**Aplicação publicada:** https://pap-online.onrender.com/

## Arquitetura e tecnologias

- Java 21 e Spring Boot 4.1
- Spring MVC, Spring Data JPA, Hibernate e Bean Validation
- Sessões HTTP, perfis ALUNO/PROFISSIONAL/ADMIN e BCrypt
- H2 para desenvolvimento/testes locais; PostgreSQL no perfil `prod`
- HTML/CSS/JavaScript em `paponline/src/main/resources/static/`
- Gradle e Docker

O fluxo é `navegador → Spring MVC → services → repositories/JPA → banco`. Não há frontend publicado separadamente nem arquitetura de microserviços.

## Funcionalidades presentes no backend

Autenticação por sessão, cadastro público de alunos com aceite de consentimento, administração de profissionais, agenda e atendimentos, solicitações de adaptação com anexos, conversas, notificações, avisos e áreas de serviço. O escopo implementado ainda não cobre todos os requisitos oficiais; a matriz de rastreabilidade e as pendências estão em [`paponline/CONTRATO-BACKEND.md`](paponline/CONTRATO-BACKEND.md).

## Execução local

Requer JDK 21. No PowerShell:

```powershell
cd paponline
$env:SPRING_PROFILES_ACTIVE = "local"
.\gradlew.bat bootRun
```

No Linux/macOS:

```bash
cd paponline
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

O perfil local usa H2 em arquivo (`paponline/data/`), não cria contas de demonstração por padrão e mantém o console H2 desligado. Para habilitá-lo explicitamente, defina `PAP_H2_CONSOLE_ENABLED=true`; para carregar dados de demonstração apenas fora de produção, defina `PAP_DEMO_SEED=true`.

## Testes e build

```powershell
cd paponline
.\gradlew.bat clean test
.\gradlew.bat bootJar
```

O workflow de CI executa os testes e gera o JAR, sem deploy e sem publicar frontend estático.

## Variáveis de ambiente

Para produção, ative `SPRING_PROFILES_ACTIVE=prod` e configure:

| Variável | Uso |
|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco |
| `RESEND_API_KEY` | Chave fornecida pela conta Resend |
| `PAP_EMAIL_FROM` | Remetente em domínio verificado no Resend |
| `PAP_UPLOAD_DIR` | Diretório montado em armazenamento persistente |
| `PAP_CONSENT_VERSION` | Versão aprovada do termo de consentimento |
| `PAP_DDL_AUTO` | Política Hibernate; padrão atual `update` no perfil produtivo |
| `PAP_SESSION_COOKIE_SECURE` | Cookie Secure; produção força `true` |

Não versionar arquivos com valores secretos. O envio de e-mail falha fechado sem as variáveis do Resend e não devolve código de recuperação.

## Deploy no Render

Configure o serviço Docker com o diretório raiz `paponline`, perfil `prod`, PostgreSQL externo e as variáveis acima. O armazenamento de upload precisa ser um disco persistente do Render montado em `PAP_UPLOAD_DIR` ou um serviço de objetos que ainda deverá ser integrado. Configure health check em `/health`. Faça o primeiro deploy somente após criar/verificar o schema PostgreSQL, configurar e-mail/domínio, armazenamento durável, backups e os demais itens pendentes documentados.

O workflow antigo de GitHub Pages foi substituído por CI. **A desativação do GitHub Pages é manual**: nas configurações do repositório, defina Pages como desativado/remova a origem de publicação.

## Documentação

- Backend e execução: [`paponline/README.md`](paponline/README.md)
- API, configuração e rastreabilidade: [`paponline/CONTRATO-BACKEND.md`](paponline/CONTRATO-BACKEND.md)
- Especificação oficial: [`docs/Especificação de Requisitos de Software — PAP Online.md`](docs/Especificação%20de%20Requisitos%20de%20Software%20%E2%80%94%20PAP%20Online.md)
