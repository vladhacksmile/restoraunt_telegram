package com.vladhacksmile.service;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.Serializable;

public interface AnswerConsumer {

    <T extends Serializable> void consume(BotApiMethod<T> botApiMethod);
}
