package com.banco.clientes.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String CLIENTES_EXCHANGE = "clientes.exchange";

    /** Exchange tipo topic: enruta por patrón de routing key (cliente.creado, cliente.*, etc.). */
    @Bean
    public TopicExchange clientesExchange() {
        return new TopicExchange(CLIENTES_EXCHANGE, true, false); // durable, no auto-delete
    }

    /** Los mensajes viajan como JSON (legibles e independientes del lenguaje). */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}