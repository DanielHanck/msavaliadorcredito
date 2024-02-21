package io.gitub.msavaliadorcredito.msavaliadorcredito.domain.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DadosSolicitacaoEmissaocartao {
    private Long idCartao;
    private String cpf;
    private String endereco;
    private BigDecimal limiteLiberado;
}
