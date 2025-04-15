package com.vladhacksmile.crm.service.dispatchers;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.io.Serializable;

public interface ProducerService {

    <T extends Serializable> void producerAnswer(BotApiMethod<T> botApiMethod);
}
