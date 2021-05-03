package com.emacorrea.spc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends ApiException {

    private static final long serialVersionUID = -1658835336361768385L;

    public BadRequestException(final String msg) {
        super(HttpStatus.BAD_REQUEST, msg);
    }

    public BadRequestException() {
        super(HttpStatus.BAD_REQUEST.value());
    }
}

