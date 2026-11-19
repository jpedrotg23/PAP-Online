package com.br.unifor.paponline.service;

import com.br.unifor.paponline.config.SessionContext;
import com.br.unifor.paponline.entidades.Anexo;
import com.br.unifor.paponline.entidades.SolicitacaoAdaptacao;
import com.br.unifor.paponline.repository.AnexoRepository;
import com.br.unifor.paponline.repository.SolicitacaoAdaptacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnexoService {

    private static final long MAX_BYTES = 10L * 1024 * 1024;
    private static final String PASTA_UPLOADS = "uploads";

    private static final String TIPO_PDF = "application/pdf";
    private static final String TIPO_JPG = "image/jpeg";
    private static final String TIPO_PNG = "image/png";

    private final AnexoRepository repository;
    private final SolicitacaoAdaptacaoRepository solicitacaoRepository;

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
        } else if (!"PROFISSIONAL".equals(sessao.perfil()) && !"ADMIN".equals(sessao.perfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Perfil sem permissão para anexar arquivos nesta solicitação.");
        }

        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Envie um arquivo PDF, JPG ou PNG.");
        }

        validarArquivo(arquivo);

        String nomeOriginal = arquivo.getOriginalFilename() != null
                ? arquivo.getOriginalFilename().replaceAll("[\\\\/]+", "_")
                : "arquivo";

        String extensao = "";
        int ponto = nomeOriginal.lastIndexOf('.');
        if (ponto >= 0) {
            extensao = nomeOriginal.substring(ponto);
        }
        String nomeArmazenado = UUID.randomUUID().toString() + extensao;

        Path pasta = Paths.get(PASTA_UPLOADS).toAbsolutePath().normalize();
        try {
            Files.createDirectories(pasta);
            Path destino = pasta.resolve(nomeArmazenado);
            try (var in = arquivo.getInputStream()) {
                Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
            }

            Anexo anexo = new Anexo();
            anexo.setSolicitacaoId(solicitacaoId);
            anexo.setNomeArquivo(nomeOriginal);
            anexo.setCaminho(destino.toString());
            anexo.setTipoMime(arquivo.getContentType());
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

        if ("ALUNO".equals(sessao.perfil())) {
            SolicitacaoAdaptacao solicitacao = buscarSolicitacao(solicitacaoId);
            if (!Objects.equals(solicitacao.getAlunoId(), sessao.userId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Você não tem permissão para acessar este anexo.");
            }
        }

        Path caminho = Paths.get(anexo.getCaminho());
        Resource resource = new FileSystemResource(caminho);
        if (!resource.exists() || !resource.isReadable()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "O arquivo solicitado não está disponível no servidor.");
        }
        return new AnexoDownload(anexo, resource);
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O arquivo excede o limite de 10 MB.");
        }
        String tipoMime = arquivo.getContentType();
        boolean mimeOk = TIPO_PDF.equalsIgnoreCase(tipoMime)
                || TIPO_JPG.equalsIgnoreCase(tipoMime)
                || TIPO_PNG.equalsIgnoreCase(tipoMime);

        String nome = arquivo.getOriginalFilename() != null
                ? arquivo.getOriginalFilename().toLowerCase()
                : "";
        boolean extOk = nome.endsWith(".pdf") || nome.endsWith(".jpg")
                || nome.endsWith(".jpeg") || nome.endsWith(".png");

        if (!mimeOk || !extOk) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato não permitido. Envie apenas PDF, JPG ou PNG.");
        }
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

    public record AnexoDownload(Anexo anexo, Resource resource) {
    }
}