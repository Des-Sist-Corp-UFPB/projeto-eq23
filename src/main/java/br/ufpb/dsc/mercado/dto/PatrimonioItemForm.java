package br.ufpb.dsc.mercado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PatrimonioItemForm(
        @NotBlank(message = "O código do patrimônio é obrigatório")
        @Size(max = 50, message = "O código do patrimônio pode ter no máximo 50 caracteres")
        String codigo,

        @NotBlank(message = "O número de série do patrimônio é obrigatório")
        @Size(max = 50, message = "O número de série do patrimônio pode ter no máximo 50 caracteres")
        String numeroSerie
) {
}
