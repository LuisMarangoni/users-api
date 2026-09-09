package br.com.luismarangoni.usersapi.usuario;

public class UsuarioNaoEncontradoException extends RuntimeException {

    public UsuarioNaoEncontradoException(Long id) {
        super("Usuário não encontrado com o ID: " + id);
    }
}
