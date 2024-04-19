package com.vladhacksmile.crm.model.result.status;

import lombok.Getter;

@Getter
public enum Status {
    OK(1, 200),
    CREATED(2, 201),
    NOT_FOUND(2, 404),
    INTERNAL_ERROR(3, 503),
    INCORRECT_PARAMS(4, 400),
    INCORRECT_STATE(5, 400),
    ACCESS_DENIED(6, 405);

    private final int id;
    private final int httpStatus;

    Status(int id, int httpStatus) {
        this.id = id;
        this.httpStatus = httpStatus;
    }
}