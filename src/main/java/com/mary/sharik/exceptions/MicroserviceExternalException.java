package com.mary.sharik.exceptions;

public class MicroserviceExternalException extends CustomHandleRuntimeException {
    public MicroserviceExternalException(String message) {
        super(message);
    }
}
