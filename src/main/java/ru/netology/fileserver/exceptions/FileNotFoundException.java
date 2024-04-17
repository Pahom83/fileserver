package ru.netology.fileserver.exceptions;

import lombok.NoArgsConstructor;

import java.io.IOException;
@NoArgsConstructor
public class FileNotFoundException extends IOException {
    public FileNotFoundException(String message) {
        super(message);
    }
}
