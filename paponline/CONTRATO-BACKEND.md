# Contrato e estado técnico — PAP Online

Este contrato descreve o código atualmente presente, não uma promessa de funcionalidades futuras. O projeto é um monólito: o Spring Boot serve a API e os arquivos estáticos em `src/main/resources/static/`. A stack é Java 21, Spring Boot 4.1, Spring MVC, Spring Data JPA, Hibernate, H2/PostgreSQL e Gradle.

## Sessão, autorização e dados pessoais

- `/auth/login` autentica e cria sessão HTTP; a identidade e o perfil são derivados da conta, não de `requestedRole`. O identificador da sessão é regenerado no login e o logout invalida a sessão.
- A sessão expira após 30 minutos. O cookie é HttpOnly, SameSite=Lax e Secure no perfil `prod`.
- Operações mutáveis de sessões autenticadas validam `Origin`/`Referer` e bloqueiam contexto `Sec-Fetch-Site: cross-site`. A interface deve ser servida pela mesma origem. Requisições de escrita sem origem/referer são recusadas.
- As rotas de usuário retornam DTOs de aluno, profissional e administrador. A entidade também ignora a senha na serialização. O segredo de recuperação não tem endpoint de leitura.
- `POST /alunos` é o único cadastro público. `POST /profissionais`, `/admins` e alterações administrativas exigem ADMIN. Alunos consultam seus próprios dados; profissionais consultam alunos/atendimentos com vínculo. Há verificação adicional nos serviços para dados sensíveis.
- Alunos só veem suas notificações e solicitações. Conversas e mensagens exigem participação; a conversa aluno-profissional requer atendimento registrado. O profissional só vê documentos de solicitações de sua área ou atribuídas a ele; o aluno só os próprios. Administradores não recebem conteúdo de anexos ou solicitações.
- O modelo `Atendimento` atualmente não tem campo de prontuário ou narrativa clínica. O setor administrativo só recebe dados de agenda; os dados clínicos previstos em RF28/RGN06 ainda não foram implementados.
- Exclusão de contas é anonimização/desativação, mantendo os IDs de relacionamentos e registros. Exclusão física de atendimentos foi bloqueada para preservar histórico. Não existe exportação de dados ou fluxo completo de direitos LGPD.

## Recuperação de senha e e-mail

`POST /auth/recuperar` aceita `identifier` (e-mail ou telefone normalizado). A conta é localizada e o código de seis dígitos é enviado ao e-mail cadastrado usando a API HTTPS do Resend. O código é salvo com BCrypt somente depois que o Resend aceita o envio, expira em 15 minutos, tem até cinco tentativas e o envio é limitado a um por minuto e cinco por dia por conta. Uma nova solicitação invalida códigos anteriores antes da tentativa de envio; se a entrega falhar, nenhum código novo é salvo. Senhas iguais à atual são recusadas.

O endpoint nunca devolve o código. Contas inexistentes e falhas na entrega recebem o mesmo HTTP 200 e o mesmo corpo genérico: `Se o endereço estiver cadastrado, você receberá as instruções de recuperação.` Assim, a resposta não confirma se a conta existe nem afirma que o e-mail foi enviado. Falhas operacionais são registradas somente com metadados diagnósticos seguros (código, classe técnica ou status HTTP), sem e-mail, código, token ou credenciais.

Configure `RESEND_API_KEY` e `PAP_EMAIL_FROM` no ambiente. O endereço remetente deve pertencer a domínio verificado no Resend. A suíte automatizada usa servidor HTTP simulado; não envia e-mails reais.

## Rotas principais

