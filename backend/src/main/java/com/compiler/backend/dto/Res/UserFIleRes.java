package com.compiler.backend.dto.Res;

import jakarta.persistence.Lob;
import lombok.Data;

@Data
public class UserFIleRes {
    private Integer id;
    private String fileName;
    private String fileContent;
    private String fileExtension;

}
