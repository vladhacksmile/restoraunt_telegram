package com.vladhacksmile.crm.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladhacksmile.crm.jdbc.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
}
