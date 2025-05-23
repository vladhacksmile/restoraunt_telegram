package com.vladhacksmile.crm.dao;

import com.vladhacksmile.crm.jdbc.user.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TelegramUserDAO extends JpaRepository<TelegramUser, Long> {

    Optional<TelegramUser> findByTelegramId(Long telegramId);

    Optional<TelegramUser> findByUserId(Long userId);
}