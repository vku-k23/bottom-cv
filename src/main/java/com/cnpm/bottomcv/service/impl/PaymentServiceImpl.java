package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.constant.SubscriptionStatus;
import com.cnpm.bottomcv.dto.request.PaymentSessionRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.PaymentResponse;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.model.Subscription;
import com.cnpm.bottomcv.repository.CompanyRepository;
import com.cnpm.bottomcv.repository.SubscriptionRepository;
import com.cnpm.bottomcv.service.PaymentService;
import com.cnpm.bottomcv.service.SubscriptionService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final SubscriptionService subscriptionService;
    private final SubscriptionRepository subscriptionRepository;
    private final CompanyRepository companyRepository;

    @Value("${bottom-cv.stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${bottom-cv.stripe.webhook-secret}")
    private String stripeWebhookSecret;

    @Value("${bottom-cv.client}")
    private String clientUrl;

    @Override
    @Transactional
    public PaymentResponse createCheckoutSession(PaymentSessionRequest request) {
        // Initialize Stripe with secret key
        Stripe.apiKey = stripeSecretKey;

        // Verify company exists
        companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", request.getCompanyId().toString()));

        // Calculate subscription period (1 month from now)
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime expiryDate = startDate.plusMonths(1);

        // Create subscription record with PENDING status initially
        Subscription subscription = subscriptionService.createSubscription(
                request.getCompanyId(),
                request.getPlanType(),
                startDate,
                expiryDate,
                null // sessionId will be set after Stripe session creation
        );

        // Create Stripe Checkout Session
        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(clientUrl + "/admin/payment/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(clientUrl + "/admin/payment/cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(request.getCurrency().toLowerCase())
                                                .setUnitAmount(request.getAmountMinor())
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Subscription Plan: " + request.getPlanType().getDisplayName())
                                                                .setDescription("Monthly subscription for " + request.getPlanType().getDisplayName() + " plan")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .putMetadata("companyId", request.getCompanyId().toString())
                .putMetadata("planType", request.getPlanType().getDisplayName())
                .putMetadata("subscriptionId", subscription.getId().toString());

        SessionCreateParams params = paramsBuilder.build();

        try {
            Session session = Session.create(params);

            // Update subscription with Stripe session ID
            subscription.setStripeSessionId(session.getId());
            subscriptionRepository.save(subscription);

            return PaymentResponse.builder()
                    .sessionId(session.getId())
                    .checkoutSessionUrl(session.getUrl())
                    .provider("STRIPE")
                    .status("PENDING")
                    .build();
        } catch (Exception e) {
            log.error("Error creating Stripe checkout session", e);
            // Mark subscription as failed
            subscriptionService.updateSubscriptionStatus(subscription.getId(), SubscriptionStatus.FAILED);
            throw new RuntimeException("Failed to create checkout session: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void handleWebhook(String payload, String signature) {
        Stripe.apiKey = stripeSecretKey;

        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Invalid webhook signature", e);
            throw new RuntimeException("Invalid webhook signature", e);
        } catch (Exception e) {
            log.error("Error parsing webhook event", e);
            throw new RuntimeException("Error parsing webhook event", e);
        }

        // Handle the event
        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;
            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded(event);
                break;
            case "payment_intent.payment_failed":
                handlePaymentIntentFailed(event);
                break;
            default:
                log.info("Unhandled event type: {}", event.getType());
        }
    }

    private void handleCheckoutSessionCompleted(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (session == null) {
            log.error("Session is null in checkout.session.completed event");
            return;
        }

        String sessionId = session.getId();
        String paymentIntentId = session.getPaymentIntent() != null ? session.getPaymentIntent().toString() : null;
        String customerId = session.getCustomer() != null ? session.getCustomer().toString() : null;

        log.info("Checkout session completed: {}", sessionId);

        // Update subscription from Stripe data
        try {
            subscriptionService.updateSubscriptionFromStripe(sessionId, paymentIntentId, customerId);
            log.info("Subscription updated successfully for session: {}", sessionId);
        } catch (Exception e) {
            log.error("Error updating subscription from Stripe webhook", e);
        }
    }

    private void handlePaymentIntentSucceeded(Event event) {
        com.stripe.model.PaymentIntent paymentIntent = (com.stripe.model.PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if (paymentIntent == null) {
            log.error("PaymentIntent is null in payment_intent.succeeded event");
            return;
        }

        log.info("Payment intent succeeded: {}", paymentIntent.getId());
        // Additional handling if needed
    }

    private void handlePaymentIntentFailed(Event event) {
        com.stripe.model.PaymentIntent paymentIntent = (com.stripe.model.PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if (paymentIntent == null) {
            log.error("PaymentIntent is null in payment_intent.payment_failed event");
            return;
        }

        log.error("Payment intent failed: {}", paymentIntent.getId());

        // Find subscription by payment intent and mark as failed
        subscriptionRepository.findByStripePaymentIntentId(paymentIntent.getId())
                .ifPresent(subscription -> {
                    subscriptionService.updateSubscriptionStatus(subscription.getId(), SubscriptionStatus.FAILED);
                    log.info("Subscription marked as failed: {}", subscription.getId());
                });
    }

    @Override
    @Transactional
    public PaymentResponse verifySession(String sessionId) {
        Stripe.apiKey = stripeSecretKey;

        try {
            // Retrieve session from Stripe
            Session session = Session.retrieve(sessionId);

            // Find subscription by session ID
            Subscription subscription = subscriptionRepository.findByStripeSessionId(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Subscription", "stripeSessionId", sessionId));

            // Check payment status
            if ("paid".equals(session.getPaymentStatus())) {
                // Payment is successful, activate subscription
                String paymentIntentId = session.getPaymentIntent() != null ? session.getPaymentIntent().toString() : null;
                String customerId = session.getCustomer() != null ? session.getCustomer().toString() : null;

                subscriptionService.updateSubscriptionFromStripe(sessionId, paymentIntentId, customerId);
                log.info("Subscription activated for session: {}", sessionId);

                return PaymentResponse.builder()
                        .id(subscription.getId())
                        .sessionId(sessionId)
                        .provider("STRIPE")
                        .status("PAID")
                        .referenceId(paymentIntentId)
                        .build();
            } else {
                // Payment not completed yet
                return PaymentResponse.builder()
                        .id(subscription.getId())
                        .sessionId(sessionId)
                        .provider("STRIPE")
                        .status(session.getPaymentStatus() != null ? session.getPaymentStatus().toUpperCase() : "PENDING")
                        .build();
            }
        } catch (Exception e) {
            log.error("Error verifying Stripe session: {}", sessionId, e);
            throw new RuntimeException("Failed to verify session: " + e.getMessage(), e);
        }
    }

    @Override
    public ListResponse<PaymentResponse> listInvoices(int pageNo, int pageSize) {
        // TODO: Implement invoice listing from Stripe
        return ListResponse.<PaymentResponse>builder().build();
    }
}
