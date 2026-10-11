package com.banco.cuentas.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String CLIENTES_EXCHANGE = "clientes.exchange";
    public static final String CLIENTES_QUEUE = "cuentas.clientes.queue";
    public static final String CLIENTES_ROUTING_PATTERN = "cliente.*";

    public static final String CLIENTES_DLX = "cuentas.clientes.dlx";
    public static final String CLIENTES_DLQ = "cuentas.clientes.dlq";

    // ---------- Cola principal ----------

    /** Mismo exchange que declara ms-clientes (declararlo en ambos lados es seguro e idempotente). */
    @Bean
    public TopicExchange clientesExchange() {
        return new TopicExchange(CLIENTES_EXCHANGE, true, false);
    }

    /** Cola durable: si ms-cuentas está caído, los mensajes esperan aquí. */
    @Bean
    public Queue clientesQueue() {
        return QueueBuilder.durable(CLIENTES_QUEUE)
                .deadLetterExchange(CLIENTES_DLX)
                .deadLetterRoutingKey(CLIENTES_DLQ)
                .build();
    }

    /** Recibe cliente.creado, cliente.actualizado y cliente.eliminado. */
    @Bean
    public Binding clientesBinding() {
        return BindingBuilder.bind(clientesQueue())
                .to(clientesExchange())
                .with(CLIENTES_ROUTING_PATTERN);
    }

    // ---------- Dead Letter Queue ----------

    @Bean
    public DirectExchange clientesDeadLetterExchange() {
        return new DirectExchange(CLIENTES_DLX, true, false);
    }

    /** Aquí terminan los mensajes que fallaron después de todos los reintentos. */
    @Bean
    public Queue clientesDeadLetterQueue() {
        return QueueBuilder.durable(CLIENTES_DLQ).build();
    }

    @Bean
    public Binding clientesDeadLetterBinding() {
        return BindingBuilder.bind(clientesDeadLetterQueue())
                .to(clientesDeadLetterExchange())
                .with(CLIENTES_DLQ);
    }

    // ---------- Conversión JSON ----------

    @Bean
    public MessageConverter jsonMessageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        // Ignora el header __TypeId__ (com.banco.clientes...) y usa el tipo del parámetro del listener
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }
}