package br.ufpb.dsc.mercado.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record AtivoForm(
                @NotBlank(message = "O nome é obrigatório") @Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres") String nome,

                @Size(max = 2000, message = "A descrição pode ter no máximo 2000 caracteres") String descricao,

                @NotBlank(message = "O patrimônio é obrigatório") @Pattern(regexp = "^[Yy]\\d{4}$", message = "O patrimônio deve estar no formato Y seguido de 4 números (ex.: Y1234)") String numeroSerie,

                @NotBlank(message = "O status é obrigatório") @Size(max = 20, message = "O status deve ter no máximo 20 caracteres") String status,

                @Valid java.util.List<PatrimonioItemForm> patrimonios) {
}
