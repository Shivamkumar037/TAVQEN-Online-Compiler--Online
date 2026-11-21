package com.compiler.backend.compiler.service;
import com.compiler.backend.compiler.config.CompilerProperties; import org.springframework.stereotype.Component;
@Component
public class JDoodleLanguageMapper {
 private final CompilerProperties props; public JDoodleLanguageMapper(CompilerProperties p){props=p;}
 public Mapped map(String key,Integer requested){if(key==null)throw new IllegalArgumentException("language is required");
  var l=props.getJdoodle().getLanguages().get(key.toLowerCase()); if(l==null||l.getJdoodleLanguage()==null)throw new IllegalArgumentException("No JDoodle language mapping configured for "+key);
  Integer idx=requested!=null?requested:l.getVersionIndex(); if(idx==null)throw new IllegalArgumentException("JDoodle versionIndex is not configured for "+key);
  return new Mapped(l.getJdoodleLanguage(),idx);}
 public record Mapped(String language,Integer versionIndex){}
}
