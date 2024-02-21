package io.gitub.msavaliadorcredito.msavaliadorcredito.application;

import feign.FeignException;
import io.gitub.msavaliadorcredito.msavaliadorcredito.application.exception.DadosClienteNotFoundException;
import io.gitub.msavaliadorcredito.msavaliadorcredito.application.exception.ErroComunicacaoMicroservicesException;
import io.gitub.msavaliadorcredito.msavaliadorcredito.application.exception.ErroSolicitacaoCartaoException;
import io.gitub.msavaliadorcredito.msavaliadorcredito.domain.model.*;
import io.gitub.msavaliadorcredito.msavaliadorcredito.infra.clients.CartoesResourceClient;
import io.gitub.msavaliadorcredito.msavaliadorcredito.infra.clients.ClienteResoureceClient;
import io.gitub.msavaliadorcredito.msavaliadorcredito.infra.mqueue.SolicitacaoEmissaoCartaoPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AvaliadorCreditoService {
    @Autowired
    CartoesResourceClient cartoesResourceClient;

    @Autowired
    ClienteResoureceClient clienteResoureceClient;

    @Autowired
    SolicitacaoEmissaoCartaoPublisher solicitacaoEmissaoCartaoPublisher;

    public SituacaoCliente obterSituacaocliente(String cpf) throws DadosClienteNotFoundException, ErroComunicacaoMicroservicesException {
        try {
            ResponseEntity<DadosCliente> dadosClienteResponse = clienteResoureceClient.dadosCliente(cpf);
            ResponseEntity<List<CartaoCliente>> cartoes = cartoesResourceClient.getCartoesByCliente(cpf);
            return  SituacaoCliente.builder()
                            .cliente(dadosClienteResponse.getBody())
                            .cartoes(cartoes.getBody())
                            .build();
        } catch (FeignException.FeignClientException e) {
            int status = e.status();
            if(HttpStatus.NOT_FOUND.value() == status) {
                throw new DadosClienteNotFoundException();
            }

            throw new ErroComunicacaoMicroservicesException(e.getMessage(), status);
        }
    }

    public ReturnAvaliacaoCliente realizarAvaliacao(String cpf, Long renda) throws DadosClienteNotFoundException, ErroComunicacaoMicroservicesException {
        try {
            ResponseEntity<DadosCliente> dadosClienteResponse = clienteResoureceClient.dadosCliente(cpf);
            ResponseEntity<List<Cartao>> cartoesResponse = cartoesResourceClient.getCartoesRendaAteh(renda);

            List<Cartao> cartoes = cartoesResponse.getBody();
            var listaCartoesAprovados = cartoes.stream().map(cartao -> {
                DadosCliente dadosCliente = dadosClienteResponse.getBody();
                BigDecimal limiteBasico = cartao.getLimiteBasico();
                BigDecimal idadeBD = BigDecimal.valueOf(dadosCliente.getIdade());
                var fator = idadeBD.divide(BigDecimal.valueOf(10));
                BigDecimal limiteAprovado = fator.multiply(limiteBasico);

                CartaoAprovado cartaoAprovado = new CartaoAprovado();
                cartaoAprovado.setLimiteAprovado(limiteAprovado);
                cartaoAprovado.setCartao(cartao.getNome());
                cartaoAprovado.setBandeira(cartao.getBandeira());

                return cartaoAprovado;
            }).collect(Collectors.toList());

            return new  ReturnAvaliacaoCliente(listaCartoesAprovados);

        } catch (FeignException.FeignClientException e) {
            int status = e.status();
            if(HttpStatus.NOT_FOUND.value() == status) {
                throw new DadosClienteNotFoundException();
            }

            throw new ErroComunicacaoMicroservicesException(e.getMessage(), status);
        }
    }

    public ProtocoloSolicitacaoCartao solicitarEmissaoCartao(DadosSolicitacaoEmissaocartao dados) {
        try {
            solicitacaoEmissaoCartaoPublisher.solicitarcartao(dados);
            var protocolo = UUID.randomUUID().toString();
            return new ProtocoloSolicitacaoCartao(protocolo);
        }catch (Exception e) {
            throw new ErroSolicitacaoCartaoException(e.getMessage());
        }
    }
}
