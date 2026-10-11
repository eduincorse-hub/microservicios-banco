package com.banco.cuentas.messaging;

import com.banco.cuentas.config.RabbitMQConfig;
import com.banco.cuentas.service.ClienteReplicaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClienteEventListener {

    private final ClienteReplicaService clienteReplicaService;

    @RabbitListener(queues = RabbitMQConfig.CLIENTES_QUEUE)
    public void onClienteEvent(ClienteEvent event) {
        log.info("Evento recibido [{}] clienteId={}", event.tipo(), event.clienteId());
        clienteReplicaService.sincronizar(event);
    }
}