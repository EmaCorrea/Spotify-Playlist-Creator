package com.emacorrea.spc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class TooManyRequestsException  extends ApiException {

    private static final long serialVersionUID = -2510257704632053504L;

    public TooManyRequestsException(final String msg) {
        super(HttpStatus.TOO_MANY_REQUESTS, msg);
    }

    public TooManyRequestsException() {
        super(HttpStatus.TOO_MANY_REQUESTS.value());
    }

}
