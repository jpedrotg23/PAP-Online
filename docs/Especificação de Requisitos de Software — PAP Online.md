# Especificação de Requisitos de Software — PAP Online

**Autores:** Arthur, João Pedro, Paulo, Suzani e Sálua

## Histórico de versões

| Data | Versão | Descrição | Autor |
| --- | --- | --- | --- |
| 26/09/2026 | 1.0 | Versão inicial do documento de requisitos | Arthur, João Pedro, Paulo, Suzani e Sálua |
| 08/11/2026 | 1.1 | Revisão geral: padronização para sistema web; requisitos funcionais e não funcionais complementados; regras de negócio, atores, casos de uso, arquitetura, glossário e modelos de análise | Arthur, João Pedro, Paulo, Suzani e Sálua |
| 24/11/2026 | 1.2 | Atualização para refletir a implementação: tecnologias (Java 21, Gradle, Docker, Render, PostgreSQL e Resend), status REALIZADO, entidades, arquitetura, estado de implementação (Apêndice C) e linhas de assinatura | Arthur, João Pedro, Paulo, Suzani e Sálua |

## 1. Introdução

Este documento contempla os requisitos necessários para o desenvolvimento do PAP Online, sistema web do Programa de Apoio Psicopedagógico (PAP) da Universidade de Fortaleza (Unifor). O sistema visa facilitar o acesso, a organização e o acompanhamento dos serviços oferecidos pelo PAP, promovendo maior eficiência no atendimento e melhor suporte à saúde mental e acadêmica dos estudantes.

### 1.1 Finalidade

A finalidade deste documento é apontar e organizar os requisitos funcionais e não funcionais envolvidos no planejamento do projeto. Os requisitos funcionais são o detalhamento das características e funções do sistema. Os requisitos não funcionais são os requisitos de sistema que não são detalhados imediatamente na especificação de caso de uso.

### 1.2 Escopo do Produto

O PAP Online é uma aplicação web, acessada por navegador, composta por ambientes voltados ao corpo discente, à equipe de profissionais do PAP e à gestão do programa. Seu propósito é centralizar e modernizar o ciclo de atendimento do programa: cadastro e acesso seguro, agendamento, remarcação e cancelamento de atendimentos, comunicação entre estudantes e profissionais, solicitação e acompanhamento de adaptações acadêmicas, envio de notificações e lembretes, registro e histórico dos atendimentos e emissão de relatórios gerenciais.

Ao automatizar fluxos operacionais e substituir processos manuais, o produto facilita o acesso ao suporte psicopedagógico, reduz o tempo de resposta às solicitações, diminui as faltas por meio de lembretes automáticos e consolida métricas sobre a demanda acadêmica, permitindo intervenções mais ágeis e assertivas, sempre preservando o sigilo das informações.

### 1.3 Referências

- LEI GERAL DE PROTEÇÃO DE DADOS (LGPD). Lei nº 13.709, de 14 de agosto de 2018. Disponível em: http://www.planalto.gov.br/ccivil\_03/\_ato2015-2018/2018/lei/l13709.htm.
- CONSELHO FEDERAL DE PSICOLOGIA (CFP). Resolução CFP nº 11/2018: regulamenta a prestação de serviços psicológicos realizados por meios de tecnologias da informação e da comunicação. Brasília, 2018.
- UNIVERSIDADE DE FORTALEZA (UNIFOR). Diretrizes do Programa de Apoio Psicopedagógico (PAP). Fortaleza, Unifor.
- UNIVERSIDADE DE FORTALEZA (UNIFOR). Manual de Aplicação da Marca e Identidade Visual. Fortaleza, Unifor.

## 2. Descrição Geral

### 2.1 Perspectiva do Produto

O PAP Online é um novo sistema web desenvolvido para facilitar o acesso, a organização e o acompanhamento dos serviços do Programa de Apoio Psicopedagógico (PAP) da Unifor. O projeto surgiu devido à dificuldade de realizar agendamentos e acompanhar os atendimentos. O sistema será acessado por navegador, com layout responsivo, e utilizará banco de dados relacional.

### 2.2 Funções do Produto

As principais funções previstas para o PAP Online são:

