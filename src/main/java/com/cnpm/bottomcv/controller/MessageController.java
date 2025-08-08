package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.MessageRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.MessageResponse;
import com.cnpm.bottomcv.service.MessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Messaging API", description = "Skeleton endpoints for candidate-employer messaging")
@RestController
@RequestMapping(value = "/api/v1/front/messages", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CANDIDATE','EMPLOYER')")
    public ResponseEntity<MessageResponse> sendMessage(@Valid @RequestBody MessageRequest request) {
        var response = messageService.sendMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/conversations")
    @PreAuthorize("hasAnyRole('CANDIDATE','EMPLOYER')")
    public ResponseEntity<ListResponse<MessageResponse>> listConversations(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        var response = messageService.listConversations(pageNo, pageSize);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/{id}")
    @PreAuthorize("hasAnyRole('CANDIDATE','EMPLOYER')")
    public ResponseEntity<ListResponse<MessageResponse>> getConversation(@PathVariable Long id,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "50") int pageSize) {
        var response = messageService.getConversation(id, pageNo, pageSize);
        return ResponseEntity.ok(response);
    }
}