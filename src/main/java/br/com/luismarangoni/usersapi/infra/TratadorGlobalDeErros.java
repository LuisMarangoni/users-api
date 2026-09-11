package br.com.luismarangoni.usersapi.infra;


import br.com.luismarangoni.usersapi.usuario.UsuarioNaoEncontradoException;
import br.com.luismarangoni.usersapi.usuario.EmailJaCadastradoException;
import br.com.luismarangoni.usersapi.usuario.CredenciaisInvalidasException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class TratadorGlobalDeErros {

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ProblemDetail> tratarCredenciaisInvalidas(
            CredenciaisInvalidasException exception
    ) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage()
        );

        problema.setTitle("Credenciais inválidas");

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(problema);
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ProblemDetail> tratarEmailJaCadastrado(
            EmailJaCadastradoException exception
    ) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problema.setTitle("E-mail já cadastrado");

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(problema);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> tratarValidacao(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> campos = new LinkedHashMap<>();

        for (FieldError erro : exception.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(
                    erro.getField(),
                    erro.getDefaultMessage()
            );
        }

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Um ou mais campos possuem valores inválidos"
        );

        problema.setTitle("Dados inválidos");
        problema.setProperty("campos", campos);

        return ResponseEntity
                .badRequest()
                .body(problema);
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<ProblemDetail> tratarUsuarioNaoEncontrado(
            UsuarioNaoEncontradoException exception
    ) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );

        problema.setTitle("Usuário não encontrado");

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(problema);
    }

}
