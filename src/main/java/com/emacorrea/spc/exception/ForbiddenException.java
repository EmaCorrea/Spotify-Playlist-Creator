package com.emacorrea.spc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends ApiException {

    private static final long serialVersionUID = -3953279557621705064L;

    public ForbiddenException(final String msg) {
        super(HttpStatus.FORBIDDEN, msg);
    }

    public ForbiddenException() {
        super(HttpStatus.FORBIDDEN.value());
    }

}
