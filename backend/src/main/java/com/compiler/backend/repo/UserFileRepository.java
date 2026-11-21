package com.compiler.backend.repo;

import com.compiler.backend.entity.UserFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFileRepository extends JpaRepository<UserFile, Integer> {


    List<UserFile> findByUserFolderId(Integer folderId);


    Optional<UserFile> findByFileNameAndUserFolderId(
            String fileName,
            Integer folderId
    );
}