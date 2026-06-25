package br.ufpb.dsc.mercado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PatrimonioItemForm(
        @NotBlank(message = "O código do patrimônio é obrigatório")
        @Pattern(regexp = "^[A-Za-z]\\d{4}$", message = "O código do patrimônio deve ser uma letra seguida de 4 números (ex: Y5942)")
        String codigo,

        @NotBlank(message = "O número de série do patrimônio é obrigatório")
        @Size(max = 11, message = "O número de série do patrimônio deve ter no máximo 11 caracteres (ex: 5853Z210586)")
        String numeroSerie
) {
}

