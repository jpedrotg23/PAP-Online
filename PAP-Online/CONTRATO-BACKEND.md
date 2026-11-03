# Contrato integrado — PAP Online

Este documento registra propostas de integração. Os contratos abaixo ainda precisam ser validados pela equipe de backend e não representam uma API implementada.

## Configuração
A URL-base deve ser configurada exclusivamente em `js/api-config.js`. O frontend usa cookies de sessão (`credentials: include`); cookies de autenticação devem ser `HttpOnly`, `Secure` em produção e associados a `SameSite` adequado. Verificações JavaScript controlam apenas navegação e nunca substituem autorização do servidor.

## Autenticação
- `POST /auth/login`: credenciais e, opcionalmente, perfil solicitado para UX. Resposta: identidade e `role` autorizado (`student`, `professional` ou `admin`). O perfil solicitado nunca concede acesso.
- `GET /auth/session`: identidade da sessão atual ou erro 401.
- `POST /auth/logout`: encerra a sessão.
- Alunos públicos podem usar cadastro. Profissionais e administradores devem ser provisionados pela instituição.

## Tela e contratos
| Área | Dados necessários | Operações propostas | Permissão |
|---|---|---|---|
| Login/cadastro | identidade, papel autorizado, estados de autenticação | login, cadastro público de aluno, recuperação | público / aluno |
| Perfil | dados pessoais permitidos pelo tipo de usuário, contato, segurança | consultar e atualizar perfil; alterar senha | usuário da própria sessão |
| Aluno — início/agendamento | próximos atendimentos, profissionais habilitados, horários disponíveis | listar, solicitar e consultar agendamento | aluno autenticado |
| Adaptações | solicitações, status, anexos, parecer | listar, criar, consultar anexos | aluno autenticado; equipe autorizada |
| Mensagens | conversas, mensagens, contatos autorizados | listar e enviar | participantes autorizados |
| Histórico | atendimentos, status, profissional, modalidade | listar e consultar detalhes | aluno autenticado |
| Profissional | agenda, solicitações, mensagens, perfil | listar agenda, atualizar atendimento, analisar solicitação | profissional autenticado |
| Admin | profissionais, alunos, mensagens, relatórios, notificações | gerenciar cadastros, publicar comunicados, exportar relatórios | administrador autorizado |

## Dados comuns
Todas as entidades deverão usar identificadores opacos. A origem dos nomes, matrículas, contatos, prazos, indicadores e permissões será exclusivamente backend. Nunca enviar ou armazenar em JavaScript dados sensíveis sem consentimento e autorização.

## Estado real da implementação frontend

### Implementado
- Configuração central da API em `js/api-config.js`.
- Cliente HTTP com cookies em `js/api.js`.
- Wrappers existentes para login, sessão, logout, perfil, agendamentos, adaptações, mensagens, notificações, relatórios e listagens administrativas básicas.
- Navegação por papéis e validação de correspondência entre perfil selecionado no login e papel da sessão.
- Redirecionamento para login quando não existe sessão válida em páginas restritas.
- Estados neutros de dados indisponíveis nas áreas de aluno, profissional e administrador.

### Preparado, mas não funcional
- Cadastro público, recuperação de senha, alteração de senha, filtros e formulários: a interface existe, porém a operação não está conectada a serviços reais.
- Os grupos de API de `appointments`, `adaptations`, `messages`, `notifications`, `reports` e `admin` são propostas de frontend; os endpoints e formatos JSON ainda não foram validados pelo backend.

## Contratos pendentes
- JSON exato de login, sessão e perfil.
- Formato de erro, paginação, filtros e ordenação.
- Tipos e estados de agendamento/adaptação/solicitação.
- Contrato de anexos, armazenamento seguro e limites.
- Nomenclatura definitiva dos papéis.
- Endpoints reais de mensagens, relatórios, notificações e administração.
- Política de recuperação de senha, MFA e expiração de sessão.
- Níveis de acesso por recurso e por operação.
