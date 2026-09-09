package br.com.luismarangoni.usersapi.perfil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "perfis")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private NomePerfil nome;

    protected Perfil() {
    }

    public Perfil(NomePerfil nome) {
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public NomePerfil getNome() {
        return nome;
    }

}
