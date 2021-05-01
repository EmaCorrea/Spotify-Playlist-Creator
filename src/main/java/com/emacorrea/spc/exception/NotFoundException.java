package com.emacorrea.spc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends ApiException {

    public NotFoundException(final String msg) {
        super(HttpStatus.NOT_FOUND, msg);
    }

    public NotFoundException() {
        super(HttpStatus.NOT_FOUND.value());
    }

}