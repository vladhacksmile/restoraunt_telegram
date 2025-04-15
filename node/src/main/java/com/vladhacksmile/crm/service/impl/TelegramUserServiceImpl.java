package com.vladhacksmile.crm.service.impl;

import com.vladhacksmile.crm.dao.TelegramUserDAO;
import com.vladhacksmile.crm.dto.auth.UserDTO;
import com.vladhacksmile.crm.jdbc.user.TelegramUser;
import com.vladhacksmile.crm.model.result.Result;
import com.vladhacksmile.crm.service.TelegramUserService;
import com.vladhacksmile.crm.service.auth.UserService;
import com.vladhacksmile.crm.utils.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDateTime;

import static com.vladhacksmile.crm.model.result.Result.resultOk;
import static com.vladhacksmile.crm.model.result.Result.resultWithStatus;
import static com.vladhacksmile.crm.model.result.status.Status.*;

@Service
public class TelegramUserServiceImpl implements TelegramUserService {

    @Autowired
    private TelegramUserDAO telegramUserDAO;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Result<TelegramUser> findOrSaveUser(Message message, String email, String phoneNumber) {
        var messageUser = message.getFrom();
        TelegramUser telegramUser = telegramUserDAO.findByTelegramId(messageUser.getId()).orElse(null);
        if (telegramUser != null) {
            return resultOk(telegramUser);
        }

        UserDTO userDTO = UserDTO.builder()
                .name(messageUser.getFirstName())
                .phoneNumber(phoneNumber)
                .mail(email)
                .password(passwordEncoder.encode(PasswordGenerator.generatePassword(10)))
                .build();

        Result<UserDTO> registerUserResult = userService.registerUser(userDTO);
        if (registerUserResult.isError()) {
            return registerUserResult.cast();
        }

        telegramUser = TelegramUser.builder()
                .userId(registerUserResult.getObject().getId())
                .telegramId(messageUser.getId())
                .chatId(message.getChatId())
                .userName(messageUser.getUserName())
                .firstUsage(LocalDateTime.now())
                .build();

        return resultWithStatus(CREATED, telegramUserDAO.save(telegramUser));
    }
}
