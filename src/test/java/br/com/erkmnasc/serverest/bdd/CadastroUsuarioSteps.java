package br.com.erkmnasc.serverest.bdd;

import br.com.erkmnasc.serverest.client.UsuariosClient;
import br.com.erkmnasc.serverest.model.Usuario;
import br.com.erkmnasc.serverest.support.DadosFactory;
import br.com.erkmnasc.serverest.support.Massa;
import io.cucumber.java.After;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

/**
 * Passos da funcionalidade de cadastro de usuários. O Cucumber cria uma
 * instância nova desta classe por cenário, então o estado não vaza entre eles.
 */
public class CadastroUsuarioSteps {

    private final List<String> idsCriados = new ArrayList<>();
    private Usuario existente;
    private Map<String, String> corpo;
    private Response resposta;

    @After
    public void excluirUsuariosCriados() {
        idsCriados.forEach(UsuariosClient::excluir);
    }

    @Dado("que tenho os dados de um novo usuário administrador")
    public void dadosDeAdministrador() {
        corpo = comoMapa(DadosFactory.usuarioAdministrador());
    }

    @Dado("que já existe um usuário cadastrado")
    public void usuarioExistente() {
        existente = DadosFactory.usuarioComum();
        idsCriados.add(Massa.cadastrarUsuario(existente));
    }

    @E("que tenho os dados de um novo usuário comum com o email desse usuário")
    public void dadosComEmailRepetido() {
        corpo = comoMapa(DadosFactory.usuarioComum().comEmail(existente.email()));
    }

    @Dado("que tenho os dados de um novo usuário comum sem o campo {string}")
    public void dadosSemCampo(String campo) {
        corpo = comoMapa(DadosFactory.usuarioComum());
        corpo.remove(campo);
    }

    @Quando("envio o cadastro")
    public void envioOCadastro() {
        resposta = UsuariosClient.cadastrar(corpo);
        if (resposta.statusCode() == 201) {
            idsCriados.add(resposta.path("_id"));
        }
    }

    @Então("o status da resposta é {int}")
    public void statusDaResposta(int status) {
        resposta.then().statusCode(status);
    }

    @E("a mensagem é {string}")
    public void mensagem(String mensagem) {
        resposta.then().body("message", equalTo(mensagem));
    }

    @E("o campo {string} traz a mensagem {string}")
    public void mensagemDoCampo(String campo, String mensagem) {
        resposta.then().body(campo, equalTo(mensagem));
    }

    @E("o usuário aparece na consulta por id com os mesmos dados")
    public void usuarioNaConsulta() {
        String id = resposta.path("_id");
        UsuariosClient.buscarPorId(id)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/usuario.json"))
                .body("nome", equalTo(corpo.get("nome")))
                .body("email", equalTo(corpo.get("email")))
                .body("administrador", equalTo(corpo.get("administrador")));
    }

    private static Map<String, String> comoMapa(Usuario usuario) {
        Map<String, String> mapa = new LinkedHashMap<>();
        mapa.put("nome", usuario.nome());
        mapa.put("email", usuario.email());
        mapa.put("password", usuario.password());
        mapa.put("administrador", usuario.administrador());
        return mapa;
    }
}
