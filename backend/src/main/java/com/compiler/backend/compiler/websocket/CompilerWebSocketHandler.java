package com.compiler.backend.compiler.websocket;
import com.fasterxml.jackson.databind.ObjectMapper;import com.compiler.backend.compiler.dto.*;import com.compiler.backend.compiler.service.ExecutionOrchestrator;
import org.springframework.stereotype.Component;import org.springframework.web.socket.*;import org.springframework.web.socket.handler.TextWebSocketHandler;
@Component
public class CompilerWebSocketHandler extends TextWebSocketHandler{
 private final ObjectMapper json;private final ExecutionOrchestrator service;
 public CompilerWebSocketHandler(ObjectMapper j,ExecutionOrchestrator s){json=j;service=s;}
 @Override protected void handleTextMessage(WebSocketSession session,TextMessage msg)throws Exception{ClientMessage req;try{req=json.readValue(msg.getPayload(),ClientMessage.class);}catch(Exception e){send(session,new ServerMessage("ERROR",null,null,"Invalid JSON",null,null));return;}
  switch(req.getType()==null?"":req.getType().toUpperCase()){case "START"->service.start(req,session.getId(),m->send(session,m));case "INPUT"->service.input(req,m->send(session,m));case "CANCEL"->service.cancel(req,m->send(session,m));default->send(session,new ServerMessage("ERROR",req.getExecutionId(),null,"Unsupported message type",null,null));}}
 private void send(WebSocketSession s,ServerMessage m){if(!s.isOpen())return;try{synchronized(s){s.sendMessage(new TextMessage(json.writeValueAsString(m)));}}catch(Exception ignored){}}
 @Override public void afterConnectionClosed(WebSocketSession s,CloseStatus status){service.cleanup(s.getId());}
}
