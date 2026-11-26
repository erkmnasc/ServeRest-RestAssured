package br.com.erkmnasc.serverest.api;

import br.com.erkmnasc.serverest.client.ProdutosClient;
import br.com.erkmnasc.serverest.client.UsuariosClient;
import br.com.erkmnasc.serverest.model.Produto;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("/produtos")
class ProdutosTest {

    private final List<String> usuariosCriados = new ArrayList<>();
    private final List<String> produtosCriados = new ArrayList<>();
    private String tokenAdmin;

    @BeforeEach
    void autenticarAdministrador() {
        Usuario admin = DadosFactory.usuarioAdministrador();
        usuariosCriados.add(Massa.cadastrarUsuario(admin));
        tokenAdmin = Massa.tokenDe(admin);
    }

    @AfterEach
    void limparMassa() {
        // Produtos primeiro: são criados pelo administrador
        produtosCriados.forEach(id -> ProdutosClient.excluir(id, tokenAdmin));
        usuariosCriados.forEach(UsuariosClient::excluir);
    }

    @Test
    @DisplayName("administrador cadastra produto e ele aparece na consulta por id")
    void cadastrarProduto() {
        Produto produto = DadosFactory.produto();

        String id = ProdutosClient.cadastrar(produto, tokenAdmin)
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/cadastro.json"))
                .extract().path("_id");
        produtosCriados.add(id);

        ProdutosClient.buscarPorId(id)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/produto.json"))
                .body("nome", equalTo(produto.nome()))
                .body("preco", equalTo(produto.preco()))
                .body("descricao", equalTo(produto.descricao()))
                .body("quantidade", equalTo(produto.quantidade()));
    }

    @Test
    @DisplayName("sem token retorna 401 (autenticação)")
    void semToken() {
        ProdutosClient.cadastrar(DadosFactory.produto(), null)
                .then()
                .statusCode(401)
                .body("message", equalTo(
                        "Token de acesso ausente, inválido, expirado ou usuário do token não existe mais"));
    }

    @Test
    @DisplayName("usuário comum retorna 403 (autorização)")
    void usuarioComum() {
        Usuario comum = DadosFactory.usuarioComum();
        usuariosCriados.add(Massa.cadastrarUsuario(comum));

        ProdutosClient.cadastrar(DadosFactory.produto(), Massa.tokenDe(comum))
                .then()
                .statusCode(403)
                .body("message", equalTo("Rota exclusiva para administradores"));
    }

    @Test
    @DisplayName("nome já usado retorna 400")
    void nomeDuplicado() {
        Produto produto = DadosFactory.produto();
        produtosCriados.add(ProdutosClient.cadastrar(produto, tokenAdmin)
                .then().statusCode(201).extract().path("_id"));

        ProdutosClient.cadastrar(produto, tokenAdmin)
                .then()
                .statusCode(400)
                .body("message", equalTo("Já existe produto com esse nome"));
    }

    @ParameterizedTest(name = "preço {0}")
    @MethodSource("precosInvalidos")
    @DisplayName("preço inválido retorna 400")
    void precoInvalido(Object preco, String mensagem) {
        Produto produto = DadosFactory.produto();
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("nome", produto.nome());
        corpo.put("preco", preco);
        corpo.put("descricao", produto.descricao());
        corpo.put("quantidade", produto.quantidade());

        ProdutosClient.cadastrar(corpo, tokenAdmin)
                .then()
                .statusCode(400)
                .body("preco", equalTo(mensagem));
    }

    static Stream<Arguments> precosInvalidos() {
        return Stream.of(
                Arguments.of(-1, "preco deve ser um número positivo"),
                Arguments.of(0, "preco deve ser um número positivo"),
                Arguments.of(1.5, "preco deve ser um inteiro"),
                Arguments.of("abc", "preco deve ser um número"));
    }

    @Test
    @DisplayName("corpo vazio retorna 400 com a mensagem de cada campo obrigatório")
    void camposObrigatorios() {
        ProdutosClient.cadastrar(Map.of(), tokenAdmin)
                .then()
                .statusCode(400)
                .body("$", aMapWithSize(4))
                .body("nome", equalTo("nome é obrigatório"))
                .body("preco", equalTo("preco é obrigatório"))
                .body("descricao", equalTo("descricao é obrigatório"))
                .body("quantidade", equalTo("quantidade é obrigatório"));
    }

    @Test
    @DisplayName("exclusão retorna 200 e o produto deixa de existir")
    void excluirProduto() {
        String id = ProdutosClient.cadastrar(DadosFactory.produto(), tokenAdmin)
                .then().statusCode(201).extract().path("_id");

        ProdutosClient.excluir(id, tokenAdmin)
                .then()
                .statusCode(200)
                .body("message", equalTo("Registro excluído com sucesso"));

        ProdutosClient.buscarPorId(id)
                .then()
                .statusCode(400)
                .body("message", equalTo("Produto não encontrado"));
    }
}
