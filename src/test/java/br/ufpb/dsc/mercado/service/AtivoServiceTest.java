package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Ativo;
import br.ufpb.dsc.mercado.dto.AtivoForm;
import br.ufpb.dsc.mercado.exception.AtivoNaoEncontradoException;
import br.ufpb.dsc.mercado.repository.AtivoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtivoService — Testes Unitários")
class AtivoServiceTest {

    @Mock
    private AtivoRepository ativoRepository;

    @InjectMocks
    private AtivoService ativoService;

    private Ativo ativoExistente;
    private AtivoForm formValido;

    @BeforeEach
    void setUp() {
        ativoExistente = new Ativo("Notebook Dell", "XPS 13", new BigDecimal("7999.99"), 1, null, "SN-DELL-1234", "ATIVO");
        ativoExistente.setId(1L);

        formValido = new AtivoForm("Notebook Thinkpad", "T14 Gen 3", new BigDecimal("6500.00"), 2, null, "SN-THINK-5678", "ATIVO");
    }

    @Test
    @DisplayName("buscarPorId: deve retornar ativo quando ID existe")
    void buscarPorId_quandoIdExiste_deveRetornarAtivo() {
        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativoExistente));

        Ativo resultado = ativoService.buscarPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNome()).isEqualTo("Notebook Dell");

        verify(ativoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("buscarPorId: deve lançar exceção quando ID não existe")
    void buscarPorId_quandoIdNaoExiste_deveLancarExcecao() {
        when(ativoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ativoService.buscarPorId(99L))
                .isInstanceOf(AtivoNaoEncontradoException.class)
                .hasMessageContaining("99");

        verify(ativoRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("criar: deve salvar e retornar o novo ativo")
    void criar_comFormValido_deveSalvarERetornarAtivo() {
        Ativo ativoSalvo = new Ativo(formValido.nome(), formValido.descricao(), formValido.preco(), formValido.quantidade(), null, formValido.numeroSerie(), formValido.status());
        ativoSalvo.setId(2L);
        when(ativoRepository.save(any(Ativo.class))).thenReturn(ativoSalvo);

        Ativo resultado = ativoService.criar(formValido);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(2L);
        assertThat(resultado.getNome()).isEqualTo("Notebook Thinkpad");
        assertThat(resultado.getPreco()).isEqualByComparingTo("6500.00");

        verify(ativoRepository, times(1)).save(any(Ativo.class));
    }

    @Test
    @DisplayName("atualizar: deve modificar os dados do ativo existente")
    void atualizar_quandoAtivoExiste_deveAtualizarDados() {
        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativoExistente));
        when(ativoRepository.save(any(Ativo.class))).thenReturn(ativoExistente);

        AtivoForm formAtualizado = new AtivoForm("Notebook Dell Pro", "XPS 13 Developer Edition", new BigDecimal("8299.99"), 1, null, "SN-DELL-1234", "ATIVO");

        Ativo resultado = ativoService.atualizar(1L, formAtualizado);

        assertThat(resultado.getNome()).isEqualTo("Notebook Dell Pro");
        assertThat(resultado.getPreco()).isEqualByComparingTo("8299.99");

        verify(ativoRepository).findById(1L);
        verify(ativoRepository).save(any(Ativo.class));
    }

    @Test
    @DisplayName("atualizar: deve lançar exceção quando ativo não existe")
    void atualizar_quandoAtivoNaoExiste_deveLancarExcecao() {
        when(ativoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ativoService.atualizar(99L, formValido))
                .isInstanceOf(AtivoNaoEncontradoException.class);

        verify(ativoRepository, never()).save(any());
    }

    @Test
    @DisplayName("excluir: deve deletar ativo quando ID existe")
    void excluir_quandoAtivoExiste_deveDeletar() {
        when(ativoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(ativoRepository).deleteById(1L);

        assertThatCode(() -> ativoService.excluir(1L))
                .doesNotThrowAnyException();

        verify(ativoRepository).existsById(1L);
        verify(ativoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("excluir: deve lançar exceção quando ativo não existe")
    void excluir_quandoAtivoNaoExiste_deveLancarExcecao() {
        when(ativoRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> ativoService.excluir(99L))
                .isInstanceOf(AtivoNaoEncontradoException.class)
                .hasMessageContaining("99");

        verify(ativoRepository, never()).deleteById(any());
    }
}
