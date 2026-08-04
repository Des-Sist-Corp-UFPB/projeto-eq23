package br.ufpb.dsc.mercado.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "chamado")
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O título é obrigatório")
    @Size(min = 5, max = 100, message = "O título deve ter entre 5 e 100 caracteres")
    @Column(name = "titulo", nullable = false, length = 100)
    private String titulo;

    @NotBlank(message = "A descrição é obrigatória")
    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @NotBlank(message = "A prioridade é obrigatória")
    @Column(name = "prioridade", nullable = false, length = 20)
    private String prioridade = "MEDIA"; // BAIXA, MEDIA, ALTA, CRITICA

    @NotBlank(message = "O status é obrigatório")
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ABERTO"; // ABERTO, EM_ATENDIMENTO, RESOLVIDO, FECHADO

    @Column(name = "categoria", length = 20)
    private String categoria; // HARDWARE, SOFTWARE, REDE, ACESSO, OUTRO — nullable, sugerido por IA

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "ativo_id", nullable = true)
    private Ativo ativo;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "patrimonio_id", nullable = true)
    private Patrimonio patrimonio;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "tecnico_id", nullable = true)
    private Usuario tecnico;

    @NotNull(message = "O cliente solicitante é obrigatório")
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

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
            this.status = "ABERTO";
        }
        if (this.prioridade == null) {
            this.prioridade = "MEDIA";
        }
    }

    @PreUpdate
    protected void preUpdate() {
        this.atualizadoEm = Instant.now();
    }

    public Chamado() {
    }

    public Chamado(String titulo, String descricao, String prioridade, String status, Ativo ativo, Usuario tecnico, Usuario cliente) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.prioridade = prioridade != null ? prioridade : "MEDIA";
        this.status = status != null ? status : "ABERTO";
        this.ativo = ativo;
        this.tecnico = tecnico;
        this.cliente = cliente;
    }

    public Chamado(String titulo, String descricao, String prioridade, String status, Patrimonio patrimonio, Usuario tecnico, Usuario cliente) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.prioridade = prioridade != null ? prioridade : "MEDIA";
        this.status = status != null ? status : "ABERTO";
        this.patrimonio = patrimonio;
        this.ativo = patrimonio != null ? patrimonio.getAtivo() : null;
        this.tecnico = tecnico;
        this.cliente = cliente;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getPrioridade() { return prioridade; }
    public void setPrioridade(String prioridade) { this.prioridade = prioridade; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Ativo getAtivo() { return ativo; }
    public void setAtivo(Ativo ativo) { this.ativo = ativo; }

    public Usuario getTecnico() { return tecnico; }
    public void setTecnico(Usuario tecnico) { this.tecnico = tecnico; }

    public Usuario getCliente() { return cliente; }
    public void setCliente(Usuario cliente) { this.cliente = cliente; }

    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }

    public Instant getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Instant updated) { this.atualizadoEm = updated; }

    public Patrimonio getPatrimonio() { return patrimonio; }
    public void setPatrimonio(Patrimonio patrimonio) {
        this.patrimonio = patrimonio;
        if (patrimonio != null) {
            this.ativo = patrimonio.getAtivo();
        }
    }
}
