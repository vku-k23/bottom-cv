package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.dto.request.PaymentSessionRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.PaymentResponse;
import com.cnpm.bottomcv.service.PaymentService;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Override
    public PaymentResponse createCheckoutSession(PaymentSessionRequest request) {
        return PaymentResponse.builder().build();
    }

    @Override
    public void handleWebhook(String payload, String signature) {
        // TODO: implement payment webhook handling
    }

    @Override
    public ListResponse<PaymentResponse> listInvoices(int pageNo, int pageSize) {
        return ListResponse.<PaymentResponse>builder().build();
    }
}