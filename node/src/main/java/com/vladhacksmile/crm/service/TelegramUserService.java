package com.vladhacksmile.crm.service;

import com.vladhacksmile.crm.jdbc.user.TelegramUser;
import com.vladhacksmile.crm.model.result.Result;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface TelegramUserService {

    Result<TelegramUser> findOrSaveUser(Message message, String email, String phoneNumber);
}
