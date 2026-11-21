
package com.compiler.backend.controller;

import com.compiler.backend.dto.Res.FolderContentRes;
import com.compiler.backend.dto.Res.UserFolderResponse;
import com.compiler.backend.entity.UserFolder;
import com.compiler.backend.service.UserFolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/folders")
 
@RequiredArgsConstructor
public class UserFolderController {
    @GetMapping("/{folderId}/contents")
    public ResponseEntity<FolderContentRes> getFolderContents(
            @PathVariable Integer folderId) {

        return ResponseEntity.ok(
                userFolderService.getFolderContents(folderId)
        );
    }
    private final UserFolderService userFolderService;


    @PostMapping("/create")
    public ResponseEntity<UserFolderResponse> createFolder(  @RequestParam Integer parentId,  @RequestParam String folderName) {

        return ResponseEntity.ok(
                userFolderService.createFolder(parentId, folderName)
        );
    }

    @PutMapping("/update/{folderId}")
    public ResponseEntity<UserFolderResponse> updateFolder(@PathVariable Integer folderId, @RequestParam String folderName) {

        return ResponseEntity.ok(
                userFolderService.updateFolder(folderId, folderName)
        );
    }

    @DeleteMapping("/delete/{folderId}")
    public ResponseEntity<String> deleteFolder( @PathVariable Integer folderId) {
        userFolderService.deleteFolder(folderId);
        return ResponseEntity.ok("Folder deleted successfully");
    }
}
