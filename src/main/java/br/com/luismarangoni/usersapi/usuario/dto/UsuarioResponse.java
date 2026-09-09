package br.com.luismarangoni.usersapi.usuario.dto;

import br.com.luismarangoni.usersapi.usuario.Usuario;
import br.com.luismarangoni.usersapi.perfil.NomePerfil;
import br.com.luismarangoni.usersapi.perfil.Perfil;

import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        boolean ativo,
        LocalDateTime dataCriacao,
        Set<NomePerfil> perfis
) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.isAtivo(),
                usuario.getDataCriacao(),
                usuario.getPerfis()
                .stream()
                .map(Perfil::getNome)
                .collect(Collectors.toUnmodifiableSet())

        );
    }

}