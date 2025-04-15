package com.vladhacksmile.crm.jdbc.user;

import lombok.Getter;

@Getter
public enum Role {

    ADMIN("SYSTEM"),
    CLIENT("CLIENT"),
    MAKER("MAKER");

    private final String name;

    Role(String name) {
        this.name = name;
    }
}