package com.vladhacksmile.crm.service.impl.dispatchers;

import com.vladhacksmile.crm.service.dispatchers.ProducerService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.Serializable;

@Service
public class ProducerServiceImpl implements ProducerService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public <T extends Serializable> void producerAnswer(BotApiMethod<T> botApiMethod) {
        rabbitTemplate.convertAndSend("answer", botApiMethod);
    }
}
