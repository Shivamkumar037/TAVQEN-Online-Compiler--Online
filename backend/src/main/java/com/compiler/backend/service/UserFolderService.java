
package com.compiler.backend.service;

import com.compiler.backend.dto.Res.FolderContentRes;
import com.compiler.backend.dto.Res.UserFolderResponse;
import com.compiler.backend.entity.UserFolder;
import com.compiler.backend.exception.FolderNotFoundException;
import com.compiler.backend.repo.UserFolderRepository;
import com.compiler.backend.repo.UserFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserFolderService {

    private final UserFolderRepository userFolderRepository;
    private final UserFileRepository userFileRepository;
    @Transactional(readOnly = true)
    public FolderContentRes getFolderContents(Integer folderId) {

        UserFolder folder = userFolderRepository.findById(folderId)
                .orElseThrow(() -> new FolderNotFoundException("Folder not found"));

        var folders = userFolderRepository
                .findByParentFolderId(folderId)
                .stream()
                .map(subFolder -> FolderContentRes.FolderItem.builder()
                        .id(subFolder.getId())
                        .folderName(subFolder.getFolderName())
                        .build())
                .toList();

        var files = userFileRepository
                .findByUserFolderId(folderId)
                .stream()
                .map(file -> FolderContentRes.FileItem.builder()
                        .id(file.getId())
                        .fileName(file.getFileName())
                        .fileExtension(file.getFileExtension())
                        .build())
                .toList();

        return FolderContentRes.builder()
                .folderId(folder.getId())
                .folderName(folder.getFolderName())
                .folders(folders)
                .files(files)
                .build();
    }
    @Transactional
    public UserFolderResponse createFolder(Integer parentId, String folderName) {

        UserFolder parentFolder = userFolderRepository.findById(parentId)
                .orElseThrow(() -> new FolderNotFoundException("Parent Folder not found"));

        UserFolder folder = UserFolder.builder()
                .folderName(folderName)
                .parentFolder(parentFolder)
                .build();
UserFolder saved=userFolderRepository.save(folder);

        return UserFolderResponse.builder()
                .id(saved.getId())
                .folderName(saved.getFolderName())
                .parentFolderId(saved.getParentFolder().getId())
                .build();
    }

    @Transactional
    public void deleteFolder(Integer folderId) {

        UserFolder folder = userFolderRepository.findById(folderId)
                .orElseThrow(() -> new FolderNotFoundException("Folder not found"));

        deleteFolderRecursive(folder);
    }

    private void deleteFolderRecursive(UserFolder folder) {

        var files = userFileRepository.findByUserFolderId(folder.getId());

        userFileRepository.deleteAll(files);

        var subFolders = userFolderRepository.findByParentFolderId(folder.getId());

        for (UserFolder subFolder : subFolders) {
            deleteFolderRecursive(subFolder);
        }

        // Delete current folder
        userFolderRepository.delete(folder);
    }

    @Transactional
    public UserFolderResponse updateFolder(Integer folderId, String folderName) {

        UserFolder folder = userFolderRepository.findById(folderId)
                .orElseThrow(() -> new FolderNotFoundException("Folder not found"));

        folder.setFolderName(folderName);
UserFolder saved=userFolderRepository.save(folder);

        return UserFolderResponse.builder()
                .id(saved.getId())
                .folderName(saved.getFolderName())
                .parentFolderId(saved.getParentFolder().getId())
                .build();
    }
}
