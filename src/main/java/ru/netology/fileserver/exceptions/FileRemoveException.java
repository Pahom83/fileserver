package ru.netology.fileserver.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FileRemoveException extends RuntimeException{
    public FileRemoveException(String message) {
        super(message);
    }
}
