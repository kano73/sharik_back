package com.mary.sharik.exception.handler;

import com.mary.sharik.exception.CustomHandleRuntimeException;
import com.mary.sharik.exception.MicroserviceExternalException;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.concurrent.CompletionException;

@RestControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(CompletionException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String customRuntime (CompletionException exception){
        String rowMessage = exception.getMessage().substring(0, exception.getMessage().length()-1);

        return rowMessage.substring(rowMessage.lastIndexOf('"') + 1);
    }

    @ExceptionHandler(MicroserviceExternalException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String externalExceptionOnMicroservice(MicroserviceExternalException exception){
        return exception.getMessage();
    }

    @ExceptionHandler(BadJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleJwtError() {
        return "Please login";
    }

    @ExceptionHandler(CustomHandleRuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String runtime(MethodArgumentNotValidException exception) {
        return exception.getMessage();
    }
}