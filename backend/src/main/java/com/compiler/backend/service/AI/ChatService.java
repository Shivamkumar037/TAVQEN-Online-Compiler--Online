package com.compiler.backend.service.AI;

import com.compiler.backend.AiModel.FallBackModel;
import com.compiler.backend.dto.Res.ChatResponse;
import com.compiler.backend.dto.req.ChatRequest;
import com.compiler.backend.service.AI.prompts.AiPrompts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final FallBackModel  fallBackModel;
    public ChatResponse chatWithAi(ChatRequest chatRequest){

        ChatResponse chatResponse = new ChatResponse();
        String query= AiPrompts.MASTER_PROMPT;


        switch(chatRequest.getMode()){

            case 1:
                query=query+AiPrompts.ASK_ANYTHING+chatRequest.getMessage();
                break;
                case 2:
                    query=query+AiPrompts.DSA+chatRequest.getMessage();
                    break;
                    case 3:
                        query=query+AiPrompts.CODE_GENRATION+chatRequest.getMessage();
                    break;
                    case 4:
                        query=query+AiPrompts.ERROR_FIXER+chatRequest.getMessage();
                    break;
                    case 5:
                        query=query+AiPrompts.DRY_RUN+chatRequest.getMessage();
                        break;
                        case 6:
                            query=query+AiPrompts.CODE_ANALYZE+chatRequest.getMessage();
                            break;
            default:
                query=query+AiPrompts.CODE_ANALYZE+chatRequest.getMessage();
                break;
        }



chatResponse.setMessage(fallBackModel.generateResponse(query));
        return chatResponse;
    }
}
