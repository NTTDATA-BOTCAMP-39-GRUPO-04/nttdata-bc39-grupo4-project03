package com.nttdata.bc39.grupo04.api.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalControllerExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public @ResponseBody HttpErrorInfo handleNotFoundException(NotFoundException exception,
                                                               WebRequest request) {
        return createHttpErrorInfo(HttpStatus.NOT_FOUND, request, exception);
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvaliteInputException.class)
    public @ResponseBody HttpErrorInfo handleInvalidateInputException(InvaliteInputException exception,
                                                                      WebRequest request) {
        return createHttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, request, exception);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public @ResponseBody HttpErrorInfo handleBadRequestException(BadRequestException exception, WebRequest request) {
        return createHttpErrorInfo(HttpStatus.BAD_REQUEST, request, exception);
    }

    private HttpErrorInfo createHttpErrorInfo(HttpStatus httpStatus,
                                              WebRequest request,
                                              Exception exception) {
        final String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        final String message = exception.getMessage();
        LOG.debug("Returning Http status: {} , path:{}, message:{}", httpStatus, path, message);
        return new HttpErrorInfo(path, httpStatus, message);
    }

}
