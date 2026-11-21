
package com.compiler.backend.service;

import com.compiler.backend.dto.Res.UserFIleRes;
import com.compiler.backend.entity.UserFile;
import com.compiler.backend.entity.UserFolder;
import com.compiler.backend.exception.FolderNotFoundException;
import com.compiler.backend.repo.UserFileRepository;
import com.compiler.backend.repo.UserFolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserFileService {

    private final UserFileRepository userFileRepository;
    private final UserFolderRepository userFolderRepository;

    // GET FILE BY ID
    @Transactional(readOnly = true)
    public UserFIleRes getFileById(Integer fileId) {

      UserFile saved= userFileRepository.findById(fileId)
                .orElseThrow(() -> new FolderNotFoundException("File not found"));


        UserFIleRes response=new UserFIleRes();
        response.setId(saved.getId());
        response.setFileContent(saved.getFileContent());
        response.setFileExtension(saved.getFileExtension());
        response.setFileName(saved.getFileName());
       return response;
    }
    @Transactional
    public UserFIleRes createFile(
            Integer folderId,
            String fileName,
            String fileExtension) {

        UserFolder folder = userFolderRepository.findById(folderId)
                .orElseThrow(() -> new FolderNotFoundException("Folder not found"));

        UserFile file = UserFile.builder()
                .fileName(fileName)
                .fileExtension(fileExtension)
                .fileContent("")
                .userFolder(folder)
                .build();

        UserFile saved=userFileRepository.save(file);

        UserFIleRes response=new UserFIleRes();

        response.setId(saved.getId());
        response.setFileContent(saved.getFileContent());
        response.setFileExtension(saved.getFileExtension());
        response.setFileName(saved.getFileName());

        return response;

    }


    @Transactional
    public UserFIleRes updateFile(
            Integer fileId,
            String fileName,
            String fileContent) {

        UserFile file = userFileRepository.findById(fileId)
                .orElseThrow(() -> new FolderNotFoundException("File not found"));

        file.setFileName(fileName);
        file.setFileContent(fileContent);

        UserFile saved=userFileRepository.save(file);
        UserFIleRes response=new UserFIleRes();
        response.setId(saved.getId());
        response.setFileContent(saved.getFileContent());
        response.setFileExtension(saved.getFileExtension());
        response.setFileName(saved.getFileName());

        return response;
    }


    @Transactional
    public void deleteFile(Integer fileId) {

        UserFile file = userFileRepository.findById(fileId)
                .orElseThrow(() -> new FolderNotFoundException("File not found"));

        userFileRepository.delete(file);
    }
}
