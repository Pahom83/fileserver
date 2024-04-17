package ru.netology.fileserver.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.netology.fileserver.entities.FileEntity;
import ru.netology.fileserver.entities.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends CrudRepository<FileEntity, Long> {
    Optional<List<FileEntity>> findFileEntitiesByUser(User user);

    Optional<FileEntity> findFileEntitiesByUserAndFilename(User user, String filename);

}
