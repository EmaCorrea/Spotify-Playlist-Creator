package com.emacorrea.spc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends ApiException {

    private static final long serialVersionUID = 775500509488920953L;

    public UnauthorizedException(final String msg) {
        super(HttpStatus.UNAUTHORIZED, msg);
    }

    public UnauthorizedException() {
        super(HttpStatus.UNAUTHORIZED.value());
    }

}
