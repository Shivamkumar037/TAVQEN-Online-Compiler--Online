package com.compiler.backend.utility;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Languages {
    private Integer id;
    private String name;
    private String extension;
    private String icon;
    private String version;
    private String defaultCode;
}
