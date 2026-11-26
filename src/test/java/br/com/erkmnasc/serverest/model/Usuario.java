package br.com.erkmnasc.serverest.model;

/**
 * Corpo de cadastro e alteração de usuário. O campo administrador é texto
 * ("true" ou "false"), como a ServeRest espera.
 */
public record Usuario(String nome, String email, String password, String administrador) {

    public Usuario comNome(String novoNome) {
        return new Usuario(novoNome, email, password, administrador);
    }

    public Usuario comEmail(String novoEmail) {
        return new Usuario(nome, novoEmail, password, administrador);
    }
}
