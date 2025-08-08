package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.MessageRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.MessageResponse;

public interface MessageService {
    MessageResponse sendMessage(MessageRequest request);

    ListResponse<MessageResponse> listConversations(int pageNo, int pageSize);

    ListResponse<MessageResponse> getConversation(Long conversationId, int pageNo, int pageSize);
}