package br.com.erkmnasc.serverest.api;

import br.com.erkmnasc.serverest.client.LoginClient;
import br.com.erkmnasc.serverest.client.UsuariosClient;
import br.com.erkmnasc.serverest.model.Usuario;
import br.com.erkmnasc.serverest.support.DadosFactory;
import br.com.erkmnasc.serverest.support.Massa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("POST /login")
class LoginTest {

    private static final String CREDENCIAIS_INVALIDAS = "Email e/ou senha inválidos";

    private Usuario usuario;
    private String idUsuario;

    @BeforeEach
    void cadastrarUsuario() {
        usuario = DadosFactory.usuarioComum();
        idUsuario = Massa.cadastrarUsuario(usuario);
    }

    @AfterEach
    void excluirUsuario() {
        UsuariosClient.excluir(idUsuario);
    }

    @Test
    @DisplayName("login válido retorna 200 e o token no contrato esperado")
    void loginValido() {
        LoginClient.login(usuario.email(), usuario.password())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/login.json"))
                .body("message", equalTo("Login realizado com sucesso"));
    }

    @Test
    @DisplayName("senha incorreta retorna 401")
    void senhaIncorreta() {
        LoginClient.login(usuario.email(), usuario.password() + "x")
                .then()
                .statusCode(401)
                .body(matchesJsonSchemaInClasspath("schemas/mensagem.json"))
                .body("message", equalTo(CREDENCIAIS_INVALIDAS));
    }

    @Test
    @DisplayName("email não cadastrado retorna 401 com a mesma mensagem da senha incorreta")
    void emailNaoCadastrado() {
        // A mesma mensagem nos dois casos evita revelar quais emails existem na base
        LoginClient.login(DadosFactory.emailUnico(), usuario.password())
                .then()
                .statusCode(401)
                .body("message", equalTo(CREDENCIAIS_INVALIDAS));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("corposInvalidos")
    @DisplayName("validação dos campos retorna 400")
    void validacaoDosCampos(String caso, Map<String, String> corpo, Map<String, String> errosEsperados) {
        var resposta = LoginClient.login(corpo).then().statusCode(400);
        resposta.body("$", aMapWithSize(errosEsperados.size()));
        errosEsperados.forEach((campo, mensagem) -> resposta.body(campo, equalTo(mensagem)));
    }

    static Stream<Arguments> corposInvalidos() {
        return Stream.of(
                Arguments.of("email com formato inválido",
                        Map.of("email", "emailinvalido", "password", "teste123"),
                        Map.of("email", "email deve ser um email válido")),
                Arguments.of("sem email",
                        Map.of("password", "teste123"),
                        Map.of("email", "email é obrigatório")),
                Arguments.of("sem senha",
                        Map.of("email", "qa@teste.com.br"),
                        Map.of("password", "password é obrigatório")),
                Arguments.of("corpo vazio",
                        Map.of(),
                        Map.of("email", "email é obrigatório", "password", "password é obrigatório")));
    }
}
