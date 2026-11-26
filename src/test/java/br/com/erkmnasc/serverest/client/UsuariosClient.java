package br.com.erkmnasc.serverest.client;

import io.restassured.response.Response;

import static br.com.erkmnasc.serverest.support.ApiConfig.spec;
import static io.restassured.RestAssured.given;

/**
 * Chamadas do recurso /usuarios. Não valida nada: as validações ficam nos testes.
 */
public final class UsuariosClient {

    private static final String USUARIOS = "/usuarios";
    private static final String USUARIO_POR_ID = "/usuarios/{id}";

    private UsuariosClient() {
    }

    public static Response cadastrar(Object corpo) {
        return given().spec(spec()).body(corpo).when().post(USUARIOS);
    }

    public static Response buscarPorId(String id) {
        return given().spec(spec()).pathParam("id", id).when().get(USUARIO_POR_ID);
    }

    public static Response listarPorEmail(String email) {
        return given().spec(spec()).queryParam("email", email).when().get(USUARIOS);
    }

    public static Response alterar(String id, Object corpo) {
        return given().spec(spec()).pathParam("id", id).body(corpo).when().put(USUARIO_POR_ID);
    }

    public static Response excluir(String id) {
        return given().spec(spec()).pathParam("id", id).when().delete(USUARIO_POR_ID);
    }
}
