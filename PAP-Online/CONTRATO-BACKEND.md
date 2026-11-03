# Contrato de integração — PAP Online

> **Status:** especificação inicial para alinhamento entre as equipes. O backend ainda não foi iniciado. Os endpoints e exemplos deste documento são propostas compatíveis com o frontend atual e precisam ser aprovados pela equipe Java/Spring antes da implementação.

## 1. Objetivo e arquitetura

O frontend está implementado em HTML, CSS e JavaScript e publicado no GitHub Pages. O backend será desenvolvido separadamente com:

- **Java** — linguagem do backend.
- **Spring Boot** — estrutura da aplicação e configuração.
- **Spring Web** — criação dos endpoints HTTP/REST.
- **Spring Security** — recomendado para autenticação, sessão e autorização; adoção e configuração devem ser confirmadas pela equipe backend.

O fluxo esperado é:

`Navegador → Frontend JavaScript → API REST Spring Web → Serviços/regras de negócio → Persistência definida pela equipe backend`

Este repositório não define banco de dados, provedor de e-mail, armazenamento de arquivos ou versões de Java/Spring. Essas decisões ainda precisam ser tomadas pela equipe de backend.

### Responsabilidades

**Frontend**
- Exibir as telas e coletar dados.
- Enviar requisições HTTP e JSON conforme os contratos acordados.
- Exibir estados de carregamento, sucesso, erro e ausência de dados.
- Manter somente estado de interface; não decidir permissões reais.

**Backend**
- Validar e autenticar usuários.
- Determinar o papel autorizado, independentemente do perfil selecionado no formulário.
- Validar todos os dados recebidos.
- Aplicar regras de negócio e autorização em cada endpoint.
- Persistir dados e retornar respostas no formato acordado.
- Proteger sessões, dados pessoais e anexos.

## 2. Configuração da API

A URL-base fica em `js/api-config.js`:

```javascript
window.PAP_CONFIG = Object.freeze({
  API_BASE_URL: 'https://api.exemplo.edu.br/api/v1',
  ENABLE_PREVIEW: false
});
```

O endereço acima é apenas ilustrativo. A URL real será fornecida pela equipe backend.

O cliente atual concatena a URL-base com os caminhos abaixo. Portanto, se a base terminar em `/api/v1`, a chamada `/auth/login` será enviada para `https://api.exemplo.edu.br/api/v1/auth/login`.

**Importante:** antes de habilitar o backend, defina uma única convenção para o prefixo (`/api/v1`, por exemplo) e mantenha a mesma convenção no Spring e em `api-config.js`.

## 3. Convenções HTTP e JSON

Propostas para manter consistência entre os controllers Spring e o cliente JavaScript:

- API REST com JSON em UTF-8.
- Propriedades JSON em `camelCase`.
- Datas e horários em ISO 8601, com fuso/offset explícito quando representarem horário local.
- IDs opacos (UUID ou outro identificador não sequencial, a ser definido pelo backend).
- Valores monetários, caso existam no futuro, não devem ser enviados como texto formatado.
- Respostas de listagem devem ter paginação consistente quando o volume justificar.
- Campos e regras de validação devem ser documentados antes da integração.

### Códigos HTTP propostos

| Código | Uso |
|---|---|
| `200 OK` | Consulta ou operação concluída com resposta |
| `201 Created` | Recurso criado |
| `204 No Content` | Operação concluída sem corpo de resposta |
| `400 Bad Request` | Dados inválidos ou requisição malformada |
| `401 Unauthorized` | Usuário não autenticado ou sessão inválida |
| `403 Forbidden` | Usuário autenticado sem permissão |
| `404 Not Found` | Recurso inexistente ou não visível ao usuário |
| `409 Conflict` | Conflito de regra de negócio, como horário indisponível |
| `422 Unprocessable Content` | Opcional: erro de validação semântica, se a equipe adotar esse padrão |
| `500 Internal Server Error` | Erro inesperado do servidor |

### Formato de erro proposto

