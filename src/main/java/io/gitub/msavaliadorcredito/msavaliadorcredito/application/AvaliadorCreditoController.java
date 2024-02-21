package io.gitub.msavaliadorcredito.msavaliadorcredito.application;

import io.gitub.msavaliadorcredito.msavaliadorcredito.application.exception.DadosClienteNotFoundException;
import io.gitub.msavaliadorcredito.msavaliadorcredito.application.exception.ErroComunicacaoMicroservicesException;
import io.gitub.msavaliadorcredito.msavaliadorcredito.application.exception.ErroSolicitacaoCartaoException;
import io.gitub.msavaliadorcredito.msavaliadorcredito.domain.model.*;
import org.apache.http.protocol.ResponseServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("avaliacoes-credito")
public class AvaliadorCreditoController {

    @Autowired
    AvaliadorCreditoService avaliadorCreditoService;

    @GetMapping
    public String status () {
        return "ok";
    }

    @GetMapping(value = "situacao-cliente", params = "cpf")
    public ResponseEntity consultaSituacaoCliente(@RequestParam String cpf) {
        SituacaoCliente situacaoCliente = null;
        try {
            situacaoCliente = avaliadorCreditoService.obterSituacaocliente(cpf);
            return ResponseEntity.ok(situacaoCliente);
        } catch (DadosClienteNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ErroComunicacaoMicroservicesException e) {
            return ResponseEntity.status(HttpStatus.resolve(e.getStatus())).body(e.getMessage());
        }

    }

    @PostMapping
    public ResponseEntity realizarAvaliacao(@RequestBody DadosAvaliacao dados) {
        SituacaoCliente situacaoCliente = null;
        try {
            ReturnAvaliacaoCliente returnAvaliacaoCliente = avaliadorCreditoService.realizarAvaliacao(dados.getCpf(), dados.getRenda());
            return ResponseEntity.ok(returnAvaliacaoCliente);

        } catch (DadosClienteNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ErroComunicacaoMicroservicesException e) {
            return ResponseEntity.status(HttpStatus.resolve(e.getStatus())).body(e.getMessage());
        }
    }

    @PostMapping("solicitacoes-cartao")
    public ResponseEntity solicitarCartao(@RequestBody DadosSolicitacaoEmissaocartao dados) {
        try {
            ProtocoloSolicitacaoCartao protocolo = avaliadorCreditoService.solicitarEmissaoCartao(dados);
            return ResponseEntity.ok(protocolo);

        }catch (ErroSolicitacaoCartaoException e){
          return  ResponseEntity.internalServerError().body(e.getMessage());

        }
    }


}
