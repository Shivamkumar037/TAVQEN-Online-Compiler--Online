package com.compiler.backend.repo;

import com.compiler.backend.entity.UserFolder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFolderRepository extends JpaRepository<UserFolder, Integer> {


    Optional<UserFolder> findByUserId(Integer userId);


    List<UserFolder> findByParentFolderId(Integer parentFolderId);


    List<UserFolder> findByParentFolderIsNull();
}