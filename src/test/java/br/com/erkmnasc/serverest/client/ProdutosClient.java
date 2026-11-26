package br.com.erkmnasc.serverest.client;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static br.com.erkmnasc.serverest.support.ApiConfig.spec;
import static io.restassured.RestAssured.given;

/**
 * Chamadas do recurso /produtos. Cadastro e exclusão exigem o token de um
 * administrador; token nulo envia a requisição sem o header Authorization.
 */
public final class ProdutosClient {

    private static final String PRODUTOS = "/produtos";
    private static final String PRODUTO_POR_ID = "/produtos/{id}";

    private ProdutosClient() {
    }

    public static Response cadastrar(Object corpo, String token) {
        return autenticado(token).body(corpo).when().post(PRODUTOS);
    }

    public static Response buscarPorId(String id) {
        return given().spec(spec()).pathParam("id", id).when().get(PRODUTO_POR_ID);
    }

    public static Response excluir(String id, String token) {
        return autenticado(token).pathParam("id", id).when().delete(PRODUTO_POR_ID);
    }

    private static RequestSpecification autenticado(String token) {
        RequestSpecification requisicao = given().spec(spec());
        if (token != null) {
            requisicao.header("Authorization", token);
        }
        return requisicao;
    }
}
