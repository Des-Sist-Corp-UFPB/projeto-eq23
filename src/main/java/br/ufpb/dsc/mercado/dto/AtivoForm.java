package br.ufpb.dsc.mercado.dto;

import jakarta.validation.constraints.*;

public record AtivoForm(
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres")
        String nome,

        @Size(max = 2000, message = "A descrição pode ter no máximo 2000 caracteres")
        String descricao,

        @Size(max = 50, message = "O número de série pode ter no máximo 50 caracteres")
        String numeroSerie,

        @NotBlank(message = "O status é obrigatório")
        @Size(max = 20, message = "O status deve ter no máximo 20 caracteres")
        String status
) {
}
