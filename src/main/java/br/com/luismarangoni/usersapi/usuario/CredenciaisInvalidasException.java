package br.com.luismarangoni.usersapi.usuario;

public class CredenciaisInvalidasException
        extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha inválidos");
    }
}