| Grupo | Regras relevantes |
|---|---|
| `/auth` | Login, sessão, logout e recuperação de senha |
| `/alunos` | Cadastro público; leitura/alteração por titular ou ADMIN; listagem ADMIN |
| `/profissionais` e `/admins` | Cadastro e gestão administrativa; criação de profissional não é pública |
| `/atendimentos` | ALUNO vê os próprios, PROFISSIONAL os vinculados à sua agenda; ADMIN consulta agenda, sem prontuário; finalização só pelo responsável |
| `/horarios` | PROFISSIONAL publica e mantém sua agenda; ADMIN pode gerenciar; conflitos de horários sobrepostos são recusados |
| `/conversas` | Apenas participantes; conversa aluno-profissional requer atendimento atual ou anterior |
| `/notificacoes` | Leitura e marcação apenas pelo destinatário; criação manual ADMIN |
| `/avisos`, `/areas` | Escrita ADMIN; avisos publicados podem ser consultados por usuários autenticados |
| `/solicitacoes` | Aluno cria/acompanha; profissional analisa somente solicitação com área explícita compatível ou atribuição existente; ADMIN não lê justificativa/anexos |
| `/health` | Retorna somente `{"status":"UP"}`; não divulga banco, ambiente ou credenciais |

O campo `areaId` da solicitação é opcional para manter compatibilidade com telas legadas. Se ausente, o profissional não pode analisá-la até que exista atribuição autorizada; falta uma tela/fluxo de coordenação para atribuição de área.

## Perfis, banco e armazenamento

- `local`: H2 em arquivo (`data/`), sem seed e console H2 desligados por padrão. O console só é habilitado explicitamente com `PAP_H2_CONSOLE_ENABLED=true`.
- `test`: H2 em memória, isolado e recriado para testes.
- `prod`: PostgreSQL obrigatório por `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD`; não existe fallback para H2. Cookie Secure é forçado e DataLoader não executa, mesmo que a variável de seed seja indevidamente ligada.
- `PAP_DEMO_SEED` é falso por padrão e só deve ser usado em ambiente local isolado. O seed contém dados e credenciais de demonstração conhecidos.
- `PAP_UPLOAD_DIR` define o diretório de uploads; arquivos são limitados a 10 MB e PDF/JPEG/PNG são verificados por extensão, MIME declarado e assinatura básica do conteúdo. A resposta não revela caminhos locais.
- No Render, disco padrão é efêmero. Configure um Persistent Disk montado em `PAP_UPLOAD_DIR` ou integre armazenamento de objetos antes de guardar documentos reais.

O Hibernate usa `update` por padrão no perfil `prod`, sem remoção automática de colunas/dados. Ainda não há baseline nem migrations versionadas; o schema do PostgreSQL deve ser preparado/verificado antes da publicação. Isso não substitui uma estratégia Flyway/Liquibase e é um bloqueador para operação de dados sensíveis.

## Variáveis de ambiente de produção

| Nome | Obrigatoriedade | Descrição |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | sim | `prod` |
| `SPRING_DATASOURCE_URL` | sim | URL JDBC PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | sim | Usuário PostgreSQL |
| `SPRING_DATASOURCE_PASSWORD` | sim | Senha PostgreSQL |
| `RESEND_API_KEY` | para recuperação | Chave real provisionada no Resend |
| `PAP_EMAIL_FROM` | para recuperação | Remetente em domínio verificado |
| `PAP_UPLOAD_DIR` | para anexos | Caminho em disco persistente ou adaptador de storage |
| `PAP_CONSENT_VERSION` | sim antes do cadastro | Versão institucionalmente aprovada do termo |
| `PAP_DDL_AUTO` | opcional | Política Hibernate; padrão `update` |

Nenhum segredo deve ser salvo no repositório. No Render, defina o diretório raiz como `paponline`, configure variáveis, banco, remetente verificado, disco/backups e health check `/health`. O deploy não é executado por este repositório.

## Rastreabilidade da especificação oficial

Classificação baseada no código desta revisão. **Implementado** indica comportamento observável; **Parcial** indica cobertura incompleta; **Pendente** indica ausência de evidência ou necessidade de decisão/infraestrutura.

### Requisitos funcionais

