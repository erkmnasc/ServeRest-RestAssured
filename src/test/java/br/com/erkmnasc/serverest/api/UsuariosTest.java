package br.com.erkmnasc.serverest.api;

import br.com.erkmnasc.serverest.client.UsuariosClient;
import br.com.erkmnasc.serverest.model.Usuario;
import br.com.erkmnasc.serverest.support.DadosFactory;
import br.com.erkmnasc.serverest.support.Massa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@DisplayName("/usuarios")
class UsuariosTest {

    private final List<String> idsCriados = new ArrayList<>();

    @AfterEach
    void excluirUsuariosCriados() {
        idsCriados.forEach(UsuariosClient::excluir);
    }

    private String cadastrar(Usuario usuario) {
        String id = Massa.cadastrarUsuario(usuario);
        idsCriados.add(id);
        return id;
    }

    @Test
    @DisplayName("cadastro retorna 201 e o usuário fica disponível na consulta por id")
    void cadastrarUsuario() {
        Usuario usuario = DadosFactory.usuarioAdministrador();

        String id = UsuariosClient.cadastrar(usuario)
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/cadastro.json"))
                .extract().path("_id");
        idsCriados.add(id);

        // Confere o efeito na base, não só a resposta do POST
        UsuariosClient.buscarPorId(id)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/usuario.json"))
                .body("nome", equalTo(usuario.nome()))
                .body("email", equalTo(usuario.email()))
                .body("password", equalTo(usuario.password()))
                .body("administrador", equalTo("true"))
                .body("_id", equalTo(id));
    }

    @Test
    @DisplayName("listagem filtrada por email retorna só o usuário cadastrado")
    void listarPorEmail() {
        Usuario usuario = DadosFactory.usuarioComum();
        String id = cadastrar(usuario);

        UsuariosClient.listarPorEmail(usuario.email())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/lista-usuarios.json"))
                .body("quantidade", equalTo(1))
                .body("usuarios", hasSize(1))
                .body("usuarios[0]._id", equalTo(id));
    }

    @Test
    @DisplayName("alteração retorna 200 e a consulta mostra o novo nome")
    void alterarUsuario() {
        Usuario usuario = DadosFactory.usuarioComum();
        String id = cadastrar(usuario);
        Usuario alterado = usuario.comNome("Nome Alterado");

        UsuariosClient.alterar(id, alterado)
                .then()
                .statusCode(200)
                .body("message", equalTo("Registro alterado com sucesso"));

        UsuariosClient.buscarPorId(id)
                .then()
                .statusCode(200)
                .body("nome", equalTo("Nome Alterado"));
    }

    @Test
    @DisplayName("exclusão retorna 200 e o usuário deixa de existir")
    void excluirUsuario() {
        String id = Massa.cadastrarUsuario(DadosFactory.usuarioComum());

        UsuariosClient.excluir(id)
                .then()
                .statusCode(200)
                .body("message", equalTo("Registro excluído com sucesso"));

        UsuariosClient.buscarPorId(id)
                .then()
                .statusCode(400)
                .body("message", equalTo("Usuário não encontrado"));
    }

    @Test
    @DisplayName("excluir id inexistente não é erro: retorna 200 informando que nada foi excluído")
    void excluirIdInexistente() {
        UsuariosClient.excluir("0000000000000000")
                .then()
                .statusCode(200)
                .body("message", equalTo("Nenhum registro excluído"));
    }

    @Test
    @DisplayName("email já usado retorna 400")
    void emailDuplicado() {
        Usuario existente = DadosFactory.usuarioComum();
        cadastrar(existente);

        UsuariosClient.cadastrar(DadosFactory.usuarioComum().comEmail(existente.email()))
                .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/mensagem.json"))
                .body("message", equalTo("Este email já está sendo usado"));
    }

    @Test
    @DisplayName("corpo vazio retorna 400 com a mensagem de cada campo obrigatório")
    void camposObrigatorios() {
        UsuariosClient.cadastrar(Map.of())
                .then()
                .statusCode(400)
                .body("$", aMapWithSize(4))
                .body("nome", equalTo("nome é obrigatório"))
                .body("email", equalTo("email é obrigatório"))
                .body("password", equalTo("password é obrigatório"))
                .body("administrador", equalTo("administrador é obrigatório"));
    }

    @ParameterizedTest(name = "administrador = \"{0}\"")
    @ValueSource(strings = {"sim", "1", "TRUE"})
    @DisplayName("administrador diferente de \"true\" ou \"false\" retorna 400")
    void administradorInvalido(String valor) {
        Usuario usuario = DadosFactory.usuarioComum();
        Map<String, String> corpo = new HashMap<>();
        corpo.put("nome", usuario.nome());
        corpo.put("email", usuario.email());
        corpo.put("password", usuario.password());
        corpo.put("administrador", valor);

        UsuariosClient.cadastrar(corpo)
                .then()
                .statusCode(400)
                .body("administrador", equalTo("administrador deve ser 'true' ou 'false'"));
    }
}
