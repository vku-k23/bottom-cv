package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.PaymentSessionRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse createCheckoutSession(PaymentSessionRequest request);

    void handleWebhook(String payload, String signature);

    ListResponse<PaymentResponse> listInvoices(int pageNo, int pageSize);
}