package br.ufpb.dsc.mercado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para criação e edição de categorias.
 *
 * <p>Record Java imutável com validações Bean Validation.
 *
 * @param nome      nome da categoria — obrigatório, entre 2 e 80 caracteres
 * @param descricao descrição opcional — máximo 500 caracteres
 *
 * @author DSC - UFPB Campus IV
 */
public record CategoriaForm(

        @NotBlank(message = "O nome da categoria é obrigatório")
        @Size(min = 2, max = 80, message = "O nome deve ter entre 2 e 80 caracteres")
        String nome,

        @Size(max = 500, message = "A descrição pode ter no máximo 500 caracteres")
        String descricao

) {
}
