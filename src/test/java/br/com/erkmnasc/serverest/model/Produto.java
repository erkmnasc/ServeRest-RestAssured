package br.com.erkmnasc.serverest.model;

/**
 * Corpo de cadastro de produto. Preço e quantidade são inteiros na ServeRest.
 */
public record Produto(String nome, int preco, String descricao, int quantidade) {
}
