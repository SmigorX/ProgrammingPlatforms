package com.marketviz.exception;

import org.springframework.http.HttpStatus;

/** Thrown when a requested entity does not exist in the database. */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
