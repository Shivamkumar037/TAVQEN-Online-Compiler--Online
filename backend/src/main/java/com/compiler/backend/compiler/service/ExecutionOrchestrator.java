package com.compiler.backend.compiler.service;
import com.compiler.backend.compiler.config.CompilerProperties; import com.compiler.backend.compiler.dto.ClientMessage; import com.compiler.backend.compiler.dto.ServerMessage;
import org.springframework.stereotype.Service; import java.util.*; import java.util.concurrent.*; import java.util.function.Consumer;
@Service
public class ExecutionOrchestrator {
 private final CompilerProperties props;private final JDoodleAccountManager accounts;private final JDoodleTokenService tokens;private final JDoodleLanguageMapper mapper;private final JDoodleSocketBridge bridge;
 private final Map<String,JDoodleSocketBridge.Upstream> active=new ConcurrentHashMap<>();private final Map<String,String> owner=new ConcurrentHashMap<>();
 public ExecutionOrchestrator(CompilerProperties p,JDoodleAccountManager a,JDoodleTokenService t,JDoodleLanguageMapper m,JDoodleSocketBridge b){props=p;accounts=a;tokens=t;mapper=m;bridge=b;}
 public void start(ClientMessage req,String clientId,Consumer<ServerMessage> out){
  if(!props.getJdoodle().isEnabled()){out.accept(new ServerMessage("ERROR",req.getExecutionId(),null,"JDoodle provider is disabled",null,null));return;}
  if(blank(req.getExecutionId())||blank(req.getCode())||blank(req.getLanguage())){out.accept(new ServerMessage("ERROR",req.getExecutionId(),null,"START requires executionId, language and code",null,null));return;}
  JDoodleLanguageMapper.Mapped lang;try{lang=mapper.map(req.getLanguage(),req.getVersionIndex());}catch(Exception e){out.accept(new ServerMessage("ERROR",req.getExecutionId(),null,e.getMessage(),null,null));return;}
  List<CompilerProperties.Account> list=accounts.eligibleAccounts();if(list.isEmpty()){out.accept(new ServerMessage("ERROR",req.getExecutionId(),null,"No configured JDoodle account has local quota capacity",null,null));return;}
  attempt(list,0,req,clientId,lang,out);
 }
 private void attempt(List<CompilerProperties.Account> list,int i,ClientMessage req,String clientId,JDoodleLanguageMapper.Mapped lang,Consumer<ServerMessage> out){
  if(i>=list.size()){out.accept(new ServerMessage("ERROR",req.getExecutionId(),null,"All JDoodle accounts are unavailable or quota exhausted",null,null));return;}
  var account=list.get(i);
  try{String token=tokens.create(account);accounts.recordExecutionReservation(account);
   bridge.open(token,req.getExecutionId(),req.getCode(),lang.language(),lang.versionIndex(),out,()->{accounts.markExhausted(account);attempt(list,i+1,req,clientId,lang,out);})
    .whenComplete((upstream,error)->{if(error!=null){String msg=String.valueOf(error.getMessage()).toLowerCase();if(msg.contains("429")||msg.contains("quota")){accounts.markExhausted(account);attempt(list,i+1,req,clientId,lang,out);}else out.accept(new ServerMessage("ERROR",req.getExecutionId(),null,"JDoodle connection failed: "+error.getMessage(),null,null));}
     else{active.put(req.getExecutionId(),upstream);owner.put(req.getExecutionId(),clientId);}});
  }catch(Exception e){String msg=String.valueOf(e.getMessage()).toLowerCase();if(msg.contains("429")||msg.contains("quota")){accounts.markExhausted(account);attempt(list,i+1,req,clientId,lang,out);}else out.accept(new ServerMessage("ERROR",req.getExecutionId(),null,"JDoodle token request failed: "+e.getMessage(),null,null));}
 }
 public void input(ClientMessage req,Consumer<ServerMessage> out){var c=active.get(req.getExecutionId());if(c==null)out.accept(new ServerMessage("ERROR",req.getExecutionId(),null,"No active execution for executionId",null,null));else c.input(req.getData());}
 public void cancel(ClientMessage req,Consumer<ServerMessage> out){out.accept(new ServerMessage("ERROR",req.getExecutionId(),null,"JDoodle documents no remote process-cancel message; closing this socket cannot guarantee the remote process is stopped",null,null));}
 public void cleanup(String clientId){for(String id:new ArrayList<>(owner.keySet()))if(clientId.equals(owner.get(id))){owner.remove(id);var c=active.remove(id);if(c!=null)c.close();}}
 private boolean blank(String s){return s==null||s.isBlank();}
}
