package io.gitub.msavaliadorcredito.msavaliadorcredito.application.exception;

public class DadosClienteNotFoundException extends  Exception {
    public DadosClienteNotFoundException() {
        super("Dados Cliente não encontrado para o cpf informado.");
    }
}
