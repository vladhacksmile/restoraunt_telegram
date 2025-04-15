package com.vladhacksmile.crm.service.impl.dispatchers;

import com.vladhacksmile.crm.service.dispatchers.ConsumerService;
import com.vladhacksmile.crm.service.dispatchers.MessageProcessorService;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Log4j2
public class ConsumerServiceImpl implements ConsumerService {

    @Autowired
    private MessageProcessorService messageProcessorService;

    @Override
    @RabbitListener(queues = "telegram")
    public void consumeTextMessage(Update update) {
        messageProcessorService.processTextMessage(update);
    }
}

