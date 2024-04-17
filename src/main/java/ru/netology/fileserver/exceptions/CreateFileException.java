package ru.netology.fileserver.exceptions;

import lombok.NoArgsConstructor;

import java.io.IOException;
@NoArgsConstructor
public class CreateFileException extends IOException {
    public CreateFileException(String message) {
        super(message);
    }
}
