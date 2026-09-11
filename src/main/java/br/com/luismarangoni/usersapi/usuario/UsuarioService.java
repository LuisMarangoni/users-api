package br.com.luismarangoni.usersapi.usuario;



import org.springframework.security.crypto.password.PasswordEncoder;
import br.com.luismarangoni.usersapi.usuario.dto.AtualizarUsuarioRequest;
import br.com.luismarangoni.usersapi.usuario.dto.CriarUsuarioRequest;
import br.com.luismarangoni.usersapi.usuario.dto.UsuarioResponse;
import org.springframework.stereotype.Service;
import br.com.luismarangoni.usersapi.perfil.NomePerfil;
import br.com.luismarangoni.usersapi.perfil.Perfil;
import br.com.luismarangoni.usersapi.perfil.PerfilRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;


@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PerfilRepository perfilRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    public UsuarioResponse buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(
                        () -> new UsuarioNaoEncontradoException(id)
                );

        return UsuarioResponse.from(usuario);
    }
    @Transactional
    public UsuarioResponse criar(CriarUsuarioRequest request) {
        String nomeNormalizado = request.nome().trim();
        String emailNormalizado = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new EmailJaCadastradoException(emailNormalizado);
        }

        Perfil perfilPadrao = perfilRepository
                .findByNome(NomePerfil.USUARIO)
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Perfil padrão USUARIO não encontrado"
                        )
                );

        Usuario usuario = new Usuario(
                nomeNormalizado,
                emailNormalizado
        );

        String senhaHash = passwordEncoder.encode(request.senha());
        usuario.definirSenhaHash(senhaHash);

        usuario.adicionarPerfil(perfilPadrao);
        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return UsuarioResponse.from(usuarioSalvo);
    }
    @Transactional
    public UsuarioResponse atualizar(
            Long id,
            AtualizarUsuarioRequest request
    ) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(
                        () -> new UsuarioNaoEncontradoException(id)
                );

        String nomeNormalizado = request.nome().trim();
        String emailNormalizado = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(
                emailNormalizado,
                id
        )) {
            throw new EmailJaCadastradoException(emailNormalizado);
        }

        usuario.atualizarDados(
                nomeNormalizado,
                emailNormalizado
        );

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return UsuarioResponse.from(usuarioSalvo);
    }
    @Transactional
    public UsuarioResponse atualizarAtivo(
            Long id,
            boolean novoAtivo
    ) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(
                        () -> new UsuarioNaoEncontradoException(id)
                );

        usuario.atualizarAtivo(novoAtivo);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return UsuarioResponse.from(usuarioSalvo);
    }

    @Transactional
    public UsuarioResponse adicionarPerfil(
            Long usuarioId,
            NomePerfil nomePerfil
    ) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(
                        () -> new UsuarioNaoEncontradoException(usuarioId)
                );

        Perfil perfil = perfilRepository.findByNome(nomePerfil)
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Perfil não encontrado: " + nomePerfil
                        )
                );

        usuario.adicionarPerfil(perfil);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return UsuarioResponse.from(usuarioSalvo);
    }

}

