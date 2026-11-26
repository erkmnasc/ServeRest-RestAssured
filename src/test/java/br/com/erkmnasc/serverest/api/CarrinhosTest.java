package br.com.erkmnasc.serverest.api;

import br.com.erkmnasc.serverest.client.CarrinhosClient;
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
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("/carrinhos")
class CarrinhosTest {

    private static final int ESTOQUE_INICIAL = 10;

    private String idAdmin;
    private String tokenAdmin;
    private String idCliente;
    private String tokenCliente;
    private Produto produto;
    private String idProduto;

    @BeforeEach
    void prepararMassa() {
        Usuario admin = DadosFactory.usuarioAdministrador();
        idAdmin = Massa.cadastrarUsuario(admin);
        tokenAdmin = Massa.tokenDe(admin);

        Usuario cliente = DadosFactory.usuarioComum();
        idCliente = Massa.cadastrarUsuario(cliente);
        tokenCliente = Massa.tokenDe(cliente);

        produto = DadosFactory.produtoComEstoque(ESTOQUE_INICIAL);
        idProduto = Massa.cadastrarProduto(produto, tokenAdmin);
    }

    @AfterEach
    void limparMassa() {
        // Ordem importa: com carrinho aberto, a API não deixa excluir o produto
        // nem o usuário. Cancelar a compra remove o carrinho e devolve o estoque.
        CarrinhosClient.cancelarCompra(tokenCliente);
        ProdutosClient.excluir(idProduto, tokenAdmin);
        UsuariosClient.excluir(idCliente);
        UsuariosClient.excluir(idAdmin);
    }

    private String criarCarrinho(int quantidade) {
        return CarrinhosClient.cadastrar(CarrinhosClient.umProduto(idProduto, quantidade), tokenCliente)
                .then().statusCode(201)
                .extract().path("_id");
    }

    @Test
    @DisplayName("carrinho criado aparece na consulta com quantidade e preço total calculados")
    void criarCarrinho() {
        String idCarrinho = CarrinhosClient.cadastrar(CarrinhosClient.umProduto(idProduto, 3), tokenCliente)
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/cadastro.json"))
                .extract().path("_id");

        CarrinhosClient.buscarPorId(idCarrinho)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/carrinho.json"))
                .body("idUsuario", equalTo(idCliente))
                .body("quantidadeTotal", equalTo(3))
                .body("precoTotal", equalTo(produto.preco() * 3))
                .body("produtos", hasSize(1))
                .body("produtos[0].idProduto", equalTo(idProduto))
                .body("produtos[0].precoUnitario", equalTo(produto.preco()));
    }

    @Test
    @DisplayName("criar carrinho baixa o estoque do produto")
    void criarCarrinhoBaixaEstoque() {
        criarCarrinho(3);

        assertEquals(ESTOQUE_INICIAL - 3, Massa.estoqueDo(idProduto));
    }

    @Test
    @DisplayName("concluir a compra remove o carrinho e mantém o estoque baixado")
    void concluirCompra() {
        String idCarrinho = criarCarrinho(4);

        CarrinhosClient.concluirCompra(tokenCliente)
                .then()
                .statusCode(200)
                .body("message", equalTo("Registro excluído com sucesso"));

        CarrinhosClient.buscarPorId(idCarrinho)
                .then()
                .statusCode(400)
                .body("message", equalTo("Carrinho não encontrado"));
        assertEquals(ESTOQUE_INICIAL - 4, Massa.estoqueDo(idProduto));
    }

    @Test
    @DisplayName("cancelar a compra remove o carrinho e devolve o estoque")
    void cancelarCompra() {
        criarCarrinho(4);

        CarrinhosClient.cancelarCompra(tokenCliente)
                .then()
                .statusCode(200)
                .body("message", equalTo("Registro excluído com sucesso. Estoque dos produtos reabastecido"));

        assertEquals(ESTOQUE_INICIAL, Massa.estoqueDo(idProduto));
    }

    @Test
    @DisplayName("sem token retorna 401")
    void semToken() {
        CarrinhosClient.cadastrar(CarrinhosClient.umProduto(idProduto, 1), null)
                .then()
                .statusCode(401)
                .body("message", equalTo(
                        "Token de acesso ausente, inválido, expirado ou usuário do token não existe mais"));
    }

    @Test
    @DisplayName("produto inexistente retorna 400")
    void produtoInexistente() {
        CarrinhosClient.cadastrar(CarrinhosClient.umProduto("0000000000000000", 1), tokenCliente)
                .then()
                .statusCode(400)
                .body("message", equalTo("Produto não encontrado"));
    }

    @Test
    @DisplayName("quantidade maior que o estoque retorna 400 e não mexe no estoque")
    void quantidadeMaiorQueEstoque() {
        CarrinhosClient.cadastrar(CarrinhosClient.umProduto(idProduto, ESTOQUE_INICIAL + 1), tokenCliente)
                .then()
                .statusCode(400)
                .body("message", equalTo("Produto não possui quantidade suficiente"))
                .body("item.quantidadeEstoque", equalTo(ESTOQUE_INICIAL));

        assertEquals(ESTOQUE_INICIAL, Massa.estoqueDo(idProduto));
    }

    @ParameterizedTest(name = "quantidade {0}")
    @ValueSource(ints = {0, -1})
    @DisplayName("quantidade não positiva retorna 400")
    void quantidadeNaoPositiva(int quantidade) {
        CarrinhosClient.cadastrar(CarrinhosClient.umProduto(idProduto, quantidade), tokenCliente)
                .then()
                .statusCode(400)
                // a chave do erro tem colchetes e ponto, por isso vai entre aspas no GPath
                .body("'produtos[0].quantidade'", equalTo("produtos[0].quantidade deve ser um número positivo"));
    }

    @Test
    @DisplayName("o mesmo produto duas vezes no carrinho retorna 400")
    void produtoDuplicado() {
        Map<String, Object> corpo = Map.of("produtos", List.of(
                CarrinhosClient.item(idProduto, 1),
                CarrinhosClient.item(idProduto, 2)));

        CarrinhosClient.cadastrar(corpo, tokenCliente)
                .then()
                .statusCode(400)
                .body("message", equalTo("Não é permitido possuir produto duplicado"))
                .body("idProdutosDuplicados", hasItem(idProduto));
    }

    @Test
    @DisplayName("segundo carrinho para o mesmo usuário retorna 400")
    void maisDeUmCarrinho() {
        criarCarrinho(1);

        CarrinhosClient.cadastrar(CarrinhosClient.umProduto(idProduto, 1), tokenCliente)
                .then()
                .statusCode(400)
                .body("message", equalTo("Não é permitido ter mais de 1 carrinho"));
    }

    @Test
    @DisplayName("produto que está em carrinho não pode ser excluído")
    void excluirProdutoEmCarrinho() {
        String idCarrinho = criarCarrinho(1);

        ProdutosClient.excluir(idProduto, tokenAdmin)
                .then()
                .statusCode(400)
                .body("message", equalTo("Não é permitido excluir produto que faz parte de carrinho"))
                .body("idCarrinhos", hasItem(idCarrinho));
    }

    @Test
    @DisplayName("usuário com carrinho não pode ser excluído")
    void excluirUsuarioComCarrinho() {
        String idCarrinho = criarCarrinho(1);

        UsuariosClient.excluir(idCliente)
                .then()
                .statusCode(400)
                .body("message", equalTo("Não é permitido excluir usuário com carrinho cadastrado"))
                .body("idCarrinho", equalTo(idCarrinho));
    }
}
