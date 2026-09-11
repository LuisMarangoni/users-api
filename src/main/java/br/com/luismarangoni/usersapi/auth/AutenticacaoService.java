package br.com.luismarangoni.usersapi.auth;

import br.com.luismarangoni.usersapi.usuario.CredenciaisInvalidasException;
import br.com.luismarangoni.usersapi.usuario.Usuario;
import br.com.luismarangoni.usersapi.usuario.UsuarioRepository;
import br.com.luismarangoni.usersapi.usuario.dto.LoginRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AutenticacaoService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional(readOnly = true)
    public LoginResponse autenticar(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .filter(usuarioEncontrado -> usuarioEncontrado.getSenhaHash() != null)
                .filter(usuarioEncontrado -> usuarioEncontrado.isAtivo())
                .filter(usuarioEncontrado -> passwordEncoder.matches(
                        request.senha(),
                        usuarioEncontrado.getSenhaHash()
                ))
                .orElseThrow(CredenciaisInvalidasException::new);

        return jwtTokenService.gerarToken(usuario);
    }
}
