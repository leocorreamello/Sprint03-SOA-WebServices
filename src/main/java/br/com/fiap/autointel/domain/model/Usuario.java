package br.com.fiap.autointel.domain.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfil;

    @Column(nullable = false)
    private Instant criadoEm;

    protected Usuario() {
    }

    public Usuario(String nome, String email, String senhaCriptografada, Perfil perfil) {
        this.nome = nome;
        this.email = email;
        this.senha = senhaCriptografada;
        this.perfil = perfil;
        this.criadoEm = Instant.now();
    }

    public void alterarPerfil(Perfil novoPerfil) {
        this.perfil = novoPerfil;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
