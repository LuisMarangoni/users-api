package br.com.luismarangoni.usersapi.usuario;

import br.com.luismarangoni.usersapi.perfil.NomePerfil;
import br.com.luismarangoni.usersapi.perfil.Perfil;
import br.com.luismarangoni.usersapi.perfil.PerfilRepository;
import br.com.luismarangoni.usersapi.usuario.dto.CriarUsuarioRequest;
import br.com.luismarangoni.usersapi.usuario.dto.UsuarioResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PerfilRepository perfilRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void deveCriarUsuarioComEmailNormalizadoEPerfilPadrao() {
        CriarUsuarioRequest request = new CriarUsuarioRequest(
                "  Luis Marangoni  ",
                "  LUIS@EMAIL.COM  ",
                "senha-segura"
        );

        Perfil perfilPadrao = new Perfil(NomePerfil.USUARIO);

        when(usuarioRepository.existsByEmailIgnoreCase("luis@email.com"))
                .thenReturn(false);

        when(perfilRepository.findByNome(NomePerfil.USUARIO))
                .thenReturn(Optional.of(perfilPadrao));

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        UsuarioResponse resultado = usuarioService.criar(request);

        assertEquals("Luis Marangoni", resultado.nome());
        assertEquals("luis@email.com", resultado.email());
        assertTrue(resultado.ativo());
        assertTrue(resultado.perfis().contains(NomePerfil.USUARIO));

        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void naoDeveCriarUsuarioComEmailDuplicado() {
        CriarUsuarioRequest request = new CriarUsuarioRequest(
                "Luis Marangoni",
                "luis@email.com",
                "senha-segura"
        );

        when(usuarioRepository.existsByEmailIgnoreCase("luis@email.com"))
                .thenReturn(true);

        assertThrows(
                EmailJaCadastradoException.class,
                () -> usuarioService.criar(request)
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(perfilRepository);
    }
}