package br.com.erkmnasc.serverest.support;

import br.com.erkmnasc.serverest.model.Produto;
import br.com.erkmnasc.serverest.model.Usuario;
import net.datafaker.Faker;

import java.util.Locale;
import java.util.UUID;

/**
 * Massa de teste. Email e nome de produto levam um trecho aleatório para não
 * colidir com dados que já existem na base.
 */
public final class DadosFactory {

    private static final Faker FAKER = new Faker(Locale.forLanguageTag("pt-BR"));

    private DadosFactory() {
    }

    public static Usuario usuarioAdministrador() {
        return usuario(true);
    }

    public static Usuario usuarioComum() {
        return usuario(false);
    }

    public static String emailUnico() {
        return "qa." + sufixo() + "@teste.com.br";
    }

    public static Produto produto() {
        return new Produto(
                FAKER.commerce().productName() + " " + sufixo(),
                FAKER.number().numberBetween(10, 5000),
                FAKER.commerce().material(),
                FAKER.number().numberBetween(1, 100));
    }

    private static Usuario usuario(boolean administrador) {
        return new Usuario(
                FAKER.name().fullName(),
                emailUnico(),
                "Qa@" + sufixo(),
                String.valueOf(administrador));
    }

    private static String sufixo() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
