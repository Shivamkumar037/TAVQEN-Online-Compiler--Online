package com.compiler.backend.dto.req;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginReq {

    private String email;
    private String password;
}
