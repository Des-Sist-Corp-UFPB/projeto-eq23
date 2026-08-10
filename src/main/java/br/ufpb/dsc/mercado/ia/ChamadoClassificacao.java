package br.ufpb.dsc.mercado.ia;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * Saída estruturada do {@link ClassificacaoIaService}. O modelo só consegue devolver
 * texto nesse formato — nunca executa ações nem acessa dados fora do que foi enviado no prompt.
 */
public record ChamadoClassificacao(
        @JsonPropertyDescription("Categoria do chamado. Deve ser exatamente uma destas: HARDWARE, SOFTWARE, REDE, ACESSO, OUTRO")
        String categoria,

        @JsonPropertyDescription("Prioridade sugerida. Deve ser exatamente uma destas: BAIXA, MEDIA, ALTA, CRITICA")
        String prioridade,

        @JsonPropertyDescription("Justificativa breve (uma frase) da classificação sugerida")
        String justificativa
) {
}
