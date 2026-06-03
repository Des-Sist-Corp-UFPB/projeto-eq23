package br.ufpb.dsc.mercado.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Entidade JPA que representa uma categoria de produtos.
 *
 * <p>Categorias permitem organizar produtos em grupos (ex.: Hortifruti, Laticínios, Bebidas).
 * Um produto pode pertencer a no máximo uma categoria ({@code @ManyToOne}).
 *
 * @author DSC - UFPB Campus IV
 */
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome único da categoria.
     *
     * <p>O índice UNIQUE no banco garante que não existam duas categorias com o mesmo nome
     * (a verificação case-insensitive é feita via {@code LOWER(nome)} no índice do PostgreSQL).
     */
    @NotBlank(message = "O nome da categoria é obrigatório")
    @Size(min = 2, max = 80, message = "O nome deve ter entre 2 e 80 caracteres")
    @Column(name = "nome", nullable = false, length = 80, unique = true)
    private String nome;

    @Size(max = 500, message = "A descrição pode ter no máximo 500 caracteres")
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    /**
     * Data e hora de criação — preenchida automaticamente antes do INSERT.
     */
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    // =========================================================================
    // CALLBACKS JPA
    // =========================================================================

    @PrePersist
    protected void prePersist() {
        this.criadoEm = Instant.now();
    }

    // =========================================================================
    // CONSTRUTORES
    // =========================================================================

    public Categoria() {
    }

    public Categoria(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    // =========================================================================
    // GETTERS E SETTERS
    // =========================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    @Override
    public String toString() {
        return "Categoria{id=" + id + ", nome='" + nome + "'}";
    }
}
