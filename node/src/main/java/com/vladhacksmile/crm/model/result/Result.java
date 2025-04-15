package com.vladhacksmile.crm.model.result;

import com.vladhacksmile.crm.model.result.status.Status;
import com.vladhacksmile.crm.model.result.status.StatusDescription;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {

    private Status status;

    private String description;

    private T object;

    public Result(Status status) {
        this.status = status;
    }

    public Result(Status status, T object) {
        this.status = status;
        this.object = object;
    }

    public boolean isError() {
        return this.getStatus() != Status.OK && this.getStatus() != Status.CREATED;
    }

    public boolean isOk() {
        return !isError();
    }

    public static <T> Result<T> resultOk() {
        return new Result<>(Status.OK, null);
    }

    public static <T> Result<T> resultOk(T object) {
        return new Result<>(Status.OK, object);
    }

    public static <T> Result<T> resultWithStatus(Status status, T object) {
        return new Result<>(status, null, object);
    }

    public static <T> Result<T> resultWithStatus(Status status, String description) {
        return new Result<>(status, description, null);
    }

    public static <T> Result<T> resultWithStatus(Status status, StatusDescription statusDescription) {
        return new Result<>(status, statusDescription.name(), null);
    }

    public static <T> Result<T> resultWithStatus(Status status, String description, T object) {
        return new Result<>(status, description, object);
    }

    public static <T> Result<T> resultWithStatus(Status status) {
        return new Result<>(status);
    }

    public <O> Result<O> cast() {
        //noinspection unchecked
        return (Result<O>) this;
    }
}