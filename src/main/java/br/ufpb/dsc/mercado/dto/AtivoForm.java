package br.ufpb.dsc.mercado.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record AtivoForm(
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres")
        String nome,

        @Size(max = 2000, message = "A descrição pode ter no máximo 2000 caracteres")
        String descricao,

        @NotNull(message = "O preço/valor é obrigatório")
        @DecimalMin(value = "0.00", message = "O valor não pode ser negativo")
        @Digits(integer = 8, fraction = 2, message = "O valor deve ter no máximo 8 dígitos inteiros e 2 decimais")
        BigDecimal preco,

        @NotNull(message = "A quantidade é obrigatória")
        @Min(value = 0, message = "A quantidade não pode ser negativa")
        Integer quantidade,

        Long categoriaId,

        @Size(max = 50, message = "O número de série pode ter no máximo 50 caracteres")
        String numeroSerie,

        @NotBlank(message = "O status é obrigatório")
        @Size(max = 20, message = "O status deve ter no máximo 20 caracteres")
        String status
) {
}
