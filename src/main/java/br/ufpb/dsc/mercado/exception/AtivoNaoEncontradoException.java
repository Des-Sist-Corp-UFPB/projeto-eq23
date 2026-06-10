package br.ufpb.dsc.mercado.exception;

public class AtivoNaoEncontradoException extends RuntimeException {
    public AtivoNaoEncontradoException(Long id) {
        super("Ativo não encontrado com id: " + id);
    }
    public AtivoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
