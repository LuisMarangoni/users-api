package br.com.luismarangoni.usersapi.auth;

import br.com.luismarangoni.usersapi.usuario.Usuario;
import br.com.luismarangoni.usersapi.usuario.UsuarioRepository;
import br.com.luismarangoni.usersapi.usuario.CredenciaisInvalidasException;
import br.com.luismarangoni.usersapi.usuario.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private Usuario usuario;

    @InjectMocks
    private AutenticacaoService autenticacaoService;

    @Test
    void deveAutenticarUsuarioEDelegarGeracaoDoToken() {
        LoginRequest request = new LoginRequest(
                "  LUIS@EMAIL.COM ",
                "senha-segura"
        );
        LoginResponse respostaEsperada = new LoginResponse("Bearer", "token-de-teste");

        when(usuarioRepository.findByEmailIgnoreCase("luis@email.com"))
                .thenReturn(Optional.of(usuario));
        when(usuario.getSenhaHash()).thenReturn("hash-da-senha");
        when(usuario.isAtivo()).thenReturn(true);
        when(passwordEncoder.matches("senha-segura", "hash-da-senha"))
                .thenReturn(true);
        when(jwtTokenService.gerarToken(usuario)).thenReturn(respostaEsperada);

        LoginResponse resultado = autenticacaoService.autenticar(request);

        assertEquals(respostaEsperada, resultado);
        verify(jwtTokenService).gerarToken(usuario);
    }

    @Test
    void naoDeveAutenticarComSenhaInvalida() {
        LoginRequest request = new LoginRequest(
                "luis@email.com",
                "senha-incorreta"
        );

        when(usuarioRepository.findByEmailIgnoreCase("luis@email.com"))
                .thenReturn(Optional.of(usuario));
        when(usuario.getSenhaHash()).thenReturn("hash-da-senha");
        when(usuario.isAtivo()).thenReturn(true);
        when(passwordEncoder.matches("senha-incorreta", "hash-da-senha"))
                .thenReturn(false);

        assertThrows(
                CredenciaisInvalidasException.class,
                () -> autenticacaoService.autenticar(request)
        );
    }

    @Test
    void naoDeveAutenticarUsuarioInativo() {
        LoginRequest request = new LoginRequest(
                "luis@email.com",
                "senha-segura"
        );

        when(usuarioRepository.findByEmailIgnoreCase("luis@email.com"))
                .thenReturn(Optional.of(usuario));
        when(usuario.getSenhaHash()).thenReturn("hash-da-senha");
        when(usuario.isAtivo()).thenReturn(false);

        assertThrows(
                CredenciaisInvalidasException.class,
                () -> autenticacaoService.autenticar(request)
        );
    }
}
