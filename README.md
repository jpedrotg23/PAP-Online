<div align="center">

# PAP Online

### Programa de Apoio Psicopedagógico da Universidade de Fortaleza

<p>
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white">
  <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
</p>

<p>
  <img src="https://img.shields.io/badge/Status-Em_Desenvolvimento-F59E0B?style=flat-square">
</p>

</div>

---

## 🎯 Objetivo

O **PAP Online** é um sistema web desenvolvido como proposta de digitalização e modernização do acesso aos serviços do **Programa de Apoio Psicopedagógico (PAP) da Universidade de Fortaleza (Unifor)**.

A proposta surgiu a partir da percepção de que muitos estudantes podem não conhecer os serviços oferecidos pelo PAP ou podem encontrar dificuldades para descobrir como solicitar atendimento, acompanhar seus processos e entrar em contato com a equipe responsável.

O sistema busca centralizar essas informações e serviços em uma plataforma web simples, acessível e organizada, aproximando o PAP dos estudantes e facilitando o trabalho dos profissionais responsáveis pelos atendimentos.

> 🚧 **Status: Em desenvolvimento. O frontend está publicado; o backend ainda será iniciado pela equipe responsável.**

---

## 🛠️ Tecnologias e arquitetura planejada

O projeto está dividido em duas partes:

| Camada | Tecnologias | Estado |
|:---|:---|:---|
| **Frontend** | HTML5, CSS3 e JavaScript | Implementado e publicado |
| **Backend** | Java, Spring Boot e Spring Web | Planejado; desenvolvimento ainda não iniciado |

A equipe de backend utilizará Java com Spring Boot e Spring Web para disponibilizar uma API HTTP/REST consumida pelo frontend. O uso de Spring Security é uma opção recomendada para a implementação de autenticação e autorização, mas deverá ser confirmada pela equipe de backend.

**Importante:** Java, Spring Boot e Spring Web representam a stack definida para o backend. Eles ainda não estão implementados neste repositório. O código atualmente disponível na pasta `PAP-Online/` é o frontend estático em HTML, CSS e JavaScript.

## 🌐 Acessar o frontend

- **Demonstração publicada:** https://jpedrotg23.github.io/PAP-Online/
- **Repositório:** https://github.com/jpedrotg23/PAP-Online
- **Documentação de integração:** [PAP-Online/CONTRATO-BACKEND.md](PAP-Online/CONTRATO-BACKEND.md)

O modo demonstrativo permite navegar pelas áreas de aluno, profissional e administrador sem autenticação real. Ele serve somente para apresentação do frontend e não deve ser utilizado para dados pessoais ou operações reais.

---

## 💡 Por que o PAP Online é viável?

A proposta busca solucionar um problema presente no ambiente universitário: a dificuldade de tornar os serviços de apoio psicopedagógico conhecidos e acessíveis aos estudantes.

Uma plataforma web pode funcionar como um **ponto central de acesso ao PAP**, reunindo informações e serviços em um único local.

Além de facilitar os processos já existentes, o sistema pode atuar como uma ferramenta de **divulgação institucional**, aumentando a presença do PAP no cotidiano dos estudantes.

---

## 🌱 Visibilidade do PAP

Um dos principais diferenciais da proposta é utilizar o sistema não apenas para gerenciar atendimentos, mas também para **aumentar a visibilidade do PAP dentro da Universidade de Fortaleza**.

O PAP Online poderá apresentar, de forma clara e acessível:

- O que é o PAP;
- Qual é a finalidade do programa;
- Quais serviços estão disponíveis;
- Quem pode utilizar os serviços;
- Como solicitar atendimento;
- Como funciona o acompanhamento;
- Como entrar em contato;
- Perguntas frequentes;
- Orientações gerais.

Dessa forma, o sistema pode funcionar como uma **porta de entrada para o PAP**, permitindo que mais estudantes descubram os serviços oferecidos pela universidade.

---

## 🌎 Impacto na sociedade

O PAP Online busca contribuir para uma comunidade acadêmica mais **informada, inclusiva e acolhedora**.

O sistema pode contribuir para:

- Facilitar o acesso ao apoio psicopedagógico;
- Aumentar a divulgação dos serviços do PAP;
- Reduzir dificuldades no processo de solicitação de atendimento;
- Promover maior autonomia aos estudantes;
- Facilitar o acompanhamento dos atendimentos;
- Contribuir para uma universidade mais inclusiva;
- Aproximar os estudantes dos profissionais responsáveis pelo atendimento.

