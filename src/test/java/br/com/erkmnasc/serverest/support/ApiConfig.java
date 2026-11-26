package br.com.erkmnasc.serverest.support;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Configuração comum a todas as requisições.
 *
 * A URL da API vem, nesta ordem, de -Dapi.url, da variável de ambiente
 * API_URL ou do padrão http://localhost:3000 (ServeRest local).
 */
public final class ApiConfig {

    private static final String URL_PADRAO = "http://localhost:3000";

    static {
        // Request e response só aparecem no log quando alguma validação falha
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);
    }

    private ApiConfig() {
    }

    public static String baseUri() {
        String porPropriedade = System.getProperty("api.url");
        if (porPropriedade != null && !porPropriedade.isBlank()) {
            return porPropriedade;
        }
        String porAmbiente = System.getenv("API_URL");
        if (porAmbiente != null && !porAmbiente.isBlank()) {
            return porAmbiente;
        }
        return URL_PADRAO;
    }

    public static RequestSpecification spec() {
        return new RequestSpecBuilder()
                .setBaseUri(baseUri())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }
}