- Autenticação, cadastro e controle de acesso dos usuários por perfil;
- Agendamento, remarcação e cancelamento de atendimentos;
- Comunicação entre estudantes e profissionais do PAP;
- Solicitação e acompanhamento de adaptações acadêmicas;
- Registro e histórico dos atendimentos, com sigilo das informações;
- Painel de acompanhamento dos atendimentos e serviços;
- Notificações e lembretes automáticos;
- Gestão de profissionais, horários e avisos institucionais;
- Relatórios gerenciais com dados anonimizados.

### 2.3 Classes de Usuários e Características

**Estudantes**

- Utilizam o sistema para acessar os serviços do PAP;
- Realizam agendamentos e solicitam adaptações acadêmicas;
- Utilizam o canal de comunicação com os profissionais;
- Acompanham seus atendimentos e solicitações pelo painel.

**Profissionais do PAP**

- Gerenciam sua agenda, realizam o atendimento e registram o acompanhamento dos estudantes;
- Analisam as solicitações de adaptação;
- Utilizam o sistema para comunicação com os alunos;
- Na área de Psicologia, devem garantir a confidencialidade das informações registradas.

**Administrador/Gestor**

- Responsável pela gestão e planejamento das atividades do programa;
- Cadastra profissionais, define permissões e publica avisos institucionais;
- Utiliza informações agregadas do sistema para auxiliar na tomada de decisões.

### 2.4 Ambiente de Trabalho

O sistema é uma aplicação web, acessada por navegador de internet, e é executado por uma aplicação Java 21 com o framework Spring Boot, construída com Gradle. O banco de dados relacional varia conforme o ambiente: H2 em arquivo no profile local, H2 em memória no profile test e PostgreSQL no profile prod, com a conexão configurada por variáveis de ambiente. A aplicação é empacotada em Docker e implantada no Render, e o serviço Resend é usado para o envio de e-mails. O código é mantido no GitHub.

### 2.5 Design e Implementação: Restrições

- Utilização da linguagem Java 21 e do framework Spring Boot, com Gradle como ferramenta de build;
- Utilização de banco de dados relacional: H2 nos ambientes local e de teste e PostgreSQL em produção;
- Sistema acessado por navegador web, com as telas servidas pela própria aplicação;
- Empacotamento em Docker e implantação no Render;
- Utilização do serviço Resend para o envio de e-mails de recuperação de senha;
- Utilização do GitHub como repositório de código;
- Utilização do método Kanban via GitHub para gerenciamento do fluxo de desenvolvimento;
- Necessidade de garantir a confidencialidade das informações dos atendimentos psicológicos;
- Conformidade com a LGPD e com a Resolução CFP nº 11/2018.

### 2.6 Documentação do Usuário

Serão entregues, junto com a versão de produção, os seguintes artefatos:

| Artefato | Público | Formato |
| --- | --- | --- |
| Guia rápido do estudante | Estudantes | Página de ajuda dentro do sistema, exibida no primeiro acesso |
| Manual do profissional | Profissionais do PAP | PDF, cobrindo agenda, registro de atendimento e análise de adaptações |
| Manual do administrador | Setor administrativo | PDF, cobrindo gestão de profissionais, horários e relatórios |
| Perguntas frequentes (FAQ) | Todos | Página dentro do sistema |
| Documentação da API | Equipe técnica | Contrato da API (arquivo CONTRATO-BACKEND.md), com rotas, perfis de acesso e regras de segurança |
| README do repositório | Equipe técnica | Markdown no GitHub, com instruções de execução |

Todos os textos voltados ao usuário final serão redigidos em português brasileiro, em linguagem acessível.

### 2.7 Suposições e Dependências

**Suposições:** o PAP Online será acessado por meio de navegador web e utilizará banco de dados para armazenamento das informações. O desenvolvimento seguirá o cronograma estabelecido e contará com pesquisas e coleta de dados para o levantamento das informações necessárias. Presume-se ainda que a coordenação do PAP aprove o texto do termo de consentimento antes do uso real do sistema.

**Dependências:** o funcionamento do sistema depende de Java 21, Spring Boot, Gradle e de um banco de dados (PostgreSQL em produção), além do GitHub para o código, do Docker e do Render para a implantação e do serviço Resend para o envio de e-mails. Também depende da participação dos setores envolvidos no PAP para o fornecimento das informações necessárias e da validação institucional do vínculo dos estudantes.

