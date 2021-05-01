package com.emacorrea.spc.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ApiException extends RuntimeException {

    private final int status;

    public ApiException(final HttpStatus status, final String msg) {
        super(msg);
        this.status = status.value();
    }

    public ApiException(int status) {
        this.status = status;
    }
}
