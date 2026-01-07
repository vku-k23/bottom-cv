package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.ChatRequest;
import com.cnpm.bottomcv.dto.response.ChatResponse;

public interface ChatService {
    ChatResponse sendMessage(ChatRequest request);
}

