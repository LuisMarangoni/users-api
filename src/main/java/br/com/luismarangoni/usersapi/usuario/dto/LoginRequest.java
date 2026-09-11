package br.com.luismarangoni.usersapi.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail deve possuir um formato válido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        String senha
) {
}