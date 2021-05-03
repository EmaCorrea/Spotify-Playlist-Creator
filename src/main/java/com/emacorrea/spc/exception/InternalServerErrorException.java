package com.emacorrea.spc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerErrorException extends ApiException {

    private static final long serialVersionUID = -8859722875176719148L;

    public InternalServerErrorException(final String msg) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, msg);
    }
}
