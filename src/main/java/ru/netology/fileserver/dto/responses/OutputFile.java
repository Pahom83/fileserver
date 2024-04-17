package ru.netology.fileserver.dto.responses;

import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
public class OutputFile implements MultipartFile {
    String filename;
    File file;
    long size;
    String contentType;
    byte[] content;

    public OutputFile(String filename, File file) throws IOException {
        this.filename = filename;
        this.file = file;
        contentType = getContentType();
        content = getBytes();
        size = getSize();
    }

    @Override
    public String getName() {
        return filename;
    }

    @Override
    public String getOriginalFilename() {
        return filename;
    }

    @Override
    public String getContentType() {
        try {
            return Files.probeContentType(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public long getSize() {
        return file.length();
    }

    @Override
    public byte[] getBytes() throws IOException {
        return getInputStream().readAllBytes();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public Resource getResource() {
        return MultipartFile.super.getResource();
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {

    }

    @Override
    public void transferTo(Path dest) throws IOException, IllegalStateException {
        MultipartFile.super.transferTo(dest);
    }
}
