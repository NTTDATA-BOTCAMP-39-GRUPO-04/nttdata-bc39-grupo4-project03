package com.nttdata.bc39.grupo04.api.resttemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nttdata.bc39.grupo04.api.exceptions.BadRequestException;
import com.nttdata.bc39.grupo04.api.exceptions.HttpErrorInfo;
import com.nttdata.bc39.grupo04.api.exceptions.InvaliteInputException;
import com.nttdata.bc39.grupo04.api.exceptions.NotFoundException;
import org.apache.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class RestTemplateImpl<T> {

    private final ObjectMapper mapper;
    private final RestTemplate restTemplate;
    private final Logger logger;

    public RestTemplateImpl(ObjectMapper mapper, RestTemplate restTemplate, Logger logger) {
        this.mapper = mapper;
        this.logger = logger;
        this.restTemplate = restTemplate;
    }

    public Flux<T> getWithReturnFlux(String url, String loggerMessage) {
        try {
            List<T> list = restTemplate.exchange(url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<T>>() {
                    }).getBody();
            if (Objects.isNull(list)) {
                throw new BadRequestException("Error, no se pudo establecer conexión con  la url:" + url);
            }
            return Mono.just(list).flatMapMany(Flux::fromIterable);
        } catch (HttpClientErrorException ex) {
            logger.warn(loggerMessage + ex.getMessage());
            throw handleHttpClientException(ex);
        }
    }

    public Mono<T> getWithReturnMono(String url, Class<T> tClass, String loggerMessage) {
        try {
            T objectResponse = restTemplate.getForObject(url, tClass);
            if (Objects.isNull(objectResponse)) {
                throw new InvaliteInputException("Error, no se pudo establecer conexión con  la url:" + url);
            }
            return Mono.just(objectResponse);
        } catch (HttpClientErrorException ex) {
            logger.warn(loggerMessage + ex.getMessage());
            throw handleHttpClientException(ex);
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        switch (ex.getStatusCode()) {
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(ex));
            case BAD_REQUEST:
                return new BadRequestException(getErrorMessage(ex));
            case UNPROCESSABLE_ENTITY:
                return new InvaliteInputException(getErrorMessage(ex));
            default:
                return ex;
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException io) {
            return io.getMessage();
        }
    }
}
