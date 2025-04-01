package com.mary.sharik.exceptions.handler;

import com.mary.sharik.exceptions.CustomHandleRuntimeException;
import com.mary.sharik.exceptions.MicroserviceExternalException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.concurrent.CompletionException;

@RestControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(CompletionException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String customRuntime (CompletionException exception){
        System.out.println("studipy CompletionException handler handled an exception");

        System.out.println("i am studipy handler here is how i found message: "+ exception.getMessage());

        String rowMessage = exception.getMessage().substring(0, exception.getMessage().length()-1);
        String substring = rowMessage.substring(rowMessage.lastIndexOf('"')+1);

        System.out.println("result: "+substring);

        return substring;
    }

    @ExceptionHandler(MicroserviceExternalException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String externalExceptionOnMicroservice(MicroserviceExternalException exception){
        return exception.getMessage();
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