| ID | Estado | Evidência / pendência |
|---|---|---|
| RF01 | Parcial | Cadastro público de aluno e aceite persistido; vínculo ativo não é validado institucionalmente. |
| RF02 | Parcial | Login por e-mail/telefone e senha; telefone é comparado sem máscara e registros legados ambíguos são recusados. |
| RF03 | Parcial | Envio por participante; faltam aviso permanente do canal não emergencial em telas administrativas e demais verificações de UX. |
| RF04 | Implementado | Consulta de mensagens autorizada aos participantes. |
| RF05 | Parcial | Publicação admin gera notificações para públicos configurados; não há entrega externa. |
| RF06 | Parcial | Notificações de agendamento, lembrete e mudança de solicitação; sem garantia de entrega externa/monitorada. |
| RF07 | Parcial | Notificação de agendamento/cancelamento e solicitação por área disponível; falta fluxo completo de atribuição. |
| RF08 | Parcial | Reserva de horário publicado, bloqueio transacional e controle de faltas; regras institucionais de calendário não existem. |
| RF09 | Parcial | Telas estáticas estão incluídas; painel completo e indicadores não foram verificados como contrato de backend. |
| RF10 | Implementado | Logout invalida sessão HTTP. |
| RF11 | Parcial | Alteração de telefone via atualização de perfil; não há verificação de posse do novo telefone. |
| RF12 | Parcial | Alteração de e-mail via atualização; não há confirmação do endereço novo. |
| RF13 | Implementado | Alteração exige senha atual, valida força e recusa senha igual à atual. |
| RF14 | Parcial | Dados de perfil retornados por DTO; upload/gestão de foto não implementado. |
| RF15 | Parcial | Atualização de nome e foto URL; upload de imagem e validação de origem não implementados. |
| RF16 | Parcial | Consultas de agenda/histórico existem; painel consolidado não é endpoint dedicado. |
| RF17 | Parcial | Conta é anonimizada/desativada e sessão própria invalidada; direitos/exportação LGPD incompletos. |
| RF18 | Parcial | Titular/profissional responsável podem cancelar; faltas tardias são registradas como falta. |
| RF19 | Implementado | Somente profissional responsável finaliza após início agendado. |
| RF20 | Parcial | Data/hora, área, modalidade e IDs são retornados; nomes e descrição clínica completos não são. |
| RF21 | Parcial | Histórico de atendimentos por aluno/profissional com segregação. |
| RF22 | Parcial | Código enviado via Resend, hash, 15 min, tentativas, rate limit e uso único; falta segredo/dominio real configurado. |
| RF23 | Parcial | Remarcação pelo aluno com 24h e horário novo disponível; sem calendário institucional. |
| RF24 | Parcial | Solicitação e anexos; a área é opcional e precisa ser preenchida/atribuída para análise profissional. |
| RF25 | Implementado | Aluno consulta suas solicitações e respectivos status. |
| RF26 | Parcial | Profissional valida área/atribuição; coordenação ainda não possui fluxo de atribuição e alerta de atraso. |
| RF27 | Parcial | Publicar, editar e bloquear horários; calendário, feriados e expediente não integrados. |
| RF28 | Pendente | O modelo não armazena texto de prontuário/registro clínico. |
| RF29 | Parcial | Profissional consulta somente atendimentos vinculados; não há prontuário clínico. |
| RF30 | Parcial | ADMIN cria/edita/desativa profissionais; governança de permissões e múltiplas áreas não concluída. |
| RF31 | Pendente | Não existe geração/exportação de relatórios CSV/PDF. |
| RF32 | Parcial | Scheduler registra falta após 24h do fim; sem justificativa/feriados ou monitoramento de execução. |
| RF33 | Parcial | Aceite, timestamp e versão são persistidos; texto/versão precisam de aprovação e configuração institucional. |
| RF34 | Parcial | Tabela persistente de auditoria registra acesso/mutação; imutabilidade não é garantida no banco contra DBA. |

### Requisitos não funcionais

