package ru.netology.fileserver.dto;

import lombok.Data;

import java.util.Date;

@Data
public class Exception {
    private int status;
    private String messages;
    private Date timestamp;


    public Exception(int status, String messages) {
        this.status = status;
        this.messages = messages;
        this.timestamp = new Date();
    }
}
