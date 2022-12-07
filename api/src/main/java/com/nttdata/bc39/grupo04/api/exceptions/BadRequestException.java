package com.nttdata.bc39.grupo04.api.exceptions;

import lombok.Data;

@Data
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
