package com.vladhacksmile.service.impl;

import com.vladhacksmile.config.RabbitConstants;
import com.vladhacksmile.controller.UpdateController;
import com.vladhacksmile.service.AnswerConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.Serializable;

@Service
@RequiredArgsConstructor
@Log4j2
public class AnswerConsumerImpl implements AnswerConsumer {

    @Autowired
    private UpdateController updateController;

    @Override
    @RabbitListener(queues = RabbitConstants.ANSWER_QUEUE)
    public <T extends Serializable> void consume(BotApiMethod<T> botApiMethod) {
        updateController.setView(botApiMethod);
    }
}
