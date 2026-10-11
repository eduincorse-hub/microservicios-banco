package com.banco.clientes.messaging;

import com.banco.clientes.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClienteEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Se ejecuta SOLO cuando la transacción del servicio se confirmó (commit).
     * Si hubo rollback, el evento se descarta y nunca llega a RabbitMQ.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publicar(ClienteEvent event) {
        String routingKey = "cliente." + event.tipo().name().toLowerCase();
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.CLIENTES_EXCHANGE, routingKey, event);
            log.info("Evento publicado [{}] clienteId={}", routingKey, event.clienteId());
        } catch (AmqpException ex) {
            // Los datos ya están guardados: no se revierte la operación del usuario.
            log.error("No se pudo publicar el evento [{}] clienteId={}", routingKey, event.clienteId(), ex);
        }
    }
}