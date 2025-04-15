package com.vladhacksmile.crm.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthDTO {

    /**
     * Номер телефона
     */
    private String phoneNumber;

    /**
     * Пароль
     */
    private String password;

    /**
     * JWT токен
     */
    private String jwtToken;
}
