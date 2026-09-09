package br.com.luismarangoni.usersapi.usuario.dto;

import jakarta.validation.constraints.NotNull;

public record AtualizarAtivoRequest(

        @NotNull(message = "O campo ativo é obrigatório")
        Boolean ativo
) {
}