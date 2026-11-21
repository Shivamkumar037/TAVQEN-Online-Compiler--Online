package com.compiler.backend.compiler.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown=true)
public class ClientMessage {
 private String type,executionId,language,filename,code,data; private Integer versionIndex;
 public String getType(){return type;} public void setType(String v){type=v;} public String getExecutionId(){return executionId;} public void setExecutionId(String v){executionId=v;}
 public String getLanguage(){return language;} public void setLanguage(String v){language=v;} public String getFilename(){return filename;} public void setFilename(String v){filename=v;}
 public String getCode(){return code;} public void setCode(String v){code=v;} public String getData(){return data;} public void setData(String v){data=v;}
 public Integer getVersionIndex(){return versionIndex;} public void setVersionIndex(Integer v){versionIndex=v;}
}
