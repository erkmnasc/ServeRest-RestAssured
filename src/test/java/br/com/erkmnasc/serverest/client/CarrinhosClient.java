package br.com.erkmnasc.serverest.client;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.List;
import java.util.Map;

import static br.com.erkmnasc.serverest.support.ApiConfig.spec;
import static io.restassured.RestAssured.given;

/**
 * Chamadas do recurso /carrinhos. O carrinho pertence ao usuário do token:
 * concluir e cancelar a compra agem sobre o carrinho desse usuário.
 */
public final class CarrinhosClient {

    private static final String CARRINHOS = "/carrinhos";
    private static final String CARRINHO_POR_ID = "/carrinhos/{id}";

    private CarrinhosClient() {
    }

    /** Corpo com um único produto. */
    public static Map<String, Object> umProduto(String idProduto, int quantidade) {
        return Map.of("produtos", List.of(item(idProduto, quantidade)));
    }

    public static Map<String, Object> item(String idProduto, int quantidade) {
        return Map.of("idProduto", idProduto, "quantidade", quantidade);
    }

    public static Response cadastrar(Object corpo, String token) {
        return autenticado(token).body(corpo).when().post(CARRINHOS);
    }

    public static Response buscarPorId(String id) {
        return given().spec(spec()).pathParam("id", id).when().get(CARRINHO_POR_ID);
    }

    /** Remove o carrinho e mantém o estoque baixado. */
    public static Response concluirCompra(String token) {
        return autenticado(token).when().delete(CARRINHOS + "/concluir-compra");
    }

    /** Remove o carrinho e devolve as quantidades ao estoque. */
    public static Response cancelarCompra(String token) {
        return autenticado(token).when().delete(CARRINHOS + "/cancelar-compra");
    }

    private static RequestSpecification autenticado(String token) {
        RequestSpecification requisicao = given().spec(spec());
        if (token != null) {
            requisicao.header("Authorization", token);
        }
        return requisicao;
    }
}
