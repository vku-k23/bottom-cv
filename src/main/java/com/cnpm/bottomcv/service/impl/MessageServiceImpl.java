package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.dto.request.MessageRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.MessageResponse;
import com.cnpm.bottomcv.service.MessageService;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {
    @Override
    public MessageResponse sendMessage(MessageRequest request) {
        return MessageResponse.builder().build();
    }

    @Override
    public ListResponse<MessageResponse> listConversations(int pageNo, int pageSize) {
        return ListResponse.<MessageResponse>builder().build();
    }

    @Override
    public ListResponse<MessageResponse> getConversation(Long conversationId, int pageNo, int pageSize) {
        return ListResponse.<MessageResponse>builder().build();
    }
}