package com.hrmtracker.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String FILE_UPLOAD_EXCHANGE = "file-upload-exchange";
    public static final String FILE_UPLOAD_QUEUE = "file-upload-queue";
    public static final String FILE_UPLOAD_ROUTING_KEY = "file-upload-routing-key";

    @Bean
    public Queue queue() {
        return new Queue(FILE_UPLOAD_QUEUE, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(FILE_UPLOAD_EXCHANGE);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(FILE_UPLOAD_ROUTING_KEY);
    }

    // ✅ Use JSON instead of Java serialization
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
