package com.mary.sharik.exception;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ValidationFailedException extends CustomHandleRuntimeException {
    public ValidationFailedException(String message) {
        super(message);
    }

    public ValidationFailedException(JsonProcessingException e) {
        super(e.getMessage());
    }
}
