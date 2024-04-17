package ru.netology.fileserver.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.netology.fileserver.dto.requests.InputFileRequest;
import ru.netology.fileserver.dto.responses.FileActionResponse;
import ru.netology.fileserver.dto.responses.FileInfoResponse;
import ru.netology.fileserver.dto.responses.OutputFile;
import ru.netology.fileserver.entities.FileEntity;
import ru.netology.fileserver.entities.User;
import ru.netology.fileserver.exceptions.CreateFileException;
import ru.netology.fileserver.exceptions.FileNotFoundException;
import ru.netology.fileserver.exceptions.FileRemoveException;
import ru.netology.fileserver.exceptions.UnauthorizedException;
import ru.netology.fileserver.repositories.FileRepository;
import ru.netology.fileserver.repositories.UserRepository;
import ru.netology.fileserver.utils.JWTTokenUtil;

import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final FileRepository fileRepository;
    private final JWTTokenUtil util;
    private final UserRepository userRepository;

    @Transactional
    public FileActionResponse addFile(String token, InputFileRequest fileRequest) throws CreateFileException {
        User user = getUserFromToken(token);
        if (fileRequest.file().getSize() > 1024 * 1024 * 10) {
            throw new CreateFileException("File size is very big.");
        } else {
            FileEntity fileEntity = new FileEntity();
            String mainDir = getJarPath(getClass()) + "/files";
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

    @Transactional
    public FileActionResponse renameFile(String token, String oldFileName, String newFileName) throws FileNotFoundException {
        User user = getUserFromToken(token);
        if (fileRepository.findFileEntitiesByUserAndFilename(user, oldFileName).isPresent()) {
            FileEntity fileEntityInDb = fileRepository.findFileEntitiesByUserAndFilename(user, oldFileName).get();
            File localFile = findFileOnDisk(fileEntityInDb.getFilePath(), fileEntityInDb.getFilename());
            File renamedFile = new File(fileEntityInDb.getFilePath() + "/" + newFileName);
            if (localFile != null) {
                if (localFile.renameTo(renamedFile)) {
                    fileEntityInDb.setFilename(newFileName);
                    fileRepository.save(fileEntityInDb);
                    log.debug("File \"" + oldFileName + "\" renamed successfully.");
                    return new FileActionResponse(oldFileName, "renamed");
                }
            } else {
                log.error("File \"" + oldFileName + "\" not found!");
                fileRepository.deleteById(fileEntityInDb.getId());
                log.debug("File record was deleted from database.");
                throw new FileNotFoundException("File not found!");
            }
        }
        log.error("File \"" + oldFileName + "\" not found in database!");
        throw new FileNotFoundException("File \"" + oldFileName + "\" not found!");
    }

    private File findFileOnDisk(String filePath, String filename) {
        File file = new File(filePath + "/" + filename);
        if (file.exists()) {
            return file;
        }
        return null;
    }

    @Transactional
    public FileActionResponse removeFile(String token, String fileName) throws FileNotFoundException {
        User user = getUserFromToken(token);
        if (checkFileInDb(user, fileName)) {
            File file = findFileOnDisk(fileRepository
                    .findFileEntitiesByUserAndFilename(user, fileName).get().getFilePath(), fileName);
            if (file != null) {
                if (file.delete()) {
                    fileRepository.deleteById(fileRepository.findFileEntitiesByUserAndFilename(user, fileName).get().getId());
                    log.debug("File \"" + fileName + "\" deleted successfully");
                    return new FileActionResponse(fileName, "deleted");
                } else {
                    throw new FileRemoveException("File delete error!");
                }
            }
            fileRepository.deleteById(fileRepository.findFileEntitiesByUserAndFilename(user, fileName).get().getId());
            log.error("File \"" + fileName + "\" not found on disk!", new FileNotFoundException());
            throw new FileNotFoundException("File \"" + fileName + "\" not found!");
        } else {
            log.error("File \"" + fileName + "\" not found in database!", new FileNotFoundException());
            throw new FileNotFoundException("File \"" + fileName + "\" not found!");
        }
    }

    public List<FileInfoResponse> getAllUserFiles(Integer limit, String token) {
        User user = getUserFromToken(token);
        List<FileEntity> fileList = fileRepository.findFileEntitiesByUser(user).orElse(Collections.emptyList());
        if (fileList.isEmpty()) {
            return Collections.emptyList();
        }
        List<FileInfoResponse> list = fileList.stream().map(st -> new FileInfoResponse(st.getFilename(), st.getFileSize())).toList();
        if (list.size() > limit) {
            return list.subList(0, limit);
        } else {
            return list;
        }
    }

    private User getUserFromToken(String token) {
        return userRepository.findByUsername(util.getUsername(token))
                .orElseThrow(() -> new UnauthorizedException(util.getUsername(token)));
    }

    public static String getJarPath(Class aclass) {
        try {
            return new File(aclass.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
        return null;
    }

    public byte[] getFile(String token, String filename) throws FileNotFoundException {
        User user = getUserFromToken(token);
        if (checkFileInDb(user, filename)) {
            File file = findFileOnDisk(fileRepository.findFileEntitiesByUserAndFilename(user, filename).get().getFilePath(), filename);
            if (file != null){

               try (FileInputStream fis = new FileInputStream(file)) {
                   return fis.readAllBytes();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
                log.debug("File \"" + filename + "\" sends.");
            }
        }
        throw new FileNotFoundException("File \"" + filename + "\" not found!");
    }

    private boolean checkFileInDb(User user, String filename) {
        return fileRepository.findFileEntitiesByUserAndFilename(user, filename).isPresent();
    }

}
