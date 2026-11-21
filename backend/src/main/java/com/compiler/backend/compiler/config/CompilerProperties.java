package com.compiler.backend.compiler.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.*;
@ConfigurationProperties(prefix="compiler")
public class CompilerProperties {
 private String websocketPath="/ws/execute"; private String allowedOrigins="*"; private final Jdoodle jdoodle=new Jdoodle();
 public String getWebsocketPath(){return websocketPath;} public void setWebsocketPath(String v){websocketPath=v;}
 public String getAllowedOrigins(){return allowedOrigins;} public void setAllowedOrigins(String v){allowedOrigins=v;}
 public Jdoodle getJdoodle(){return jdoodle;}
 public static class Jdoodle {
  private boolean enabled=true; private String authUrl="https://api.jdoodle.com/v1/auth-token"; private String stompUrl="https://api.jdoodle.com/v1/stomp";
  private int connectTimeoutSeconds=15; private int localDailyCreditLimit=20; private final List<Account> accounts=new ArrayList<>(); private final Map<String,Language> languages=new LinkedHashMap<>();
  public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
  public String getAuthUrl(){return authUrl;} public void setAuthUrl(String v){authUrl=v;}
  public String getStompUrl(){return stompUrl;} public void setStompUrl(String v){stompUrl=v;}
  public int getConnectTimeoutSeconds(){return connectTimeoutSeconds;} public void setConnectTimeoutSeconds(int v){connectTimeoutSeconds=v;}
  public int getLocalDailyCreditLimit(){return localDailyCreditLimit;} public void setLocalDailyCreditLimit(int v){localDailyCreditLimit=v;}
  public List<Account> getAccounts(){return accounts;} public Map<String,Language> getLanguages(){return languages;}
 }
 public static class Account {
  private String id,clientId,clientSecret; private boolean enabled=true;
  public String getId(){return id;} public void setId(String v){id=v;} public String getClientId(){return clientId;} public void setClientId(String v){clientId=v;}
  public String getClientSecret(){return clientSecret;} public void setClientSecret(String v){clientSecret=v;} public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 }
 public static class Language {
  private String jdoodleLanguage; private Integer versionIndex;
  public String getJdoodleLanguage(){return jdoodleLanguage;} public void setJdoodleLanguage(String v){jdoodleLanguage=v;}
  public Integer getVersionIndex(){return versionIndex;} public void setVersionIndex(Integer v){versionIndex=v;}
 }
}
