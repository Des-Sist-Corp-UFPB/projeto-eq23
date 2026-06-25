package br.ufpb.dsc.mercado.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "patrimonio")
public class Patrimonio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O código do patrimônio é obrigatório")
    @Pattern(regexp = "^[A-Za-z]\\d{4}$", message = "O código do patrimônio deve ser uma letra seguida de 4 números (ex: Y5942)")
    @Column(name = "codigo", nullable = false, unique = true, length = 5)
    private String codigo;

    @NotBlank(message = "O número de série é obrigatório")
    @Size(max = 11, message = "O número de série do patrimônio deve ter no máximo 11 caracteres (ex: 5853Z210586)")
    @Column(name = "numero_serie", nullable = false, unique = true, length = 11)
    private String numeroSerie;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ativo_id", nullable = false)
    private Ativo ativo;

    public Patrimonio() {
    }

    public Patrimonio(String codigo, String numeroSerie, Ativo ativo) {
        this.codigo = codigo;
        this.numeroSerie = numeroSerie;
        this.ativo = ativo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public Ativo getAtivo() {
        return ativo;
    }

    public void setAtivo(Ativo ativo) {
        this.ativo = ativo;
    }
}
