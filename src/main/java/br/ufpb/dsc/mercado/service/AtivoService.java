package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Ativo;
import br.ufpb.dsc.mercado.dto.AtivoForm;
import br.ufpb.dsc.mercado.exception.AtivoNaoEncontradoException;
import br.ufpb.dsc.mercado.repository.AtivoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class AtivoService {

    private final AtivoRepository ativoRepository;
    private final LogAuditoriaService logAuditoriaService;

    public AtivoService(AtivoRepository ativoRepository, LogAuditoriaService logAuditoriaService) {
        this.ativoRepository = ativoRepository;
        this.logAuditoriaService = logAuditoriaService;
    }

    public Page<Ativo> listar(Pageable pageable) {
        return ativoRepository.findAll(pageable);
    }

    public Page<Ativo> buscar(String busca, Pageable pageable) {
        boolean temBusca = StringUtils.hasText(busca);

        if (temBusca) {
            return ativoRepository.findByNomeContainingIgnoreCase(busca.trim(), pageable);
        } else {
            return ativoRepository.findAll(pageable);
        }
    }

    public Ativo buscarPorId(Long id) {
        return ativoRepository.findById(id)
                .orElseThrow(() -> new AtivoNaoEncontradoException(id));
    }

    @Transactional
    public Ativo criar(AtivoForm form) {
        Ativo ativo = new Ativo(
                form.nome(),
                form.descricao(),
                form.numeroSerie(),
                form.status()
        );
        if (form.patrimonios() != null) {
            for (var pForm : form.patrimonios()) {
                if (org.springframework.util.StringUtils.hasText(pForm.codigo()) && org.springframework.util.StringUtils.hasText(pForm.numeroSerie())) {
                    ativo.addPatrimonio(new br.ufpb.dsc.mercado.domain.Patrimonio(pForm.codigo().trim(), pForm.numeroSerie().trim(), ativo));
                }
            }
        }
        Ativo salvo = ativoRepository.save(ativo);
        logAuditoriaService.registrar("CRIAR", "Ativo", salvo.getId(), "Criado ativo: " + salvo.getNome());
        return salvo;
    }

    @Transactional
    public Ativo atualizar(Long id, AtivoForm form) {
        Ativo ativo = buscarPorId(id);
        ativo.setNome(form.nome());
        ativo.setDescricao(form.descricao());
        ativo.setNumeroSerie(form.numeroSerie());
        ativo.setStatus(form.status());
        
        ativo.getPatrimonios().clear();
        if (form.patrimonios() != null) {
            for (var pForm : form.patrimonios()) {
                if (org.springframework.util.StringUtils.hasText(pForm.codigo()) && org.springframework.util.StringUtils.hasText(pForm.numeroSerie())) {
                    ativo.addPatrimonio(new br.ufpb.dsc.mercado.domain.Patrimonio(pForm.codigo().trim(), pForm.numeroSerie().trim(), ativo));
                }
            }
        }
        
        Ativo salvo = ativoRepository.save(ativo);
        logAuditoriaService.registrar("ATUALIZAR", "Ativo", salvo.getId(), "Atualizado ativo: " + salvo.getNome());
        return salvo;
    }

    @Transactional
    public void excluir(Long id) {
        Ativo ativo = ativoRepository.findById(id)
                .orElseThrow(() -> new AtivoNaoEncontradoException(id));
        logAuditoriaService.registrar("EXCLUIR", "Ativo", id, "Excluído ativo: " + ativo.getNome());
        ativoRepository.deleteById(id);
    }
}
