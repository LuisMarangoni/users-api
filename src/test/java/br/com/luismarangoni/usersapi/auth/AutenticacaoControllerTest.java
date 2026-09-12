package br.com.luismarangoni.usersapi.auth;


import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import br.com.luismarangoni.usersapi.perfil.NomePerfil;
import br.com.luismarangoni.usersapi.perfil.Perfil;
import br.com.luismarangoni.usersapi.perfil.PerfilRepository;
import br.com.luismarangoni.usersapi.usuario.Usuario;
import br.com.luismarangoni.usersapi.usuario.UsuarioRepository;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AutenticacaoControllerTest {

    private static final String SENHA = "SenhaTeste123";

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveRetornarTokenNoLoginComCredenciaisValidas() throws Exception {
        String email = criarUsuario();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin(email, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void deveRetornarUnauthorizedNoLoginComSenhaInvalida() throws Exception {
        String email = criarUsuario();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin(email, "SenhaErrada123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail")
                        .value("E-mail ou senha inválidos"));
    }

    @Test
    void deveRetornarUnauthorizedAoAcessarRotaProtegidaSemToken()
            throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirAcessoARotaProtegidaComTokenValido() throws Exception {
        String email = criarUsuario();
        atribuirPerfil(email, NomePerfil.SUPORTE);
        String token = obterToken(email, SENHA);

        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void deveNegarListagemParaUsuarioComPerfilComum() throws Exception {
        String email = criarUsuario();
        String token = obterToken(email, SENHA);

        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveNegarAtribuicaoDePerfilParaUsuarioComum() throws Exception {
        String email = criarUsuario();
        String emailAlvo = criarUsuario();
        Long idAlvo = usuarioRepository.findByEmailIgnoreCase(emailAlvo)
                .orElseThrow()
                .getId();
        String token = obterToken(email, SENHA);

        mockMvc.perform(put("/usuarios/{id}/perfis/ADMIN", idAlvo)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirAtribuicaoDePerfilParaAdmin() throws Exception {
        String emailAdmin = criarUsuario();
        atribuirPerfil(emailAdmin, NomePerfil.ADMIN);

        String emailAlvo = criarUsuario();
        Long idAlvo = usuarioRepository.findByEmailIgnoreCase(emailAlvo)
                .orElseThrow()
                .getId();
        String token = obterToken(emailAdmin, SENHA);

        mockMvc.perform(put("/usuarios/{id}/perfis/SUPORTE", idAlvo)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private String criarUsuario() throws Exception {
        String email = "mockmvc-" + UUID.randomUUID() + "@email.com";
        String corpo = """
                {
                  "nome": "Usuário de teste MockMvc",
                  "email": "%s",
                  "senha": "%s"
                }
                """.formatted(email, SENHA);

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated());

        return email;
    }

    private String obterToken(String email, String senha) throws Exception {
        String resposta = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin(email, senha)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Matcher matcher = Pattern.compile("\\\"token\\\":\\\"([^\\\"]+)\\\"")
                .matcher(resposta);
        if (!matcher.find()) {
            throw new AssertionError("A resposta de login não contém o token");
        }
        return matcher.group(1);
    }

    private String corpoLogin(String email, String senha) {
        return """
                {
                  "email": "%s",
                  "senha": "%s"
                }
                """.formatted(email, senha);
    }

    private void atribuirPerfil(String email, NomePerfil nomePerfil) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow();
        Perfil perfil = perfilRepository.findByNome(nomePerfil)
                .orElseThrow();

        usuario.adicionarPerfil(perfil);
        usuarioRepository.save(usuario);
    }

}
