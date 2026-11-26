# language: pt

@usuarios
Funcionalidade: Cadastro de usuários pela API
  Como administrador da loja
  Quero cadastrar usuários pela API
  Para que eles possam acessar a ServeRest

  Cenário: Cadastrar usuário administrador com sucesso
    Dado que tenho os dados de um novo usuário administrador
    Quando envio o cadastro
    Então o status da resposta é 201
    E a mensagem é "Cadastro realizado com sucesso"
    E o usuário aparece na consulta por id com os mesmos dados

  Cenário: Não permitir cadastro com email já usado
    Dado que já existe um usuário cadastrado
    E que tenho os dados de um novo usuário comum com o email desse usuário
    Quando envio o cadastro
    Então o status da resposta é 400
    E a mensagem é "Este email já está sendo usado"

  Esquema do Cenário: Validar campo obrigatório: <campo>
    Dado que tenho os dados de um novo usuário comum sem o campo "<campo>"
    Quando envio o cadastro
    Então o status da resposta é 400
    E o campo "<campo>" traz a mensagem "<mensagem>"

    Exemplos:
      | campo         | mensagem                    |
      | nome          | nome é obrigatório          |
      | email         | email é obrigatório         |
      | password      | password é obrigatório      |
      | administrador | administrador é obrigatório |
