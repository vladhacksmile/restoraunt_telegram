package com.vladhacksmile.crm.jdbc.order;

import lombok.Getter;

@Getter
public enum OrderStatus {
    NEW(0),
    IN_PROGRESS(1),
    READY(2),
    GIVEN(3),
    CANCELED(4);

    private final int id;

    OrderStatus(int id) {
        this.id = id;
    }
}
