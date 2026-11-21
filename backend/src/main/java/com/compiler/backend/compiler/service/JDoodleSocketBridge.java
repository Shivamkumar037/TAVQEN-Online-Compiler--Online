package com.compiler.backend.compiler.service;
import com.fasterxml.jackson.databind.JsonNode; import com.fasterxml.jackson.databind.ObjectMapper;
import com.compiler.backend.compiler.config.CompilerProperties; import com.compiler.backend.compiler.dto.ServerMessage;
import org.springframework.messaging.converter.StringMessageConverter; import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler; import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHttpHeaders; import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.sockjs.client.*; import org.springframework.web.socket.messaging.WebSocketStompClient;
import java.lang.reflect.Type; import java.util.List; import java.util.Map; import java.util.concurrent.*; import java.util.concurrent.atomic.AtomicBoolean; import java.util.function.Consumer;
@Component
public class JDoodleSocketBridge {
 private final CompilerProperties props; private final ObjectMapper json;
 public JDoodleSocketBridge(CompilerProperties p,ObjectMapper j){props=p;json=j;}
 public CompletableFuture<Upstream> open(String token,String executionId,String code,String language,Integer versionIndex,Consumer<ServerMessage> out,Runnable quota){
  CompletableFuture<Upstream> future=new CompletableFuture<>(); ThreadPoolTaskScheduler scheduler=new ThreadPoolTaskScheduler(); scheduler.setPoolSize(1); scheduler.setThreadNamePrefix("jdoodle-stomp-"); scheduler.initialize();
  SockJsClient sock=new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient())));
  WebSocketStompClient stomp=new WebSocketStompClient(sock); stomp.setMessageConverter(new StringMessageConverter()); stomp.setTaskScheduler(scheduler); stomp.setDefaultHeartbeat(new long[]{0,0});
  StompSessionHandlerAdapter handler=new StompSessionHandlerAdapter(){
   @Override public void afterConnected(StompSession session,StompHeaders headers){
    Upstream upstream=new Upstream(session,stomp,scheduler,token,executionId,out); future.complete(upstream);
    session.subscribe("/user/queue/execute-i",new StompFrameHandler(){
     public Type getPayloadType(StompHeaders h){return String.class;}
     public void handleFrame(StompHeaders h,Object payload){int status=parse(h);String body=payload==null?"":payload.toString();
      if(status==429){quota.run();upstream.close();return;}
      if(status==201){out.accept(new ServerMessage("STATUS",executionId,"RUNNING",null,null,null));return;}
      if(status==204){out.accept(new ServerMessage("STATUS",executionId,"EXECUTION_TIME_MS="+elapsed(body),null,null,elapsed(body)));return;}
      if(status==410){out.accept(new ServerMessage("EXIT",executionId,null,null,0,null));upstream.close();return;}
      if(status==400||status==401||status==500){out.accept(new ServerMessage("ERROR",executionId,null,"JDoodle returned status "+status+": "+safe(body),null,null));upstream.close();return;}
      String text=extract(body); if(!text.isEmpty())out.accept(new ServerMessage("OUTPUT",executionId,text,null,null,null));
     }
    });
    try{StompHeaders sh=new StompHeaders();sh.setDestination("/app/execute-ws-api-token");sh.add("message_type","execute");sh.add("token",token);
     session.send(sh,json.writeValueAsString(Map.of("script",code,"language",language,"versionIndex",versionIndex)));
     out.accept(new ServerMessage("STATUS",executionId,"SUBMITTED",null,null,null));
    }catch(Exception e){out.accept(new ServerMessage("ERROR",executionId,null,"Unable to submit JDoodle execution",null,null));upstream.close();}
   }
   @Override public void handleTransportError(StompSession session,Throwable ex){if(!future.isDone())future.completeExceptionally(ex);else out.accept(new ServerMessage("ERROR",executionId,null,"JDoodle WebSocket transport error",null,null));}
   @Override public void handleException(StompSession session,StompCommand command,StompHeaders h,byte[] payload,Throwable ex){if(!future.isDone())future.completeExceptionally(ex);}
  };
  try{stomp.connectAsync(props.getJdoodle().getStompUrl(),new WebSocketHttpHeaders(),handler);}
  catch(Exception e){future.completeExceptionally(e);}
  future.orTimeout(props.getJdoodle().getConnectTimeoutSeconds(),TimeUnit.SECONDS).whenComplete((v,e)->{if(e!=null){stomp.stop();scheduler.shutdown();}});
  return future;
 }
 private int parse(StompHeaders h){try{return Integer.parseInt(h.getFirst("statusCode"));}catch(Exception e){return -1;}}
 private String extract(String b){try{JsonNode n=json.readTree(b);if(n.isTextual())return n.asText();if(n.has("output"))return n.get("output").asText("");}catch(Exception ignored){}return b;}
 private Long elapsed(String b){try{JsonNode n=json.readTree(b);if(n.has("cpuTime"))return (long)(Double.parseDouble(n.get("cpuTime").asText())*1000);}catch(Exception ignored){}return null;}
 private String safe(String s){return s.length()>1000?s.substring(0,1000):s;}
 public static class Upstream{
  private final StompSession session;private final WebSocketStompClient client;private final ThreadPoolTaskScheduler scheduler;private final String token,id;private final Consumer<ServerMessage> out;private final AtomicBoolean closed=new AtomicBoolean();
  Upstream(StompSession s,WebSocketStompClient c,ThreadPoolTaskScheduler t,String token,String id,Consumer<ServerMessage> o){session=s;client=c;scheduler=t;this.token=token;this.id=id;out=o;}
  public void input(String text){if(closed.get()||!session.isConnected()){out.accept(new ServerMessage("ERROR",id,null,"No active JDoodle execution",null,null));return;}
   StompHeaders h=new StompHeaders();h.setDestination("/app/execute-ws-api-token");h.add("message_type","input");h.add("token",token);session.send(h,text==null?"":text);}
  public void close(){if(!closed.compareAndSet(false,true))return;try{if(session.isConnected())session.disconnect();}catch(Exception ignored){}try{client.stop();}catch(Exception ignored){}scheduler.shutdown();}
 }
}
