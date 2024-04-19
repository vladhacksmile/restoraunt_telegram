package com.vladhacksmile.crm.dto;

import com.vladhacksmile.crm.model.result.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseMapper {

    public static <T> ResponseEntity<Result<T>> map(Result<T> result) {
        return new ResponseEntity<>(result, HttpStatus.valueOf(result.getStatus().getHttpStatus()));
    }
}
