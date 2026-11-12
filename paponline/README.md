# PAP Online — Frontend

## Estado atual

Este diretório contém o frontend do PAP Online, desenvolvido em HTML, CSS e JavaScript. As **20 telas** dos perfis de aluno, profissional e administrador estão implementadas e **integradas ao backend real**.

- **Backend:** Spring Boot 3 (Java 17) + Spring Web + Spring Data JPA + H2 (em memória)
- **Integração:** as telas consomem a API real servida pelo mesmo Spring Boot
- **Contrato de integração:** [CONTRATO-BACKEND.md](CONTRATO-BACKEND.md)

O frontend é servido **pelo próprio backend**, a partir de `src/main/resources/static/`. Não é mais uma pasta separada publicada no GitHub Pages.

## Como executar

O frontend **não roda sozinho** — ele precisa do backend no ar.

```bash
# Na raiz do projeto
gradlew.bat bootRun     # Windows
./gradlew bootRun       # Linux / macOS