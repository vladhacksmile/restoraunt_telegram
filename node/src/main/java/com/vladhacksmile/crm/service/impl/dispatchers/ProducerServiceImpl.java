package com.vladhacksmile.crm.service.impl.dispatchers;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
public class ProducerServiceImpl {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void producerAnswer(SendMessage sendMessage) {
        rabbitTemplate.convertAndSend("answer", sendMessage);
    }
}
