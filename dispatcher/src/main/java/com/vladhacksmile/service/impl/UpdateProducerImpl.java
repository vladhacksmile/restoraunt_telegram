package com.vladhacksmile.service.impl;

import com.vladhacksmile.service.UpdateProducer;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Log4j2
public class UpdateProducerImpl implements UpdateProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void produce(String rabbitQueue, Update update) {
        rabbitTemplate.convertAndSend(rabbitQueue, update);
    }
}
