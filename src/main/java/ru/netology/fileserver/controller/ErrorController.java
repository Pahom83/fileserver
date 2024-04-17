package ru.netology.fileserver.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.netology.fileserver.dto.Exception;
import ru.netology.fileserver.exceptions.*;

@RestControllerAdvice
public class ErrorController {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Exception> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Exception(400, e.getMessage()));
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<Exception> handleFileNotFound(FileNotFoundException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Exception(500, e.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Exception> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Exception(401, e.getMessage()));
    }

    @ExceptionHandler(FileRemoveException.class)
    public ResponseEntity<Exception> handleDeleteFile(FileRemoveException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Exception(500, e.getMessage()));
    }

    @ExceptionHandler(CreateFileException.class)
    public ResponseEntity<Exception> handleCreateFile(CreateFileException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Exception(400, e.getMessage()));
    }
}
