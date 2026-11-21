package com.compiler.backend.dto.Res;

import com.compiler.backend.entity.UserFolder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserRes {
    private Integer id;
    private String name;
    private String email;
    private UserFolderResponse userFolder;
}
