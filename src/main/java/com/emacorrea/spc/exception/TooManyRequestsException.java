package com.emacorrea.spc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class TooManyRequestsException  extends ApiException {

    public TooManyRequestsException(final String msg) {
        super(HttpStatus.TOO_MANY_REQUESTS, msg);
    }
}
