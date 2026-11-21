package com.br.unifor.paponline.service;

import com.br.unifor.paponline.config.SessionContext;
import com.br.unifor.paponline.entidades.Anexo;
import com.br.unifor.paponline.entidades.SolicitacaoAdaptacao;
import com.br.unifor.paponline.entidades.Profissional;
import com.br.unifor.paponline.repository.AnexoRepository;
import com.br.unifor.paponline.repository.ProfissionalRepository;
import com.br.unifor.paponline.repository.SolicitacaoAdaptacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.InvalidPathException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnexoService {

    private static final long MAX_BYTES = 10L * 1024 * 1024;
    private final AnexoRepository repository;
    private final SolicitacaoAdaptacaoRepository solicitacaoRepository;
    private final ProfissionalRepository profissionalRepository;

    @Value("${pap.upload-dir:uploads}")
    private String pastaUploads;

    @Transactional
    public Anexo salvar(Long solicitacaoId, MultipartFile arquivo) {
        SessionContext.SessionInfo sessao = exigirSessao();
        SolicitacaoAdaptacao solicitacao = buscarSolicitacao(solicitacaoId);

        if ("ALUNO".equals(sessao.perfil())) {
            if (!Objects.equals(solicitacao.getAlunoId(), sessao.userId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Você não tem permissão para anexar arquivos nesta solicitação.");
            }
            if (!"ENVIADA".equals(solicitacao.getStatus())
                    && !"PENDENTE_DOCUMENTACAO".equals(solicitacao.getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Só é possível anexar arquivos enquanto a solicitação estiver ENVIADA ou PENDENTE_DOCUMENTACAO.");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Somente o aluno responsável pode anexar documentos à solicitação.");
        }

        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Envie um arquivo PDF, JPG ou PNG.");
        }

        byte[] conteudo;
        try {
            conteudo = arquivo.getBytes();
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não foi possível ler o arquivo enviado.");
        }
        String tipoMime = validarArquivo(arquivo, conteudo);

        String nomeOriginal = arquivo.getOriginalFilename() != null
                ? arquivo.getOriginalFilename().replaceAll("[\\\\/]+", "_")
                : "arquivo";

        String extensao = "";
        int ponto = nomeOriginal.lastIndexOf('.');
        if (ponto >= 0) {
            extensao = nomeOriginal.substring(ponto);
        }
        String extensaoSegura = switch (tipoMime) {
            case "application/pdf" -> ".pdf";
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato não permitido.");
        };
        String nomeArmazenado = UUID.randomUUID() + extensaoSegura;

        Path pasta = Paths.get(pastaUploads).toAbsolutePath().normalize();
        try {
            Files.createDirectories(pasta);
            Path destino = pasta.toRealPath().resolve(nomeArmazenado);
            if (!destino.normalize().startsWith(pasta.toRealPath())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Caminho de armazenamento inválido.");
            }
            Files.write(destino, conteudo, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

            Anexo anexo = new Anexo();
            anexo.setSolicitacaoId(solicitacaoId);
            anexo.setNomeArquivo(nomeOriginal.length() > 255 ? nomeOriginal.substring(0, 255) : nomeOriginal);
            anexo.setCaminho(destino.toString());
            anexo.setTipoMime(tipoMime);
            anexo.setTamanho(arquivo.getSize());
            return repository.save(anexo);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível armazenar o arquivo.");
        }
    }

    public List<Anexo> listarPorSolicitacao(Long solicitacaoId) {
        SessionContext.SessionInfo sessao = exigirSessao();
        SolicitacaoAdaptacao solicitacao = buscarSolicitacao(solicitacaoId);

        if ("ALUNO".equals(sessao.perfil())
                && !Objects.equals(solicitacao.getAlunoId(), sessao.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não tem permissão para visualizar os anexos desta solicitação.");
        }
        if ("PROFISSIONAL".equals(sessao.perfil())) exigirResponsavel(sessao, solicitacao);
        if (!"ALUNO".equals(sessao.perfil()) && !"PROFISSIONAL".equals(sessao.perfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Perfil sem permissão para visualizar documentos da solicitação.");
        }
        return repository.findBySolicitacaoId(solicitacaoId);
    }

    public AnexoDownload obterParaDownload(Long solicitacaoId, Long anexoId) {
        SessionContext.SessionInfo sessao = exigirSessao();

        Anexo anexo = repository.findById(anexoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Anexo não encontrado."));
        if (!Objects.equals(anexo.getSolicitacaoId(), solicitacaoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Anexo não encontrado.");
        }

        SolicitacaoAdaptacao solicitacao = buscarSolicitacao(solicitacaoId);
        if ("ALUNO".equals(sessao.perfil())) {
            if (!Objects.equals(solicitacao.getAlunoId(), sessao.userId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Você não tem permissão para acessar este anexo.");
            }
        } else if ("PROFISSIONAL".equals(sessao.perfil())) {
            exigirResponsavel(sessao, solicitacao);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Perfil sem permissão para acessar documentos da solicitação.");
        }

        Path raiz;
        Path caminho;
        try {
            raiz = Paths.get(pastaUploads).toAbsolutePath().normalize().toRealPath();
            caminho = Paths.get(anexo.getCaminho()).toAbsolutePath().normalize().toRealPath();
        } catch (InvalidPathException | IOException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Anexo não encontrado.");
        }
        if (!caminho.startsWith(raiz)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Anexo não encontrado.");
        }
        Resource resource = new FileSystemResource(caminho);
        if (!resource.exists() || !resource.isReadable() || !Files.isRegularFile(caminho)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "O arquivo solicitado não está disponível no servidor.");
        }
        return new AnexoDownload(anexo, resource);
    }

    private String validarArquivo(MultipartFile arquivo, byte[] conteudo) {
        if (arquivo.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O arquivo excede o limite de 10 MB.");
        }
        String tipoDetectado = detectarMime(conteudo);
        String tipoDeclarado = arquivo.getContentType();

        String nome = arquivo.getOriginalFilename() != null
                ? arquivo.getOriginalFilename().toLowerCase()
                : "";
        boolean extOk = ("application/pdf".equals(tipoDetectado) && nome.endsWith(".pdf"))
                || ("image/jpeg".equals(tipoDetectado)
                && (nome.endsWith(".jpg") || nome.endsWith(".jpeg")))
                || ("image/png".equals(tipoDetectado) && nome.endsWith(".png"));

        if (!extOk || tipoDeclarado == null || !tipoDetectado.equalsIgnoreCase(tipoDeclarado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato não permitido. Envie apenas PDF, JPG ou PNG.");
        }
        return tipoDetectado;
    }

    private String detectarMime(byte[] conteudo) {
        if (conteudo.length >= 5
                && conteudo[0] == '%' && conteudo[1] == 'P' && conteudo[2] == 'D'
                && conteudo[3] == 'F' && conteudo[4] == '-') return "application/pdf";
        if (conteudo.length >= 3 && (conteudo[0] & 0xff) == 0xff
                && (conteudo[1] & 0xff) == 0xd8 && (conteudo[2] & 0xff) == 0xff) return "image/jpeg";
        if (conteudo.length >= 8 && (conteudo[0] & 0xff) == 0x89
                && conteudo[1] == 'P' && conteudo[2] == 'N' && conteudo[3] == 'G'
                && (conteudo[4] & 0xff) == 0x0d && (conteudo[5] & 0xff) == 0x0a
                && (conteudo[6] & 0xff) == 0x1a && (conteudo[7] & 0xff) == 0x0a) return "image/png";
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "O conteúdo não corresponde a um PDF, JPG ou PNG válido.");
    }

    private SolicitacaoAdaptacao buscarSolicitacao(Long solicitacaoId) {
        return solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Solicitação não encontrada."));
    }

    private SessionContext.SessionInfo exigirSessao() {
        SessionContext.SessionInfo sessao = SessionContext.get();
        if (sessao == null || sessao.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Sua sessão expirou. Entre novamente no PAP Online.");
        }
        return sessao;
    }

    private void exigirResponsavel(SessionContext.SessionInfo sessao, SolicitacaoAdaptacao solicitacao) {
        Profissional profissional = profissionalRepository.findById(sessao.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Profissional não encontrado."));
        boolean atribuido = Objects.equals(solicitacao.getProfissionalId(), sessao.userId());
        boolean mesmaArea = solicitacao.getAreaId() != null
                && Objects.equals(solicitacao.getAreaId(), profissional.getAreaId());
        if (!atribuido && !mesmaArea) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não está autorizado a acessar os documentos desta solicitação.");
        }
    }

    public record AnexoDownload(Anexo anexo, Resource resource) {
    }
}