## 3. Requisitos do Sistema

### 3.1 Requisitos Funcionais

| ID | Requisito | Descrição | Prioridade |
| --- | --- | --- | --- |
| RF01 | Cadastro | O usuário deve se cadastrar na plataforma informando nome, matrícula, vínculo e curso (estudantes), e-mail, telefone e senha, e aceitar o termo de consentimento, para ter acesso às funcionalidades. | ALTA |
| RF02 | Login | O usuário deve entrar na plataforma informando e-mail ou telefone e senha. | ALTA |
| RF03 | Enviar mensagem | O usuário Aluno pode enviar mensagem ao usuário Profissional, e vice-versa. | MÉDIA |
| RF04 | Receber mensagem | O sistema deve exibir, na aba de conversa, a mensagem enviada pelo outro usuário. | MÉDIA |
| RF05 | Notificação de evento institucional | O sistema deve enviar a todos os usuários as notificações dos eventos e avisos cadastrados pelo usuário Administrador. | MÉDIA |
| RF06 | Notificação ao aluno | O sistema deve enviar notificações ao aluno nas seguintes situações: confirmação de agendamento, lembrete 24h antes do atendimento e atualização de status de solicitação de adaptação. | MÉDIA |
| RF07 | Notificação ao profissional | O sistema deve enviar notificações ao profissional nas seguintes situações: novo agendamento em sua agenda, cancelamento de atendimento e nova solicitação de adaptação vinculada à sua área. | MÉDIA |
| RF08 | Agendamento | O usuário Aluno pode agendar um atendimento, escolhendo a área, o dia e um horário disponível. | ALTA |
| RF09 | Tela inicial | Após o login, o sistema deve exibir uma tela inicial com o próximo atendimento agendado (data, hora, profissional e modalidade), atalhos para agendar atendimento e solicitar adaptação, indicador de mensagens e notificações não lidas e os avisos institucionais mais recentes. O conteúdo varia conforme o perfil do usuário. | ALTA |
| RF10 | Logout | O usuário pode encerrar a sessão (logout). | ALTA |
| RF11 | Alterar telefone | O usuário pode alterar o telefone cadastrado. | MÉDIA |
| RF12 | Alterar e-mail | O usuário pode alterar o e-mail cadastrado. | MÉDIA |
| RF13 | Alterar senha | O usuário pode alterar a senha cadastrada. | ALTA |
| RF14 | Perfil | O usuário deve ter um perfil com foto, nome e e-mail. | ALTA |
| RF15 | Alterar perfil | O usuário pode alterar a foto e o nome. | MÉDIA |
| RF16 | Painel | O sistema deve ter um painel de acompanhamento dos atendimentos do usuário. | ALTA |
| RF17 | Excluir conta | O usuário pode excluir a conta criada no sistema. | ALTA |
| RF18 | Cancelar consulta | O usuário pode cancelar a consulta marcada. | ALTA |
| RF19 | Finalizar consulta | O usuário Profissional pode finalizar a consulta, passando-a ao status REALIZADO, quando ela terminar. | ALTA |
| RF20 | Descrição da consulta | A consulta deve ter uma descrição com horário, data, nome do profissional e tipo da consulta. | ALTA |
| RF21 | Histórico | O sistema deve ter uma tela de histórico com as consultas já realizadas pelo usuário. | ALTA |
| RF22 | Recuperar senha | O sistema deve permitir redefinir a senha por link ou código enviado ao e-mail cadastrado, com validade máxima de 30 minutos. | ALTA |
| RF23 | Remarcar consulta | O usuário Aluno pode remarcar um atendimento já agendado, respeitando a antecedência mínima da RGN04. | ALTA |
| RF24 | Solicitar adaptação | O usuário Aluno pode registrar uma solicitação de adaptação acadêmica, informando o tipo, a justificativa e anexando documentação comprobatória. | ALTA |
| RF25 | Acompanhar solicitação | O usuário Aluno pode consultar o status de suas solicitações (Enviada, Em análise, Deferida, Indeferida, Pendente de documentação). | ALTA |
| RF26 | Analisar solicitação | O usuário Profissional pode analisar as solicitações de adaptação vinculadas à sua área, alterando o status e registrando um parecer. | ALTA |
| RF27 | Gerenciar agenda | O usuário Profissional pode publicar, editar e bloquear horários de atendimento em sua agenda. | ALTA |
| RF28 | Registrar atendimento | O usuário Profissional pode registrar as informações do atendimento realizado, respeitando as regras RGN06 e RGN14. | ALTA |
| RF29 | Consultar histórico do aluno | O usuário Profissional pode consultar o histórico dos atendimentos dos estudantes sob sua responsabilidade. | ALTA |
| RF30 | Gerenciar profissionais | O usuário Administrador pode cadastrar, editar e inativar profissionais e definir suas permissões e áreas de atuação. | ALTA |
| RF31 | Gerar relatórios | O usuário Administrador pode gerar relatórios quantitativos por período, área, status e curso, exportáveis em PDF e CSV, respeitando a RGN15. | MÉDIA |
| RF32 | Registrar falta | O sistema deve registrar automaticamente como falta o atendimento não finalizado pelo profissional até 24 horas após o término do horário agendado, aplicando a RGN05. | MÉDIA |
| RF33 | Aceitar termo de consentimento | O sistema deve apresentar o Termo de Consentimento (LGPD) no cadastro e registrar o aceite com data, hora e versão do termo. | ALTA |
| RF34 | Registrar auditoria de atendimento | O sistema deve registrar, na entidade AuditoriaAtendimento, o usuário, a ação, o atendimento afetado e a data e hora de cada consulta ou alteração de atendimento, sem permitir a alteração ou a exclusão desses registros pela aplicação. | ALTA |

