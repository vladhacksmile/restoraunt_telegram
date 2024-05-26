package com.vladhacksmile.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.invoices.CreateInvoiceLink;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.io.Serializable;

import static org.aspectj.lang.reflect.DeclareAnnotation.Kind.Method;

@Log4j2
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    @Autowired
    private UpdateController updateController;

    @PostConstruct
    public void init() {
        updateController.registerBot(this);
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        updateController.process(update);
    }

    public <T extends Serializable> void sendMessage(BotApiMethod<T> botApiMethod) {
        if (botApiMethod != null) {
            try {
                execute(botApiMethod);
            } catch (TelegramApiException e) {
                log.error(e);
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }
}
