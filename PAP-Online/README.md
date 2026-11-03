# PAP Online

## Estado atual
Frontend oficial preparado para integração com backend. Não existe autenticação real, cadastro persistido nem dados operacionais neste repositório. Todos os registros fixos de demonstração foram removidos.

## Modo de demonstração público

No localhost e no GitHub Pages do projeto `jpedrotg23.github.io/PAP-Online/`, enquanto `ENABLE_PREVIEW` estiver habilitado e `API_BASE_URL` estiver vazio, qualquer visitante pode escolher Aluno, Profissional ou Administrador e navegar pelas interfaces com credenciais arbitrárias.

Essa pré-visualização é pública e não representa autenticação ou autorização reais. Senhas não são armazenadas, usuários não são criados e o frontend não envia cadastros, agendamentos, mensagens, alterações ou exclusões ao backend. Todos os dados operacionais continuam vazios até a integração oficial.

Quando o backend estiver configurado, defina `API_BASE_URL` e desative `ENABLE_PREVIEW`. A presença de uma URL-base válida desativa automaticamente a pré-visualização. A autorização real passará a depender exclusivamente do backend.

## Executar
Abra `index.html` em um servidor local ou pela extensão Live Server do VS Code.

## Integração
- Configure a URL-base em `js/api-config.js`.
- Os serviços HTTP estão em `js/api.js`.
- A identificação do perfil e o controle de navegação estão em `js/auth.js`.
- Os contratos propostos estão em `CONTRATO-BACKEND.md`.

## Perfis
- Aluno: páginas `aluno-*`.
- Profissional: páginas `profissional-*`.
- Administrador: páginas `admin-*`.

O backend deve validar credenciais, perfil, sessão e permissões. Os controles JavaScript não são segurança real.
