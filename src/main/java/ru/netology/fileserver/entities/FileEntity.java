package ru.netology.fileserver.entities;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "users_files")
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "file_name", nullable = false, unique = true)
    private String filename;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_path")
    private String filePath;

    @ManyToOne
    private User user;
}
