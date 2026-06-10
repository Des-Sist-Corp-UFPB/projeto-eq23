package br.ufpb.dsc.mercado.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;

@Entity
@Table(name = "ativo")
public class Ativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do ativo é obrigatório")
    @Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres")
    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Size(max = 2000, message = "A descrição pode ter no máximo 2000 caracteres")
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;



    @Size(max = 50, message = "O número de série pode ter no máximo 50 caracteres")
    @Column(name = "numero_serie", length = 50)
    private String numeroSerie;

    @NotBlank(message = "O status é obrigatório")
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ATIVO";

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @PrePersist
    protected void prePersist() {
        Instant agora = Instant.now();
        this.criadoEm = agora;
        this.atualizadoEm = agora;
        if (this.status == null) {
            this.status = "ATIVO";
        }
    }

    @PreUpdate
    protected void preUpdate() {
        this.atualizadoEm = Instant.now();
    }

    public Ativo() {
    }

    public Ativo(String nome, String descricao, String numeroSerie, String status) {
        this.nome = nome;
        this.descricao = descricao;
        this.numeroSerie = numeroSerie;
        this.status = status != null ? status : "ATIVO";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }



    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }

    public Instant getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
