package br.com.luismarangoni.usersapi.auth;

public record LoginResponse(
        String tipo,
        String token
) {
}
