package com.compiler.backend.compiler.service;
import com.compiler.backend.compiler.config.CompilerProperties;
import org.springframework.http.MediaType; import org.springframework.stereotype.Service; import org.springframework.web.client.RestClient; import java.util.Map;
@Service
public class JDoodleTokenService {
 private final RestClient client=RestClient.create(); private final CompilerProperties props;
 public JDoodleTokenService(CompilerProperties p){props=p;}
 public String create(CompilerProperties.Account a){String token=client.post().uri(props.getJdoodle().getAuthUrl()).contentType(MediaType.APPLICATION_JSON)
  .body(Map.of("clientId",a.getClientId(),"clientSecret",a.getClientSecret())).retrieve().body(String.class);
  if(token==null||token.isBlank())throw new IllegalStateException("JDoodle returned an empty token");return token.trim();}
}
