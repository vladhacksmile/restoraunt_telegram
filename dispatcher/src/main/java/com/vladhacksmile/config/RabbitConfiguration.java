package com.vladhacksmile.config;

import lombok.Getter;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class RabbitConfiguration {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue textQueue() {
        return new Queue(RabbitConstants.MESSAGE_QUEUE);
    }

    @Bean
    public Queue answerQueue() {
        return new Queue(RabbitConstants.ANSWER_QUEUE);
    }
}
