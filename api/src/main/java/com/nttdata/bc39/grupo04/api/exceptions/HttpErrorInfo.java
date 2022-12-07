package com.nttdata.bc39.grupo04.api.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HttpErrorInfo {
    private ZonedDateTime timestamp;
    private String path;
    private HttpStatus httpStatus;
    private String message;

    public HttpErrorInfo(String path, HttpStatus httpStatus, String message) {
        this.path = path;
        this.httpStatus = httpStatus;
        this.message = message;
        this.timestamp = ZonedDateTime.now();
    }
}
