package br.ufpb.dsc.mercado.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotBlank(message = "O usuário é obrigatório")
    @Column(unique = true)
    private String username;

    @NotBlank(message = "A senha é obrigatória")
    private String senha;

    @NotBlank(message = "O papel é obrigatório")
    @Column(name = "role", nullable = false, length = 20)
    private String role = "CLIENTE"; // ADMIN_SISTEMA, ADMIN, TECNICO, CLIENTE (hierarquia, ver UsuarioService)

    // false para contas provisionadas via Google (senha placeholder, nunca informada a ninguém) —
    // controla se a tela de Perfil exige a senha atual para trocar a senha (não tem o que confirmar
    // se a pessoa nunca teve uma senha de verdade).
    @Column(name = "senha_definida", nullable = false)
    private boolean senhaDefinida = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Usuario() {
    }

    public Usuario(String nome, String username, String senha) {
        this.nome = nome;
        this.username = username;
        this.senha = senha;
    }

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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isSenhaDefinida() {
        return senhaDefinida;
    }

    public void setSenhaDefinida(boolean senhaDefinida) {
        this.senhaDefinida = senhaDefinida;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // --- Métodos da Interface UserDetails do Spring Security ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String papel = role != null ? role : "CLIENTE";
        // ADMIN_SISTEMA herda ROLE_ADMIN também, para não precisar caçar e duplicar toda checagem
        // "hasRole(ADMIN)" espalhada pelo sistema — ele pode fazer tudo que um ADMIN normal faz,
        // mais a criação de novos admins (ver UsuarioService).
        if ("ADMIN_SISTEMA".equals(papel)) {
            return List.of(() -> "ROLE_USER", () -> "ROLE_ADMIN_SISTEMA", () -> "ROLE_ADMIN");
        }
        return List.of(() -> "ROLE_USER", () -> "ROLE_" + papel);
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
