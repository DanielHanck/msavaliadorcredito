package io.gitub.msavaliadorcredito.msavaliadorcredito.infra.mqueue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gitub.msavaliadorcredito.msavaliadorcredito.domain.model.DadosSolicitacaoEmissaocartao;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SolicitacaoEmissaoCartaoPublisher {
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    Queue queue;

    public void solicitarcartao(DadosSolicitacaoEmissaocartao dados) throws JsonProcessingException {
        var json = convertIntoJson(dados);
        rabbitTemplate.convertAndSend(queue.getName(), json);

    }

    private String convertIntoJson(DadosSolicitacaoEmissaocartao dados) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        var json = mapper.writeValueAsString(dados);
        return json;
    }

}
