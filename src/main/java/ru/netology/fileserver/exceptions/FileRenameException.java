package ru.netology.fileserver.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FileRenameException extends RuntimeException{
    public FileRenameException(String message) {
        super(message);
    }

}