O sistema **não pretende substituir os profissionais do PAP**. Seu objetivo é facilitar o acesso aos serviços e melhorar a comunicação entre estudantes e equipe.

---

## 👥 Usuários do sistema

### 👨‍🎓 Estudante

- Acessar informações sobre o PAP;
- Solicitar atendimento;
- Visualizar profissionais disponíveis;
- Consultar horários;
- Agendar atendimento;
- Consultar histórico de atendimentos;
- Acompanhar solicitações;
- Solicitar adaptações acadêmicas;
- Enviar documentos;
- Entrar em contato com profissionais.

### 🧑‍⚕️ Profissional do PAP

- Visualizar sua agenda;
- Consultar atendimentos;
- Acessar informações dos alunos vinculados;
- Registrar orientações;
- Acompanhar solicitações de adaptação;
- Comunicar-se com estudantes.

### 🧑‍💼 Administrador/Gestor

- Gerenciar estudantes;
- Gerenciar profissionais;
- Organizar horários;
- Acompanhar atendimentos;
- Gerenciar solicitações;
- Acompanhar informações administrativas;
- Visualizar indicadores do PAP.

---

## ⚙️ Funcionalidades previstas

### 🔐 Autenticação

- Login de usuários;
- Controle de acesso de acordo com o perfil;
- Validação das credenciais;
- Recuperação de acesso.

### 📅 Atendimento

- Solicitação de atendimento;
- Visualização de profissionais;
- Visualização de horários disponíveis;
- Agendamento;
- Cancelamento ou alteração de atendimento;
- Consulta ao histórico.

### 🏢 Gestão

- Cadastro de profissionais;
- Edição de profissionais;
- Gerenciamento de horários;
- Visualização dos atendimentos;
- Gerenciamento de usuários.

### ♿ Solicitações de adaptação

- Criação de solicitação;
- Descrição da necessidade do estudante;
- Envio de documentação;
- Acompanhamento do status;
- Análise por profissional ou gestor.

### 💬 Comunicação

- Canal de comunicação entre estudante e equipe;
- Histórico de mensagens;
- Organização das conversas por usuário.

### 📢 Informações sobre o PAP

- O que é o PAP;
- Serviços oferecidos;
- Público atendido;
- Como solicitar atendimento;
- Como entrar em contato;
- Perguntas frequentes;
- Orientações gerais.

---

## 📚 Contexto acadêmico

O PAP Online está sendo desenvolvido como um projeto acadêmico envolvendo estudantes da área de tecnologia, tendo como foco a aplicação de conceitos de desenvolvimento de sistemas e solução de problemas reais dentro do ambiente universitário.

A proposta busca demonstrar como uma solução digital pode ser utilizada para melhorar o acesso dos estudantes a serviços institucionais e contribuir para uma experiência universitária mais inclusiva.

---

## 👨‍💻 Equipe

| Nome | Curso |
|---|---|
| **João Pedro Teixeira Guimarães** | Ciência da Computação |
| **Suzani Jia Yi Wang** | Ciência da Computação |
| **Arthur Braz Masrua** | Ciência da Computação |
| **Paulo Haskley Queiroz de Lima** | Análise e Desenvolvimento de Sistemas |

---

## 📌 Considerações finais

O **PAP Online** parte de uma ideia simples:

> **Tornar o apoio psicopedagógico mais fácil de encontrar e acessar.**

Mais do que criar uma plataforma para agendamento, a proposta é utilizar a tecnologia para **aproximar o PAP da comunidade acadêmica e aumentar a visibilidade dos serviços oferecidos pela universidade**.

Uma plataforma centralizada pode permitir que estudantes descubram o PAP, entendam quais serviços estão disponíveis e encontrem um caminho mais simples para buscar apoio.

O projeto está em desenvolvimento e poderá evoluir conforme os requisitos e necessidades identificados durante sua construção.

---

<div align="center">

### PAP Online

**Tecnologia aproximando estudantes do apoio que eles precisam.**

<br>

<img src="https://img.shields.io/badge/Made_with-Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Made with Java">
<img src="https://img.shields.io/badge/Powered_by-Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Powered by Spring Boot">

</div>
