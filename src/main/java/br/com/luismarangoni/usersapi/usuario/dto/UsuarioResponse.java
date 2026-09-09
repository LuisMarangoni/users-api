package br.com.luismarangoni.usersapi.usuario.dto;

import br.com.luismarangoni.usersapi.usuario.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        boolean ativo,
        LocalDateTime dataCriacao
) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.isAtivo(),
                usuario.getDataCriacao()
        );
    }
}