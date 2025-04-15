package com.vladhacksmile.crm.jdbc.user;

import lombok.*;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Entity
@Table(name = "TelegramUser",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "userId"),
                @UniqueConstraint(columnNames = "telegramId")
        })
public class TelegramUser {

    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Идентификатор пользователя CRM системы
     */
    @Column(name = "userId", nullable = false)
    private Long userId;

    /**
     * Идентификатор пользователя Telegram ID
     */
    @Column(name = "telegramId", nullable = false)
    private Long telegramId;

    /**
     * Идентификатор чата
     */
    @Column(name = "chatId", nullable = false)
    private Long chatId;

    /**
     * Имя пользователя
     */
    @Column(name = "userName", nullable = false)
    private String userName;

    /**
     * Первое использование телеграм бота
     */
    @Column(name = "firstUsage", nullable = false)
    private LocalDateTime firstUsage;
}