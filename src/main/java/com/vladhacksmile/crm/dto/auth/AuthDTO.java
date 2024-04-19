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

    private String phoneNumber;

    private String password;

    private String jwtToken;
}
