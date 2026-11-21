
package com.compiler.backend.controller;

import com.compiler.backend.dto.Res.UserFIleRes;
import com.compiler.backend.entity.UserFile;
import com.compiler.backend.service.UserFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class UserFileController {

    private final UserFileService userFileService;

    // CREATE FILE
    @PostMapping("/create")
    public ResponseEntity<UserFIleRes> createFile(
            @RequestParam Integer folderId,
            @RequestParam String fileName,
            @RequestParam String fileExtension) {

        return ResponseEntity.ok(
                userFileService.createFile(folderId, fileName, fileExtension)
        );
    }

    // UPDATE FILE
    @PutMapping("/update/{fileId}")
    public ResponseEntity< UserFIleRes> updateFile(
            @PathVariable Integer fileId,
            @RequestParam String fileName,
            @RequestBody String fileContent) {

        return ResponseEntity.ok(
                userFileService.updateFile(fileId, fileName, fileContent)
        );
    }

    // DELETE FILE
    @DeleteMapping("/delete/{fileId}")
    public ResponseEntity<String> deleteFile(
            @PathVariable Integer fileId) {

        userFileService.deleteFile(fileId);

        return ResponseEntity.ok("File deleted successfully");
    }




    @GetMapping("/{fileId}")
    public ResponseEntity<UserFIleRes> getFileById(
            @PathVariable Integer fileId) {

        return ResponseEntity.ok(
                userFileService.getFileById(fileId)
        );
    }
}
