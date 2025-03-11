package com.mary.sharik.exceptions.handler;

import com.mary.sharik.exceptions.CustomHandleRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(CustomHandleRuntimeException.class)
    public String customRuntime (CustomHandleRuntimeException exception){
        return exception.getMessage();
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFoundException() {
        return "404 not found";
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String runtime(RuntimeException exception) {
        return exception.getMessage();
    }
}