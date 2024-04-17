package ru.netology.fileserver.dto.requests;

import org.springframework.web.multipart.MultipartFile;

public record InputFileRequest(String filename, MultipartFile file) {
}
