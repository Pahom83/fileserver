package ru.netology.fileserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.netology.fileserver.dto.requests.InputFileRequest;
import ru.netology.fileserver.dto.requests.RenameRequest;
import ru.netology.fileserver.services.FileService;

import java.io.FileNotFoundException;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("/list")
    public ResponseEntity<?> getListFiles(@RequestParam ("limit") Integer limit, @RequestHeader("auth-token") String token){
        return ResponseEntity.ok(fileService.getAllUserFiles(limit, token));
    }

    @PostMapping("/file")
    public ResponseEntity<?> addFile(@RequestHeader("auth-token") String token, InputFileRequest inputFileRequest) throws Exception {
        return ResponseEntity.ok(fileService.addFile(token, inputFileRequest));
    }

    @PutMapping(value = "/file")
    public ResponseEntity<?> renameFile(@RequestHeader("auth-token") String token, @RequestParam ("filename") String oldFilename, @RequestBody RenameRequest renameRequest) throws ru.netology.fileserver.exceptions.FileNotFoundException {
        return ResponseEntity.ok(fileService.renameFile(token, oldFilename, renameRequest.filename()));
    }

    @DeleteMapping("/file")
    public ResponseEntity<?> removeFile(@RequestHeader("auth-token") String token, @RequestParam("filename") String filename) throws ru.netology.fileserver.exceptions.FileNotFoundException {
        return ResponseEntity.ok(fileService.removeFile(token, filename));
    }

    @GetMapping("/file")
    public ResponseEntity<?> downloadFile(@RequestHeader("auth-token") String token,  @RequestParam("filename") String filename) throws ru.netology.fileserver.exceptions.FileNotFoundException {
        return ResponseEntity.ok(fileService.getFile(token, filename));
    }


}