### 3.2 Requisitos Não Funcionais

| ID | Requisito | Descrição | Prioridade |
| --- | --- | --- | --- |
| RNF01 | Desempenho | As telas de consulta devem responder em até 3 segundos e as operações de gravação em até 5 segundos, com conexão de 10 Mbps e até 200 usuários simultâneos. | ALTA |
| RNF02 | Segurança na transmissão | Toda a comunicação entre cliente e servidor deve ocorrer sobre HTTPS com TLS 1.2 ou superior. | ALTA |
| RNF03 | Segurança na autenticação | O controle de acesso deve ser baseado em perfis (Estudante, Profissional, Administrador), com sessão expirando após 30 minutos de inatividade. Senhas devem ser armazenadas com hash e sal (BCrypt ou equivalente). | ALTA |
| RNF04 | Conformidade legal | O sistema deve estar em conformidade com a Lei nº 13.709/2018 (LGPD) e com a Resolução CFP nº 11/2018, incluindo registro de consentimento e direitos de acesso, correção e exclusão dos dados pessoais. | ALTA |
| RNF05 | Confiabilidade | O sistema deve manter disponibilidade mínima de 99% no horário de expediente da Unifor, com backup diário retido por 30 dias. | ALTA |
| RNF06 | Usabilidade | Um estudante sem treinamento prévio deve concluir um agendamento em no máximo 5 interações a partir da tela inicial. As mensagens de erro devem indicar a ação corretiva. | ALTA |
| RNF07 | Acessibilidade | As interfaces devem atender ao nível AA da WCAG 2.1, incluindo contraste mínimo, navegação por teclado e compatibilidade com leitores de tela. | MÉDIA |
| RNF08 | Compatibilidade | O sistema deve funcionar nas duas versões mais recentes do Chrome, Firefox, Edge e Safari, com layout responsivo a partir de 360px de largura. | ALTA |
| RNF09 | Manutenibilidade | O código deve seguir as convenções Java, adotar arquitetura em camadas (controller, service, repository) e manter cobertura mínima de 60% de testes unitários nas regras de negócio. | MÉDIA |
| RNF10 | Auditabilidade | Todo acesso a registros de atendimento deve ser gravado em log imutável, mantido por no mínimo 12 meses. | ALTA |
| RNF11 | Portabilidade e implantação | A aplicação deve ser executável com Java 21, empacotada em contêiner Docker, com a configuração do ambiente de produção fornecida por variáveis de ambiente, sem ajuste manual do sistema operacional. | MÉDIA |

### 3.3 Regras de Negócio

