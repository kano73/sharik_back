package com.mary.sharik.exceptions;

public class ValidationFailedException extends CustomHandleRuntimeException {
    public ValidationFailedException(String message) {
        super(message);
    }
}
