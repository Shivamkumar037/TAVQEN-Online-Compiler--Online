package com.compiler.backend.compiler.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ServerMessage(String type,String executionId,String data,String message,Integer exitCode,Long executionTimeMs) {}
