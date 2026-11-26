package br.com.erkmnasc.serverest.client;

import io.restassured.response.Response;

import java.util.Map;

import static br.com.erkmnasc.serverest.support.ApiConfig.spec;
import static io.restassured.RestAssured.given;

/**
 * Chamadas do recurso /login.
 */
public final class LoginClient {

    private static final String LOGIN = "/login";

    private LoginClient() {
    }

    public static Response login(String email, String password) {
        return login(Map.of("email", email, "password", password));
    }

    public static Response login(Map<String, ?> corpo) {
        return given().spec(spec()).body(corpo).when().post(LOGIN);
    }
}
