package com.vladhacksmile.crm.service.dispatchers;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface MessageProcessorService {

    void processTextMessage(Update update);

}