| ID | Requisito relacionado | Descrição |
| --- | --- | --- |
| RGN01 | RF01, RF13 | A senha deverá conter no mínimo 8 caracteres, entre letras maiúsculas, minúsculas e números. |
| RGN02 | RF01 | Apenas pessoas com vínculo ativo com a Unifor (graduação ou pós-graduação) podem se cadastrar como Estudante. O vínculo deve ser validado no cadastro. |
| RGN03 | RF08 | Cada estudante pode manter no máximo um atendimento agendado em aberto por área de serviço (Psicologia e Psicopedagogia contam separadamente). Novo agendamento na mesma área só é liberado após o atendimento anterior atingir o status REALIZADO, ser cancelado ou ser registrado como falta. |
| RGN04 | RF18, RF23 | O cancelamento e a remarcação pelo estudante devem ser feitos com antecedência mínima de 24 horas. Após esse prazo, o cancelamento é registrado como falta. |
| RGN05 | RF08, RF32 | O registro de 3 faltas não justificadas no mesmo semestre letivo suspende novos agendamentos pelo sistema. A liberação depende de contato com a coordenação do PAP. |
| RGN06 | RF28, RF29 | O conteúdo do registro de atendimento (prontuário) é visível exclusivamente ao profissional responsável. O setor administrativo acessa apenas dados de agenda, nunca o conteúdo clínico. Base: Resolução CFP nº 11/2018. |
| RGN07 | RF01, RF33 | O cadastro só é concluído após o aceite do Termo de Consentimento para tratamento de dados pessoais e de saúde (Lei nº 13.709/2018), registrado com data, hora e versão do termo. |
| RGN08 | RF17 | A exclusão de conta remove ou anonimiza os dados cadastrais, mas preserva os registros de atendimento por no mínimo 5 anos, conforme a obrigação de guarda de documentos do CFP. |
| RGN09 | RF08, RF27 | O estudante só pode agendar em horários previamente publicados pelo profissional na agenda do sistema. |
| RGN10 | RF08 | Um horário reservado fica bloqueado para os demais imediatamente após a confirmação. Não é permitido duplo agendamento no mesmo horário e profissional. |
| RGN11 | RF03, RF04 | O chat é um canal assíncrono de comunicação, não sendo canal de emergência nem de atendimento em tempo real. Essa informação deve ficar permanente na tela de conversa, junto com os contatos CVV (188) e SAMU (192). |
| RGN12 | RF03, RF04 | O chat só é habilitado entre um estudante e os profissionais que tenham ou tenham tido atendimento vinculado a ele. Não há chat entre estudantes. |
| RGN13 | RF26 | As solicitações de adaptação devem ser analisadas em até 5 dias úteis a partir do envio. O sistema deve sinalizar as que ultrapassarem o prazo. |
| RGN14 | RF19, RF28 | Após o atendimento atingir o status REALIZADO, o registro não pode ser editado nem excluído. Correções são feitas por novo registro anexado ao original, com data e autor. |
| RGN15 | RF31 | Relatórios gerenciais exibem apenas dados agregados e anonimizados. Nenhum relatório pode identificar estudantes individualmente. |
| RGN16 | RF27 | Os horários disponíveis respeitam o calendário acadêmico e o expediente da Unifor. Feriados e recessos bloqueiam a agenda automaticamente. |
| RGN17 | RF30 | O cadastro de profissionais e a atribuição de permissões são feitos exclusivamente pelo perfil Administrador. Não há autocadastro de profissional. |
| RGN18 | RF19 | O atendimento só pode ser marcado como REALIZADO pelo profissional responsável, e somente após o início do horário agendado. |

## 4. Casos de Uso

### 4.1 Atores

| ID | Ator | Descrição |
| --- | --- | --- |
| A01 | Estudante | Aluno com vínculo ativo na Unifor. Agenda, remarca e cancela atendimentos, utiliza o chat, solicita adaptações acadêmicas e acompanha seu histórico. |
| A02 | Profissional do PAP | Psicólogo ou psicopedagogo do programa. Gerencia a própria agenda, realiza e registra atendimentos, analisa solicitações de adaptação e se comunica com os estudantes. |
| A03 | Administrador/Gestor | Responsável pela gestão do programa. Cadastra profissionais, define permissões, acompanha a operação, publica avisos e gera relatórios gerenciais. |
| A04 | Sistema de Notificações | Ator secundário. Componente responsável pelo disparo automático de lembretes, confirmações e avisos. |

