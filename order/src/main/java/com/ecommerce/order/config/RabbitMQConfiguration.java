package com.ecommerce.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration class.
 *
 * This class defines all messaging infrastructure required for the Order Service:
 *
 * - Queue: where messages are stored
 * - Exchange: routes messages to queues
 * - Binding: connects exchange to queue using routing key
 * - RabbitTemplate: used to publish messages
 * - MessageConverter: converts Java objects <-> JSON
 * - AmqpAdmin: automatically creates the above resources on startup
 *
 * Flow:
 * Producer (RabbitTemplate) -> Exchange -> (Routing Key) -> Queue -> Consumer
 */
@Configuration
public class RabbitMQConfiguration {

    /**
     * Name of the exchange.
     * Injected from application configuration (order-service.yaml).
     */
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    /**
     * Name of the queue.
     */
    @Value("${rabbitmq.queue.name}")
    private String queueName;

    /**
     * Routing key used to route messages from exchange to queue.
     */
    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    /**
     * Defines a durable queue.
     *
     * Durable = survives RabbitMQ restarts.
     *
     * @return Queue instance
     */
    @Bean
    public Queue queue() {
        return QueueBuilder
                .durable(queueName)
                .build();
    }

    /**
     * Defines a Topic Exchange.
     *
     * Topic exchanges route messages based on pattern matching of routing keys.
     * Example patterns:
     * - order.created
     * - order.*
     * - order.#
     *
     * Durable = survives broker restart.
     *
     * @return TopicExchange instance
     */
    @Bean
    public TopicExchange exchange() {
        return ExchangeBuilder
                .topicExchange(exchangeName)
                .durable(true)
                .build();
    }

    /**
     * Binds the queue to the exchange using the routing key.
     *
     * This means:
     * Any message sent to the exchange with this routing key
     * will be delivered to this queue.
     *
     * @return Binding instance
     */
    @Bean
    public Binding binding() {
        return BindingBuilder
                .bind(queue())
                .to(exchange())
                .with(routingKey);
    }

    /**
     * AMQP Admin responsible for automatically declaring
     * queues, exchanges, and bindings on application startup.
     *
     * Without this, you would need to manually create them in RabbitMQ.
     *
     * @param connectionFactory RabbitMQ connection factory
     * @return AmqpAdmin instance
     */
    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);

        // Ensures that the infrastructure (queue, exchange, binding)
        // is created automatically when the application starts
        admin.setAutoStartup(true);

        return admin;
    }

    /**
     * Message converter used by RabbitTemplate.
     *
     * Converts Java objects to JSON when sending messages,
     * and JSON back to Java objects when receiving.
     *
     * Uses Jackson under the hood.
     *
     * @return MessageConverter instance
     */
    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    /**
     * RabbitTemplate is the main abstraction used to send messages.
     *
     * Configuration:
     * - Uses JSON message converter
     * - Default exchange is pre-configured
     *
     * Example usage:
     * rabbitTemplate.convertAndSend("order.created", orderObject);
     *
     * @param connectionFactory RabbitMQ connection factory
     * @return RabbitTemplate instance
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);

        // Ensures objects are serialized to JSON
        template.setMessageConverter(messageConverter());

        // Sets default exchange, so you don't need to specify it every time
        template.setExchange(exchangeName);

        return template;
    }
}