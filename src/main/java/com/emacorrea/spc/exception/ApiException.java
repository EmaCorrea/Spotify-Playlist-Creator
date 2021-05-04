package com.emacorrea.spc.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ApiException extends RuntimeException {

    private final int status;

    private static final long serialVersionUID = 1771504470592173488L;

    public ApiException(final HttpStatus status, final String msg) {
        super(msg);
        this.status = status.value();
    }

    public ApiException(int status) {
        super();
        this.status = status;
    }
}
