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

    @Test
    void deveNegarAtribuicaoDePerfilParaSuporte() throws Exception {
        String emailSuporte = criarUsuario();
        atribuirPerfil(emailSuporte, NomePerfil.SUPORTE);

        String emailAlvo = criarUsuario();
        Long idAlvo = usuarioRepository.findByEmailIgnoreCase(emailAlvo)
                .orElseThrow()
                .getId();
        String token = obterToken(emailSuporte, SENHA);

        mockMvc.perform(put("/usuarios/{id}/perfis/ADMIN", idAlvo)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirQueUsuarioConsulteProprioPerfil() throws Exception {
        String email = criarUsuario();
        String token = obterToken(email, SENHA);

        mockMvc.perform(get("/usuarios/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void devePermitirUsuarioAtualizarProprioPerfil() throws Exception {
        String email = criarUsuario();
        String token = obterToken(email, SENHA);
        String novoEmail = "atualizado-" + UUID.randomUUID() + "@email.com";

        String corpo = """
            {
              "nome": "Nome Atualizado",
              "email": "%s"
            }
            """.formatted(novoEmail);

        mockMvc.perform(put("/usuarios/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(novoEmail));
    }

    @Test
    void devePaginarListagemDeUsuarios() throws Exception {
        String emailSuporte = criarUsuario();
        criarUsuario();
        criarUsuario();

        atribuirPerfil(emailSuporte, NomePerfil.SUPORTE);
        String token = obterToken(emailSuporte, SENHA);

        mockMvc.perform(get("/usuarios")
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void deveFiltrarUsuariosPorEmail() throws Exception {
        String emailSuporte = criarUsuario();
        String emailBuscado = criarUsuario();
        criarUsuario();

        atribuirPerfil(emailSuporte, NomePerfil.SUPORTE);
        String token = obterToken(emailSuporte, SENHA);

        mockMvc.perform(get("/usuarios")
                        .param("email", emailBuscado)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value(emailBuscado))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void deveCombinarFiltroPorNomeEStatusAtivo() throws Exception {
        String emailSuporte = criarUsuario();
        String emailAlvo = criarUsuario();
        criarUsuario();

        atribuirPerfil(emailSuporte, NomePerfil.SUPORTE);

        Usuario alvo = usuarioRepository.findByEmailIgnoreCase(emailAlvo)
                .orElseThrow();
        alvo.atualizarDados("Cliente Inativo", emailAlvo);
        alvo.atualizarAtivo(false);
        usuarioRepository.save(alvo);

        String token = obterToken(emailSuporte, SENHA);

        mockMvc.perform(get("/usuarios")
                        .param("nome", "inativo")
                        .param("ativo", "false")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Cliente Inativo"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void deveRetornarBadRequestAoCadastrarComDadosInvalidos() throws Exception {
        String corpo = """
            {
              "nome": "",
              "email": "email-invalido",
              "senha": "SenhaTeste123"
            }
            """;

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.email").exists());
    }

    @Test
    void deveRetornarConflictAoCadastrarEmailJaExistente() throws Exception {
        String email = "duplicado-" + UUID.randomUUID() + "@email.com";

        String corpo = """
            {
              "nome": "Usuario Teste",
              "email": "%s",
              "senha": "SenhaTeste123"
            }
            """.formatted(email);

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").exists());
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
