package br.com.erkmnasc.serverest.support;

import br.com.erkmnasc.serverest.client.LoginClient;
import br.com.erkmnasc.serverest.client.ProdutosClient;
import br.com.erkmnasc.serverest.client.UsuariosClient;
import br.com.erkmnasc.serverest.model.Produto;
import br.com.erkmnasc.serverest.model.Usuario;

/**
 * Preparação de estado pela própria API, usada nos BeforeEach. Se o setup
 * falhar, o teste para aqui com a causa, e não numa validação mais adiante.
 */
public final class Massa {

    private Massa() {
    }

    public static String cadastrarUsuario(Usuario usuario) {
        return UsuariosClient.cadastrar(usuario)
                .then().statusCode(201)
                .extract().path("_id");
    }

    public static String tokenDe(Usuario usuario) {
        return LoginClient.login(usuario.email(), usuario.password())
                .then().statusCode(200)
                .extract().path("authorization");
    }

    public static String cadastrarProduto(Produto produto, String tokenAdmin) {
        return ProdutosClient.cadastrar(produto, tokenAdmin)
                .then().statusCode(201)
                .extract().path("_id");
    }

    public static int estoqueDo(String idProduto) {
        return ProdutosClient.buscarPorId(idProduto)
                .then().statusCode(200)
                .extract().path("quantidade");
    }
}
