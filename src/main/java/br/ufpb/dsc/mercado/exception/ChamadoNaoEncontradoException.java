package br.ufpb.dsc.mercado.exception;

public class ChamadoNaoEncontradoException extends RuntimeException {
    public ChamadoNaoEncontradoException(Long id) {
        super("Chamado não encontrado com id: " + id);
    }
    public ChamadoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