### 4.2 Casos de Uso

| ID | Ator | Caso de uso | Requisitos relacionados |
| --- | --- | --- | --- |
| UC01 | A01, A02, A03 | Realizar login | RF02, RF10, RF22 |
| UC02 | A01 (secundário: A04) | Agendar atendimento | RF08, RF06, RF20 |
| UC03 | A01 | Remarcar ou cancelar atendimento | RF18, RF23 |
| UC04 | A01, A02 | Utilizar chat | RF03, RF04 |
| UC05 | A01 (secundário: A04) | Solicitar adaptação | RF24, RF07 |
| UC06 | A01 | Acompanhar solicitação | RF25, RF06 |
| UC07 | A03 | Gerenciar profissionais e permissões | RF30 |
| UC08 | A02, A03 | Gerenciar horários e atendimentos | RF27, RF16 |
| UC09 | A02 | Registrar atendimento | RF19, RF28, RF32, RF34 |
| UC10 | A02 | Consultar histórico do aluno | RF29 |
| UC11 | A02 | Analisar solicitação de adaptação | RF26 |
| UC12 | A03 | Gerar relatórios | RF31 |

### 4.3 Diagrama de Casos de Uso

**Figura 1 — Diagrama de casos de uso do PAP Online.** \[Inserir aqui a imagem do diagrama de casos de uso.\]

## 5. Diagrama de Classes

**Figura 2 — Diagrama de classes do PAP Online.** \[Inserir aqui a imagem do diagrama de classes.\]

Entidades principais do domínio, conforme o código (os campos completos estão nas classes do pacote entidades):

- **Usuario** (base de Aluno, Profissional e Administrador): nome, e-mail, telefone, senha (guardada como hash), foto, ativo e data de cadastro.
- **Aluno:** matrícula, curso, vínculo e aceite do termo de consentimento (versão e data).
- **Profissional:** CRP, cargo e área de atuação.
- **Administrador:** cargo.
- **AreaServico:** nome e descrição.
- **HorarioDisponivel:** profissional, data, hora de início e de fim, disponível, bloqueado e motivo do bloqueio.
- **Atendimento:** aluno, profissional, área, horário, data e hora de início e de fim, modalidade, local e status (AGENDADO, REALIZADO, CANCELADO ou FALTA).
- **SolicitacaoAdaptacao:** aluno, área (opcional), protocolo, tipo, justificativa, status, data de envio, prazo do parecer e parecer.
- **Anexo:** documento enviado em uma solicitação de adaptação.
- **Conversa** e **Mensagem:** troca de mensagens entre aluno e profissional.
- **Notificacao:** aviso individual ao usuário, com indicador de leitura.
- **AvisoInstitucional:** comunicado publicado pelo administrador, com categoria, público-alvo e status.
- **AuditoriaAtendimento:** registro de acessos e alterações em atendimentos, protegido contra alteração e exclusão pela aplicação.
- **TokenRecuperacaoSenha:** código de recuperação de senha, guardado como hash.
- **RegistroAtendimento** (prevista, ainda não implementada): conteúdo clínico do atendimento (RF28).

## 6. Arquitetura do Projeto

A arquitetura do projeto PAP Online será baseada no padrão MVC (Model, View, Controller), amplamente adotado em aplicações Java com o framework Spring Boot. Essa arquitetura divide a aplicação em três camadas:

- **Modelo:** representa a camada que contém os processos de negócio da aplicação, encapsulando dados e comportamentos independentemente da apresentação. No PAP Online, essa camada aplica as regras de negócio (RGN01 a RGN18), como o controle de faltas, a confidencialidade do prontuário e as regras de agendamento, e persiste os dados no banco por meio do Spring Data JPA.
- **Visão:** representa basicamente a interface com o usuário. É utilizada para receber a entrada dos dados e apresentar o resultado. No PAP Online, corresponde à interface web acessada pelo navegador, adaptada a cada perfil: Estudante, Profissional do PAP e Administrador.
- **Controle:** representa a forma como a aplicação reage aos estímulos do usuário e determina o fluxo de apresentação, servindo de camada intermediária entre a Visão e o Modelo. No PAP Online, é implementada pelos Controllers do Spring, que recebem as requisições HTTP, as encaminham ao Modelo e devolvem a resposta processada.

