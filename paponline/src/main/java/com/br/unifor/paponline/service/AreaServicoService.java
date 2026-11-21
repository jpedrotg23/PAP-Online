package com.br.unifor.paponline.service;

import com.br.unifor.paponline.config.SessionContext;
import com.br.unifor.paponline.dto.AreaServicoRequest;
import com.br.unifor.paponline.entidades.AreaServico;
import com.br.unifor.paponline.repository.AreaServicoRepository;
import com.br.unifor.paponline.repository.AtendimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AreaServicoService {

    private final AreaServicoRepository repository;
    private final AtendimentoRepository atendimentoRepository;

    public List<AreaServico> listar() {
        return repository.findAll();
    }

    public AreaServico buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Área de serviço não encontrada"));
    }

    public AreaServico criar(AreaServicoRequest req) {
        exigirAdministrador();
        AreaServico a = new AreaServico();
        a.setNome(req.nome().trim().toUpperCase());
        a.setDescricao(req.descricao());
        return repository.save(a);
    }

    public AreaServico atualizar(Long id, AreaServicoRequest req) {
        exigirAdministrador();
        AreaServico a = buscar(id);
        a.setNome(req.nome().trim().toUpperCase());
        a.setDescricao(req.descricao());
        return repository.save(a);
    }

    public void excluir(Long id) {
        exigirAdministrador();
        AreaServico a = buscar(id);
        if (atendimentoRepository.existsByAreaId(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Área possui atendimentos vinculados e não pode ser excluída.");
        }
        repository.delete(a);
    }

    private void exigirAdministrador() {
        SessionContext.SessionInfo sessao = SessionContext.get();
        if (sessao == null || !"ADMIN".equals(sessao.perfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Somente administradores podem gerenciar áreas de serviço.");
        }
    }
}