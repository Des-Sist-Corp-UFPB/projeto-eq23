package br.ufpb.dsc.mercado.dto;

import jakarta.validation.constraints.*;

public record ChamadoForm(
        @NotBlank(message = "O título é obrigatório")
        @Size(min = 5, max = 100, message = "O título deve ter entre 5 e 100 caracteres")
        String titulo,

        @NotBlank(message = "A descrição é obrigatória")
        String descricao,

        @NotBlank(message = "A prioridade é obrigatória")
        String prioridade,

        String status,

        Long ativoId,

        Long tecnicoId,

        Long clienteId
) {
}