O sistema é desenvolvido como uma aplicação única (monólito): as três camadas convivem no mesmo projeto e são implantadas juntas. O código é organizado em pacotes por camada (config, controller, dto, entidades, repository, service e util), com um serviço para cada domínio (usuário, atendimento, horário, solicitação de adaptação, conversa, notificação, aviso e relatório). Tecnicamente, a camada de Modelo se divide em Service (regras de negócio) e Repository (persistência com Spring Data JPA), e as rotas de usuário e de relatórios trocam dados com a Visão por meio de DTOs, sem expor as entidades. O front-end é composto por páginas HTML estáticas servidas pela própria aplicação. O banco de dados varia por profile: H2 em arquivo no ambiente local, H2 em memória nos testes e PostgreSQL em produção, com a conexão configurada por variáveis de ambiente. A aplicação é empacotada em Docker e implantada no Render, e o Resend envia os e-mails.

**Figura 3 — Arquitetura do PAP Online.** \[Inserir aqui a imagem da arquitetura.\]

## 7. Apêndice A: Glossário

| Termo / Sigla | Definição |
| --- | --- |
| Acolhimento | Primeiro contato do estudante com o PAP, no qual a demanda é escutada e encaminhada. |
| Adaptação acadêmica | Ajuste em prazos, formatos de avaliação ou condições de ensino concedido a estudantes com necessidades específicas. |
| Ator | Pessoa, sistema ou componente que interage com o sistema em um caso de uso. |
| Atendimento | Sessão realizada entre um estudante e um profissional do PAP. |
| CFP | Conselho Federal de Psicologia. |
| Caso de uso (UC) | Descrição de uma sequência de interações entre um ator e o sistema para atingir um objetivo. |
| Dado pessoal sensível | Conforme a LGPD, dado sobre saúde, origem racial, convicção religiosa e outros, sujeito a proteção reforçada. |
| Estudante | Aluno com vínculo ativo na Unifor, usuário principal do sistema. |
| GitHub | Plataforma de hospedagem de código com controle de versão. |
| Kanban | Método visual de gestão de fluxo de trabalho, organizado em colunas por etapa. |
| LGPD | Lei Geral de Proteção de Dados, Lei nº 13.709/2018. |
| MVC | Padrão de arquitetura que separa a aplicação em Modelo, Visão e Controle. |
| PAP | Programa de Apoio Psicopedagógico da Universidade de Fortaleza. |
| PAP Online | Sistema objeto desta especificação. |
| Prontuário | Conjunto de registros do acompanhamento de um estudante, de guarda obrigatória pelo profissional. |
| Regra de negócio (RGN) | Restrição ou política da organização que o sistema deve respeitar. |
| Requisito funcional (RF) | Função que o sistema deve executar. |
| Requisito não funcional (RNF) | Atributo de qualidade ou restrição que o sistema deve atender. |
| Responsivo | Interface que adapta seu layout ao tamanho da tela do dispositivo. |
| Spring Boot | Framework Java para construção de aplicações web e APIs. |
| SRS | Software Requirements Specification. Este documento. |
| Status do atendimento | Situação do atendimento: AGENDADO, REALIZADO, CANCELADO ou FALTA. |
| Unifor | Universidade de Fortaleza. |
| WCAG | Web Content Accessibility Guidelines, diretrizes internacionais de acessibilidade web. |
| Docker | Tecnologia que empacota a aplicação e suas dependências em um contêiner executável. |
| Gradle | Ferramenta de build e de gerenciamento de dependências do projeto Java. |
| H2 | Banco de dados relacional leve, usado em arquivo no ambiente local e em memória nos testes. |
| PostgreSQL | Banco de dados relacional usado no ambiente de produção. |
| Profile | Conjunto de configurações do Spring para cada ambiente: local, test e prod. |
| REALIZADO | Status do atendimento já efetuado e finalizado pelo profissional responsável. |
| Render | Plataforma de nuvem na qual a aplicação é implantada. |
| Resend | Serviço de envio de e-mails, usado na recuperação de senha. |

