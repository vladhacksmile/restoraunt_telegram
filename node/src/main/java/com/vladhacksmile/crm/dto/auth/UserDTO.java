package com.vladhacksmile.crm.dto.auth;

import com.vladhacksmile.crm.jdbc.user.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserDTO {

    /**
     * Идентификатор
     */
    private Long id;

    /**
     * Имя
     */
    private String name;

    /**
     * Фамилия
     */
    private String surname;

    /**
     * Отчество
     */
    private String patronymic;

    /**
     * Номер телефона
     */
    private String phoneNumber;

    /**
     * Электронная почта
     */
    private String mail;

    /**
     * Пароль
     */
    private String password;

    /**
     * Роль
     */
    private Role role;

}
