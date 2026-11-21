package com.compiler.backend.dto.Res;

import com.compiler.backend.entity.User;
import com.compiler.backend.entity.UserFile;
import com.compiler.backend.entity.UserFolder;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserFolderResponse {
    private Integer id;
    private String folderName;
    private Integer parentFolderId;
}