O cliente atual lê a propriedade `message` da resposta de erro. Para facilitar o tratamento futuro, recomenda-se padronizar:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Existem campos inválidos.",
  "fieldErrors": {
    "identifier": "Informe um e-mail válido."
  }
}
```

`code` deve ser estável para tratamento programático; `message` deve ser apropriada para apresentação; `fieldErrors` é opcional. A equipe deve definir se usará esse formato ou um formato equivalente e documentá-lo em todos os controllers.

## 4. Autenticação, sessão e papéis

Os três papéis de negócio esperados pelo frontend são:

- `student` — aluno.
- `professional` — profissional.
- `admin` — administrador.

O frontend aceita atualmente aliases de papel para compatibilidade, mas a API deve retornar um único nome canônico: `student`, `professional` ou `admin`.

### Endpoints propostos

| Método | Caminho | Finalidade |
|---|---|---|
| `POST` | `/auth/login` | Autenticar credenciais |
| `GET` | `/auth/session` | Consultar a sessão atual |
| `POST` | `/auth/logout` | Encerrar a sessão |

### Login

O frontend atual envia:

```json
{
  "identifier": "usuario@exemplo.edu.br",
  "password": "senha-informada",
  "requestedRole": "student"
}
```

`requestedRole` serve somente para UX e comparação no frontend. Nunca deve conceder papel ou permissão.

Resposta canônica proposta:

```json
{
  "user": {
    "id": "identificador-opaco",
    "role": "student",
    "name": "Nome apresentado na interface"
  }
}
```

O campo `name` é opcional para o fluxo atual de redirecionamento, mas será útil para preencher o cabeçalho das páginas quando a integração de perfil for implementada. A resposta não deve incluir senha, hash, token secreto ou dados pessoais desnecessários.

O papel retornado deve ser obtido da conta autenticada no servidor. Se o perfil da conta não corresponder ao perfil selecionado, o frontend mostrará uma mensagem e não deve conceder o acesso solicitado.

### Consulta de sessão

`GET /auth/session` deve retornar a identidade autenticada no mesmo formato canônico do login (`{ "user": { "id": "...", "role": "..." } }`). Sem sessão válida, deve retornar `401`.

### Logout

`POST /auth/logout` deve invalidar a sessão no servidor e limpar o cookie. A resposta pode ser `204 No Content`, que já é suportada pelo cliente atual.

### Sessão por cookie

O frontend atual usa `fetch` com `credentials: 'include'`. Se a equipe optar por sessão baseada em cookie:

- Cookie de sessão com `HttpOnly`.
- `Secure` em produção.
- Política `SameSite` definida conforme a topologia de domínios.
- Escopo de cookie restrito ao necessário.
- Expiração, renovação e revogação definidas pelo backend.
- Não retornar o identificador de sessão em JSON nem armazená-lo no JavaScript.

**Atenção à hospedagem:** o frontend está em `jpedrotg23.github.io`. Se a API ficar em outro domínio registrável, o cookie poderá ser considerado de terceiro pelo navegador. `SameSite=None; Secure` pode ser necessário para envio cross-site, mas políticas de bloqueio de cookies de terceiros ainda podem interferir. A equipe deve testar nos navegadores suportados e considerar hospedar frontend e API sob o mesmo domínio organizacional ou adotar outra estratégia aprovada.

## 5. CORS, credenciais e CSRF

### CORS

A origem de produção atual do frontend é:

`https://jpedrotg23.github.io`

O backend deve permitir explicitamente essa origem e as origens locais usadas durante o desenvolvimento, conforme a equipe definir.

Quando houver sessão por cookie:
- Permitir credenciais (`allowCredentials(true)`).
- Não combinar credenciais com origem curinga (`*`).
- Permitir somente métodos necessários, inicialmente `GET`, `POST`, `PATCH`, `PUT`, `DELETE` e `OPTIONS`, conforme os endpoints efetivamente implementados.
- Permitir os headers de conteúdo e segurança acordados.
- Responder corretamente às requisições preflight `OPTIONS`.
- Processar CORS antes dos filtros de autenticação que poderiam bloquear o preflight.

A configuração definitiva deverá ser feita no backend Spring e validada com a URL real de produção.

### CSRF

Como a proposta atual usa cookies de sessão, a proteção CSRF precisa ser definida antes da ativação da autenticação real.

A equipe backend deverá decidir, documentar e implementar:
1. Estratégia de token CSRF compatível com a sessão.
2. Como o frontend obtém o token.
3. Qual header envia o token nas operações que alteram estado.
4. Como login, logout, cadastro e demais operações serão protegidos.

