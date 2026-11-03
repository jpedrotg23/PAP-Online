# PAP Online

## Estado atual
Frontend oficial preparado para integração com backend. Não existe autenticação real, cadastro persistido nem dados operacionais neste repositório. Todos os registros fixos de demonstração foram removidos.

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
