package ru.netology.fileserver.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.fileserver.dto.requests.InputFileRequest;
import ru.netology.fileserver.dto.responses.FileActionResponse;
import ru.netology.fileserver.dto.responses.FileInfoResponse;
import ru.netology.fileserver.entities.FileEntity;
import ru.netology.fileserver.entities.User;
import ru.netology.fileserver.exceptions.*;
import ru.netology.fileserver.repositories.FileRepository;
import ru.netology.fileserver.repositories.UserRepository;
import ru.netology.fileserver.utils.JWTTokenUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    private static final int SIZE = 10485760;
    private final FileRepository fileRepository;
    private final JWTTokenUtil util;
    private final UserRepository userRepository;

    @Transactional(rollbackFor = {CreateFileException.class})
    public FileActionResponse addFile(String token, InputFileRequest fileRequest) throws Exception {
        User user = getUserFromToken(token);
        if (fileRequest.file().getSize() > SIZE) {
            log.error("File size is very big.");
            throw new CreateFileException("File size is very big.");
        } else {
            String mainDir = new File(FileService.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent() + "/files";
            FileEntity fileEntity = new FileEntity();
            fileEntity.setFilename(fileRequest.filename());
            fileEntity.setFileSize(fileRequest.file().getSize());
            fileEntity.setUser(user);
            fileEntity.setFilePath(mainDir + "/" + user.getUsername().replaceFirst("@", "_at_"));
            try {
                if (Files.notExists(Path.of(fileEntity.getFilePath()))) {
                    Files.createDirectories(Path.of(fileEntity.getFilePath()));
                }
                File file = new File(fileEntity.getFilePath() + "/" + fileEntity.getFilename());
                if (!file.exists()) {
                    if (file.createNewFile()) {
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            fos.write(fileRequest.file().getBytes());
                        }
                        fileRepository.save(fileEntity);
                    }
                }
            } catch (IOException e) {
                log.error(e.getMessage());
                throw new CreateFileException(e.getMessage());
            }
            log.debug("File added successfully.");
            return new FileActionResponse(fileRequest.filename(), "added");
        }
    }

    @Transactional(rollbackFor = {FileRenameException.class})
    public FileActionResponse renameFile(String token, String oldFileName, String newFileName) throws FileNotFoundException {
        log.debug("Start renaming file \"" + oldFileName + "\".");
        User user = getUserFromToken(token);
        Optional<FileEntity> opt = fileRepository.findFileEntitiesByUserAndFilename(user, oldFileName);
        if (opt.isPresent()) {
            FileEntity fileEntity = opt.get();
            File localFile = findFileOnDisk(fileEntity);
            File renamedFile = new File(fileEntity.getFilePath() + "/" + newFileName);
            if (localFile != null) {
                if (localFile.renameTo(renamedFile)) {
                    fileEntity.setFilename(newFileName);
                    fileRepository.save(fileEntity);
                    log.debug("File \"" + oldFileName + "\" renamed successfully.");
                    return new FileActionResponse(oldFileName, "renamed");
                } else {
                    log.error("Rename file \"" + oldFileName + "\" failed");
                    throw new FileRenameException("Rename file \"" + oldFileName + "\" failed");
                }
            } else {
                log.error("File \"" + oldFileName + "\" not found on disk!");
                fileRepository.deleteById(fileEntity.getId());
                log.debug("File record was deleted from database.");
            }
        }
        log.error("File \"" + oldFileName + "\" not found in database!");
        throw new FileNotFoundException("File \"" + oldFileName + "\" not found!");
    }

    private File findFileOnDisk(FileEntity fileEntity) {
        File file = new File(fileEntity.getFilePath() + "/" + fileEntity.getFilename());
        if (file.exists()) {
            return file;
        }
        return null;
    }

    @Transactional(rollbackFor = {FileRemoveException.class})
    public FileActionResponse removeFile(String token, String fileName) throws FileNotFoundException {
        log.debug("Start remove file \"" + fileName + "\".");
        User user = getUserFromToken(token);
        Optional<FileEntity> opt = fileRepository.findFileEntitiesByUserAndFilename(user, fileName);
        if (opt.isPresent()) {
            File file = findFileOnDisk(opt.get());
            if (file != null) {
                if (file.delete()) {
                    fileRepository.deleteById(opt.get().getId());
                    log.debug("File \"" + fileName + "\" deleted successfully");
                    return new FileActionResponse(fileName, "deleted");
                } else {
                    throw new FileRemoveException("File delete error!");
                }
            }
            fileRepository.deleteById(opt.get().getId());
            log.error("File \"" + fileName + "\" not found on disk!");
        } else {
            log.error("File \"" + fileName + "\" not found in database!");
        }
        throw new FileNotFoundException("File \"" + fileName + "\" not found!");
    }

    @Transactional
    public List<FileInfoResponse> getAllUserFiles(Integer limit, String token) {
        User user = getUserFromToken(token);
        log.debug("Get list of files for user " + user.getUsername());
        List<FileEntity> fileList = fileRepository.findFileEntitiesByUser(user).orElse(Collections.emptyList());
        if (!fileList.isEmpty()) {
            List<FileInfoResponse> list = fileList.stream().map(st -> new FileInfoResponse(st.getFilename(), st.getFileSize())).toList();
            if (list.size() > limit) {
                log.debug("Return list of files to " + user.getUsername());
                return list.subList(0, limit);
            } else {
                log.debug("Return list of files to " + user.getUsername());
                return list;
            }
        }
        log.debug("Files not found.");
        return Collections.emptyList();
    }

    private User getUserFromToken(String token) {
        return userRepository.findByUsername(util.getUsername(token))
                .orElseThrow(() -> new UnauthorizedException(util.getUsername(token)));
    }

    @Transactional
    public byte[] getFile(String token, String fileName) throws FileNotFoundException {
        User user = getUserFromToken(token);
        log.debug("Start send file \"" + fileName + "\" from file-server to user " + user.getUsername());
        Optional<FileEntity> opt = fileRepository.findFileEntitiesByUserAndFilename(user, fileName);
        if (opt.isPresent()) {
            File file = findFileOnDisk(opt.get());
            if (file != null){
               try (FileInputStream fis = new FileInputStream(file)) {
                   log.debug("File \"" + fileName + "\" sends.");
                   return fis.readAllBytes();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
        log.error("File \"" + fileName + "\" not found!");
        throw new FileNotFoundException("File \"" + fileName + "\" not found!");
    }

}