O cliente JavaScript atual **ainda não implementa o fluxo de token CSRF**. Quando a equipe definir o mecanismo (por exemplo, Spring Security com token CSRF), será necessário atualizar `api.js` para obter e enviar o token antes de habilitar operações reais. Não desative a proteção CSRF apenas para contornar uma falha de integração sem uma decisão de segurança documentada.

## 6. Matriz de recursos e permissões

| Área | Operações propostas | Papel autorizado |
|---|---|---|
| Cadastro público | Solicitar criação de conta de aluno | Público, sujeito a validação |
| Perfil | Consultar e atualizar o próprio perfil | Usuário autenticado |
| Aluno — agenda | Consultar profissionais/horários e solicitar agendamento | `student` |
| Histórico | Consultar atendimentos próprios | `student` |
| Adaptações | Criar e consultar solicitações próprias | `student`; equipe autorizada para análise |
| Mensagens | Consultar conversas e enviar mensagens | Participantes autorizados |
| Profissional | Consultar agenda e solicitações atribuídas; registrar decisões autorizadas | `professional` |
| Administração | Gerenciar alunos/profissionais, notificações e relatórios | `admin` com permissões institucionais |

A tabela é uma proposta de autorização inicial. A equipe deverá detalhar permissões por operação e recurso. O servidor deve validar propriedade, vínculo e escopo dos dados em cada requisição; esconder botões no frontend não é controle de acesso.

## 7. Endpoints por recurso

