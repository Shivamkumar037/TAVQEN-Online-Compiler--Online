package com.compiler.backend.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {
    @NotBlank
    private String message;
    @NotBlank
    private Integer mode;
}
