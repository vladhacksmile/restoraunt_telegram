package com.vladhacksmile.controller;

import com.vladhacksmile.config.RabbitConstants;
import com.vladhacksmile.service.UpdateProducer;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;

@Component
@Log4j2
public class UpdateController {

    @Autowired
    private UpdateProducer updateProducer;

    private TelegramBot telegramBot;

    public void registerBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void process(Update update) {
        if (update == null) {
            log.error("Update is null");
            return;
        }

        updateProducer.produce(RabbitConstants.MESSAGE_QUEUE, update);
    }

    public <T extends Serializable> void setView(BotApiMethod<T> botApiMethod) {
        telegramBot.sendMessage(botApiMethod);
    }
}
