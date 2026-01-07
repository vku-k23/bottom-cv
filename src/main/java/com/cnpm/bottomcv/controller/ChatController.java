package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.ChatRequest;
import com.cnpm.bottomcv.dto.response.ChatResponse;
import com.cnpm.bottomcv.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Chat API", description = "AI chatbot endpoints for job search assistance")
@RestController
@RequestMapping(value = "/api/v1", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/chat")
    @Operation(summary = "Send chat message", description = "Send a message to the AI chatbot and receive a response. Optionally include conversationId to maintain context.")
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatService.sendMessage(request);
        return ResponseEntity.ok(response);
    }
}

