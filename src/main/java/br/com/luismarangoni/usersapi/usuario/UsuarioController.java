package br.com.luismarangoni.usersapi.usuario;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import br.com.luismarangoni.usersapi.usuario.dto.CriarUsuarioRequest;
import br.com.luismarangoni.usersapi.usuario.dto.UsuarioResponse;
import br.com.luismarangoni.usersapi.usuario.dto.AtualizarAtivoRequest;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import br.com.luismarangoni.usersapi.usuario.dto.AtualizarUsuarioRequest;
import org.springframework.web.bind.annotation.PutMapping;
import br.com.luismarangoni.usersapi.perfil.NomePerfil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public UsuarioResponse buscarMeuPerfil(@AuthenticationPrincipal Jwt jwt) {
        Long idUsuario = Long.valueOf(jwt.getSubject());
        return usuarioService.buscarPorId(idUsuario);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criar(
            @Valid @RequestBody CriarUsuarioRequest request
    ) {
        return usuarioService.criar(request);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public UsuarioResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarUsuarioRequest request
    ) {
        return usuarioService.atualizar(id, request);
    }

    @PutMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public UsuarioResponse atualizarMeuPerfil(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AtualizarUsuarioRequest request
    ) {
        Long idUsuario = Long.valueOf(jwt.getSubject());
        return usuarioService.atualizar(idUsuario, request);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id}/ativo")
    public UsuarioResponse atualizarAtivo(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarAtivoRequest request
    ) {
        return usuarioService.atualizarAtivo(
                id,
                request.ativo()
        );
    }

    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/perfis/{nomePerfil}")
    public UsuarioResponse adicionarPerfil(
            @PathVariable Long id,
            @PathVariable NomePerfil nomePerfil
    ) {
        return usuarioService.adicionarPerfil(id, nomePerfil);
    }

}