## 8. Apêndice B: Modelos de Análise

| Modelo | Finalidade |
| --- | --- |
| Diagrama de casos de uso (UML) | Visão geral das funcionalidades e dos atores (Figura 1) |
| Diagrama de classes (UML) | Estrutura das entidades e relacionamentos do domínio (Figura 2) |
| Diagrama entidade-relacionamento | Modelagem do banco de dados |
| Diagrama de estados do atendimento | Transições de status do atendimento |
| Diagrama de estados da solicitação de adaptação | Transições de status da solicitação |

**Estados do atendimento:** AGENDADO, que pode passar para REALIZADO, CANCELADO (pelo aluno ou pelo profissional) ou FALTA (automático, 24 horas após o término do atendimento sem finalização).

**Estados da solicitação de adaptação:** Enviada, que passa para Em análise; de Em análise, pode ir para Deferida, Indeferida ou Pendente de documentação; de Pendente de documentação, retorna para Em análise.

## 9. Apêndice C: Estado de implementação

A tabela resume o que o código entrega hoje, com base no arquivo CONTRATO-BACKEND.md. Os requisitos deste documento continuam valendo; os marcados como Parcial ou Pendente ficam para as próximas etapas.

| Tipo | Implementado | Parcial | Pendente |
| --- | --- | --- | --- |
| Requisitos funcionais (34) | RF04, RF10, RF13, RF19, RF25, RF33 | RF01, RF02, RF03, RF05, RF06, RF07, RF08, RF09, RF11, RF12, RF14, RF15, RF16, RF17, RF18, RF20, RF21, RF22, RF23, RF24, RF26, RF27, RF29, RF30, RF31, RF32, RF34 | RF28 |
| Requisitos não funcionais (11) | RNF03, RNF11 | RNF02, RNF04, RNF09, RNF10 | RNF01, RNF05, RNF06, RNF07, RNF08 |
| Regras de negócio (18) | RGN01, RGN03, RGN04, RGN07, RGN09, RGN10, RGN11, RGN12, RGN15, RGN17, RGN18 | RGN05, RGN06, RGN08, RGN13, RGN14 | RGN02, RGN16 |

Principais lacunas:

| Item | Lacuna |
| --- | --- |
| RF28, RGN14 | Não existe registro clínico do atendimento: faltam a entidade, a tela de registro e as correções por novo registro. |
| RF29 | O profissional consulta os atendimentos vinculados, mas não há histórico com prontuário. |
| RF31 | A exportação é apenas em CSV, com filtros de período e área; faltam PDF e filtros por status e curso. |
| RF14, RF15 | O perfil tem nome e foto por URL; falta o envio de imagem. |
| RF17, RGN08 | A conta é anonimizada, mas faltam o fluxo completo de direitos da LGPD e a política de retenção de cinco anos no banco. |
| RGN02 | Não há integração com a Unifor para validar o vínculo ativo. |
| RGN05 | Não diferencia faltas justificadas nem usa o calendário acadêmico. |
| RGN13, RGN16 | O prazo em dias úteis não considera feriados e não há sinalização automática de atraso; o calendário de feriados e recessos não existe. |
| RF24, RF26 | A área da solicitação é opcional, e falta o fluxo de atribuição pela coordenação. |
| RNF01, RNF05 a RNF08 | Não há medições de desempenho, usabilidade, acessibilidade e compatibilidade, nem disponibilidade e backup comprovados. |
| RNF02 | O HTTPS depende do Render e do proxy; a execução local não força TLS. |
| RNF09 | Há testes automatizados, mas a cobertura mínima de 60% não foi medida. |
| RF33, RGN07 | O texto do termo é provisório e aguarda aprovação da coordenação; o encarregado ainda está por preencher. |
| Banco de dados | O schema de produção é criado pelo Hibernate, sem migrations versionadas; deve ser revisado antes de guardar dados reais. |

## 10. Aprovação

Assinatura: \_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_    Data: \_\_\_\_/\_\_\_\_/\_\_\_\_\_\_

**Cliente**

Nome: \<Nome do Cliente>

Assinatura: \_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_    Data: \_\_\_\_/\_\_\_\_/\_\_\_\_\_\_

**Gerente do Projeto**

Nome: \<Nome do Gerente do Projeto>
