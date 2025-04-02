package com.mary.sharik.exception;

public class MicroserviceExternalException extends CustomHandleRuntimeException {
    public MicroserviceExternalException(String message) {
        super(message);
    }
}
