package ru.netology.fileserver.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(@JsonProperty("auth-token") String token) {
}