| ID | Estado | Evidência / pendência |
|---|---|---|
| RNF01 | Pendente | Não foram executados testes de carga/latência nem comprovados 200 usuários e limiares. |
| RNF02 | Parcial | HTTPS depende do Render/proxy e DNS; execução local não força TLS. |
| RNF03 | Implementado | Perfis, BCrypt, sessão com 30 min, cookie HttpOnly/SameSite e Secure no perfil prod. |
| RNF04 | Parcial | Consentimento e anonimização existem; fluxos LGPD completos e validação jurídica estão pendentes. |
| RNF05 | Pendente | 99% de disponibilidade e backups de 30 dias exigem configuração/contrato externo no Render/PostgreSQL. |
| RNF06 | Pendente | Não houve avaliação de usabilidade com usuários nem medição de interações. |
| RNF07 | Pendente | WCAG 2.1 AA não foi auditada. |
| RNF08 | Pendente | Compatibilidade de navegadores e viewport não foi testada. |
| RNF09 | Parcial | Camadas e testes foram ampliados; cobertura mínima de 60% não foi medida. |
| RNF10 | Parcial | Auditoria persistida sem expurgo automático; falta proteção imutável e política de backup/retention verificada. |
| RNF11 | Implementado | Container Linux/JVM Java 21; atende execução Linux com JVM superior ao mínimo. |

### Regras de negócio

| ID | Estado | Evidência / pendência |
|---|---|---|
| RGN01 | Implementado | Senha exige 8 caracteres, maiúscula, minúscula e número. |
| RGN02 | Pendente | Não existe integração para validar vínculo ativo com a Unifor. |
| RGN03 | Implementado | Um atendimento `AGENDADO` por aluno e área; encerrado/cancelado libera nova reserva. |
| RGN04 | Implementado | Cancelamento/remarcação pelo aluno exigem 24h; cancelamento posterior vira falta. |
| RGN05 | Parcial | Três faltas no semestre bloqueiam agendamento; não há diferenciação de justificadas nem calendário acadêmico. |
| RGN06 | Parcial | ADMIN não recebe prontuário; o modelo clínico ainda não existe e a agenda administrativa contém dados de atendimento. |
| RGN07 | Parcial | Cadastro exige aceite e registra data/versão; aprovação do termo institucional pendente. |
| RGN08 | Parcial | Contas são anonimizadas; atendimentos ficam preservados, mas retenção de cinco anos não é administrada por política de banco. |
| RGN09 | Implementado | Agendamento só aceita horário publicado/disponível. |
| RGN10 | Implementado | Reserva usa lock transacional; publicação de horários rejeita sobreposição sob lock do profissional. |
| RGN11 | Implementado | Telas de conversa exibem aviso de canal assíncrono e contatos CVV/SAMU. |
| RGN12 | Implementado | Conversas aluno-profissional exigem vínculo de atendimento atual ou anterior; terceiros são recusados. |
| RGN13 | Parcial | Prazo em dias úteis é calculado; feriados e sinalização automática de atraso não existem. |
| RGN14 | Parcial | Atendimentos finalizados não podem ser alterados e exclusão foi bloqueada; correções anexadas ao registro ainda não existem. |
| RGN15 | Pendente | Não existe geração de relatórios agregados/anonimizados. |
| RGN16 | Pendente | Calendário acadêmico, feriados e recessos não estão integrados. |
| RGN17 | Implementado | Cadastro de profissional não é público e serviços exigem perfil ADMIN. |
| RGN18 | Implementado | Finalização exige profissional responsável e horário de início atingido. |

## Testes automatizados desta revisão

Os testes cobrem serialização sem senha, recuperação de senha (expiração, tentativas, reenvio, falha de entrega, uso único e senhas iguais/diferentes), chamada HTTP simulada do Resend, autorização/origem, participação em conversas e validação/permissão de anexos. Eles são testes locais automatizados, não um teste de integração com Render, PostgreSQL provisionado ou Resend real.
