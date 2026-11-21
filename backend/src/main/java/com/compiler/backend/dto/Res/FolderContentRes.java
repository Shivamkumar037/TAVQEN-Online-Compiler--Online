package com.compiler.backend.dto.Res;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FolderContentRes {

    private Integer folderId;
    private String folderName;

    private List<FolderItem> folders;
    private List<FileItem> files;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FolderItem {
        private Integer id;
        private String folderName;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FileItem {
        private Integer id;
        private String fileName;
        private String fileExtension;
    }
}