package com.vladhacksmile.crm.service.impl.dispatchers;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.Serializable;

@Service
public class ProducerServiceImpl {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public<T extends Serializable> void producerAnswer(BotApiMethod<T> botApiMethod) {
        rabbitTemplate.convertAndSend("answer", botApiMethod);
    }
}
