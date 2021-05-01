package com.emacorrea.spc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends ApiException {

    public ForbiddenException(final String msg) {
        super(HttpStatus.FORBIDDEN, msg);
    }

    public ForbiddenException() {
        super(HttpStatus.FORBIDDEN.value());
    }

}
