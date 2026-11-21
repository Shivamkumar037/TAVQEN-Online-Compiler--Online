package com.compiler.backend.controller.AiController;

import com.compiler.backend.dto.Res.ChatResponse;
import com.compiler.backend.dto.req.ChatRequest;
import com.compiler.backend.service.AI.ChatService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChatController {
private final ChatService chatService;
    @PostMapping("/chat")
    public ChatResponse chatController(@RequestBody ChatRequest chatRequest){
     return chatService.chatWithAi(chatRequest);
    }

}