Os caminhos abaixo refletem os wrappers já existentes em `js/api.js), salvo quando indicados como extensão futura.

| Método | Caminho | Estado no cliente atual | Observação |
|---|---|---|---|
| `GET` | `/me` | Wrapper existente | Perfil do usuário autenticado |
| `PATCH` | `/me` | Wrapper existente | Atualização do próprio perfil |
| `GET` | `/appointments` | Wrapper existente | Listar atendimentos autorizados |
| `POST` | `/appointments` | Wrapper existente | Criar solicitação/agendamento |
| `GET` | `/adaptations` | Wrapper existente | Listar solicitações autorizadas |
| `POST` | `/adaptations` | Wrapper existente | Criar solicitação |
| `GET` | `/messages` | Wrapper existente | Listar conversas/mensagens autorizadas |
| `POST` | `/messages` | Wrapper existente | Enviar mensagem |
| `GET` | `/notifications` | Wrapper existente | Listar notificações do usuário |
| `GET` | `/reports` | Wrapper existente | Consultar relatórios autorizados |
| `GET` | `/admin/professionals` | Wrapper existente | Listar profissionais |
| `GET` | `/admin/students` | Wrapper existente | Listar alunos |

### Extensões ainda não disponíveis no cliente atual

As telas também indicam operações que precisarão ser detalhadas e conectadas futuramente:

- Cadastro público e recuperação/redefinição de senha.
- Alteração de senha.
- Consulta de disponibilidade e cancelamento/alteração de agendamento.
- Análise, aprovação, devolução e conclusão de solicitações de adaptação.
- Upload e download autorizado de anexos.
- Consulta de conversas e mensagens por conversa, caso o modelo adotado exija recursos separados.
- Publicação, arquivamento e leitura de comunicados.
- Criação, edição e desativação administrativa de alunos e profissionais.
- Exportação de relatórios.

Não considere esses endpoints implementados apenas porque existe uma tela ou botão correspondente.

## 8. Validação dos dados e DTOs Spring

A equipe deverá definir DTOs de entrada e saída para cada endpoint, sem expor diretamente entidades de persistência.

Recomendações:
- Utilizar validação de entrada no backend (por exemplo, Bean Validation com `@Valid` e restrições adequadas).
- Validar também regras de negócio, como disponibilidade do horário e vínculo do profissional.
- Não confiar em identificadores, papéis, status ou valores calculados enviados pelo navegador.
- Retornar somente os campos necessários à tela e permitidos ao papel autenticado.
- Manter nomes de campos JSON estáveis e alinhados com o frontend.

## 9. Paginação, filtros, datas e anexos

Esses contratos ainda precisam de decisão conjunta:

- **Paginação:** definir convenção única, como `page`, `size`, `totalElements` e `totalPages`, ou equivalente.
- **Filtros:** documentar parâmetros permitidos, valores e combinação de filtros por recurso.
- **Datas:** usar ISO 8601 e definir o fuso de referência e a conversão para exibição.
- **Anexos:** definir tipos permitidos, limite de tamanho, armazenamento, autorização de download, verificação de conteúdo e política de retenção.
- **Ordenação:** definir campos e direção permitidos para cada listagem.

O envio de arquivos não está implementado no cliente HTTP atual; precisará de um fluxo específico, provavelmente usando `FormData` ou upload controlado pelo backend.

## 10. Organização sugerida no Spring

A organização interna é decisão da equipe backend. Como ponto de partida, uma separação por responsabilidade pode incluir:

- **Controllers:** recebem requisições HTTP e retornam respostas.
- **DTOs:** representam entradas e saídas da API.
- **Services:** concentram regras de negócio.
- **Repositories:** abstraem a persistência escolhida.
- **Security/Configuration:** configuram autenticação, autorização, CORS, sessão e CSRF.
- **Exception handlers:** padronizam erros HTTP.

Essa organização é uma sugestão, não uma exigência de arquitetura ou escolha de banco de dados.

## 11. Estado real da implementação

### Implementado neste repositório
- Frontend HTML, CSS e JavaScript para aluno, profissional e administrador.
- Configuração central da API em `js/api-config.js`.
- Cliente HTTP com `credentials: 'include'` em `js/api.js`.
- Wrappers para login, sessão, logout, perfil, agendamentos, adaptações, mensagens, notificações, relatórios e listagens administrativas básicas.
- Navegação por papéis e comparação do papel retornado pela API com o perfil solicitado na tela.
- Estados vazios para dados ainda não recebidos.
- Modo de demonstração público para apresentação das interfaces.

### Preparado, mas ainda não funcional
- Cadastro, recuperação e redefinição de senha.
- Alteração persistente de perfil/senha.
- Operações reais de agendamento, adaptação, mensagens, notificações e relatórios.
- Upload e download de anexos.
- Gerenciamento administrativo persistido.
- Proteção CSRF e integração real com sessão/cookies.
- Carregamento dos dados reais nas telas.

### Ainda não iniciado
- Aplicação Java/Spring Boot.
- Controllers REST.
- Services e regras de negócio do backend.
- Persistência e banco de dados.
- Spring Security e configuração de autenticação.
- Configuração CORS/CSRF de produção.
- Testes de integração entre frontend e backend.

## 12. Checklist de integração e homologação

Antes de considerar a integração pronta, as equipes deverão confirmar:

- [ ] Stack e versões de Java/Spring definidas.
- [ ] URL-base e prefixo da API definidos.
- [ ] Contratos de login, sessão e logout aprovados.
- [ ] Papéis canônicos e matriz de permissões aprovados.
- [ ] Estratégia de sessão e cookies testada nos navegadores suportados.
- [ ] CORS configurado para as origens necessárias.
- [ ] Estratégia CSRF implementada no backend e no frontend.
- [ ] JSON de sucesso e erro padronizados.
- [ ] DTOs e validações definidos para cada recurso.
- [ ] Paginação, filtros, datas e anexos documentados.
- [ ] Testes de autorização por papel e por propriedade dos dados.
- [ ] Testes de integração dos formulários e listagens.
- [ ] Modo de demonstração desabilitado em produção quando a autenticação real estiver pronta.
- [ ] Homologação funcional e de segurança realizada.

## 13. Critério para alterar o modo de demonstração

O modo de demonstração é público e existe exclusivamente para apresentar o frontend. Antes de disponibilizar autenticação real:

1. Configurar a URL correta em `API_BASE_URL`.
2. Definir `ENABLE_PREVIEW: false`.
3. Implementar e validar autenticação, sessão, autorização, CORS e CSRF.
4. Confirmar que todas as páginas protegidas consultam a sessão real.
5. Testar que chamadas sem autenticação retornam `401` e chamadas sem permissão retornam `403`.
6. Verificar que nenhuma operação real permanece simulada no JavaScript.

A segurança definitiva deve ser aplicada no backend, independentemente do estado do frontend.
