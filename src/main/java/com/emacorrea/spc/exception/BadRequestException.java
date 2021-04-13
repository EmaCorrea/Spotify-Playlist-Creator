package com.emacorrea.spc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends ApiException {

    public BadRequestException(final String msg) {
        super(HttpStatus.BAD_REQUEST, msg);
    }
}

