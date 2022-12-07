package com.nttdata.bc39.grupo04.api.exceptions;

import lombok.Data;

@Data
public class InvaliteInputException extends RuntimeException {
    public InvaliteInputException(String message) {
        super(message);
    }

    public InvaliteInputException() {
        super();
    }

    public InvaliteInputException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvaliteInputException(Throwable cause) {
        super(cause);
    }

    protected InvaliteInputException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
