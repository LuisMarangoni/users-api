package br.com.luismarangoni.usersapi.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    boolean existsByEmailIgnoreCase(String email);
}