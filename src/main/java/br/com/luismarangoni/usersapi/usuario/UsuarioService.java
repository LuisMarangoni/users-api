package br.com.luismarangoni.usersapi.usuario;

import br.com.luismarangoni.usersapi.usuario.dto.AtualizarUsuarioRequest;
import br.com.luismarangoni.usersapi.usuario.dto.CriarUsuarioRequest;
import br.com.luismarangoni.usersapi.usuario.dto.UsuarioResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
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

    public UsuarioResponse criar(CriarUsuarioRequest request) {
        String nomeNormalizado = request.nome().trim();
        String emailNormalizado = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new EmailJaCadastradoException(emailNormalizado);
        }

        Usuario usuario = new Usuario(
                nomeNormalizado,
                emailNormalizado
        );

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return UsuarioResponse.from(usuarioSalvo);
    }

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

}
