# PAP Online — Frontend

## Estado atual

Este diretório contém o frontend oficial do PAP Online, desenvolvido em HTML, CSS e JavaScript. As 18 telas dos perfis de aluno, profissional e administrador estão implementadas e publicadas no GitHub Pages.

O backend ainda será desenvolvido pela equipe responsável. A stack planejada é **Java + Spring Boot + Spring Web**, com uma API HTTP/REST que será consumida pelos serviços JavaScript deste diretório. Nenhuma aplicação Java/Spring ou autenticação real está implementada aqui.

- **Demonstração:** https://jpedrotg23.github.io/PAP-Online/
- **Repositório:** https://github.com/jpedrotg23/PAP-Online
- **Contrato de integração:** [CONTRATO-BACKEND.md](CONTRATO-BACKEND.md)

## Modo de demonstração público

No localhost e no GitHub Pages do projeto `jpedrotg23.github.io/PAP-Online/`, enquanto `ENABLE_PREVIEW` estiver habilitado e `API_BASE_URL` estiver vazio, qualquer visitante pode escolher Aluno, Profissional ou Administrador e navegar pelas interfaces com credenciais arbitrárias.

Essa pré-visualização é pública e não representa autenticação ou autorização reais. Senhas não são armazenadas, usuários não são criados e o frontend não envia cadastros, agendamentos, mensagens, alterações ou exclusões ao backend. Todos os dados operacionais continuam vazios até a integração oficial. Não utilize esse modo para informações pessoais ou dados reais.

Quando o backend estiver configurado, a equipe deverá definir a URL-base em `js/api-config.js`, desativar `ENABLE_PREVIEW` e validar a autenticação real. A presença de uma URL-base válida também desativa automaticamente a pré-visualização. A autorização definitiva deve ser aplicada pelo backend, nunca pelo JavaScript.

## Executar localmente

1. Abra a pasta `PAP-Online/` no VS Code.
2. Utilize a extensão Live Server ou outro servidor HTTP estático.
3. Abra `index.html` pelo servidor local.

Não é necessário instalar Java, Maven ou Spring para executar o frontend isoladamente.

## Estrutura principal

| Caminho | Responsabilidade |
|---|---|
| `index.html` e páginas de recuperação/cadastro | Entrada, cadastro e recuperação visual |
| `aluno-*.html` | Portal do aluno |
| `profissional-*.html` | Portal do profissional |
| `admin-*.html` | Portal administrativo |
| `css/style.css` | Estilos compartilhados e área profissional |
| `css/portais.css` | Estilos dos portais de aluno e administrador |
| `js/api-config.js` | URL-base e controle da pré-visualização |
| `js/api.js` | Cliente HTTP e wrappers dos recursos |
| `js/auth.js` | Papel, sessão de navegação e proteção visual das rotas |
| `js/app.js` | Login, cadastro e recuperação |
| `js/pro.js` | Interações da área profissional |
| `js/portais.js` | Interações das áreas de aluno e administrador |
| `CONTRATO-BACKEND.md` | Contratos REST propostos para a equipe Java/Spring |

## Integração com o backend Java/Spring

- Configure a URL-base em `js/api-config.js`. Ela deve incluir o prefixo escolhido para a API, por exemplo `https://api.exemplo.edu.br/api/v1`.
- Os caminhos usados por `js/api.js` são relativos à URL-base.
- O cliente HTTP envia requisições com `credentials: 'include'`, preparando o uso de sessão por cookie.
- A equipe backend deverá alinhar CORS, cookies, CSRF e autorização antes da integração.
- Os endpoints e JSON do contrato são propostas de integração, não uma API já disponível.
- A documentação não escolhe banco de dados, estratégia definitiva de autenticação ou versões de Java/Spring; essas decisões pertencem à equipe backend.

## Perfis

- Aluno: páginas `aluno-*`.
- Profissional: páginas `profissional-*`.
- Administrador: páginas `admin-*`.

O backend deve validar credenciais, perfil, sessão e permissões em todas as operações protegidas. Os controles JavaScript servem à navegação e à experiência visual; não são uma barreira de segurança.
