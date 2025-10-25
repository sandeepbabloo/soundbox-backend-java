package com.paymentalert.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentalert.backend.service.PhonePeService;
import com.paymentalert.backend.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {
    
    private final PhonePeService phonePeService;
    private final SubscriptionService subscriptionService;
    private final ObjectMapper objectMapper;
    
    /**
     * PhonePe webhook handler
     * POST /api/webhook/phonepe
     */
    @PostMapping("/phonepe")
    public ResponseEntity<Map<String, Object>> handlePhonePeWebhook(@RequestBody Map<String, Object> request) {
        try {
            String response = (String) request.get("response");
            String signature = (String) request.get("signature");
            
            log.info("PhonePe webhook received: {}", request);
            
            // Validate webhook signature
            if (!phonePeService.validateWebhookSignature(response, signature)) {
                log.error("Invalid webhook signature");
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid signature"));
            }
            
            // Decode the response
            String decodedResponse = new String(Base64.getDecoder().decode(response));
            Map<String, Object> webhookData = objectMapper.readValue(decodedResponse, Map.class);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
            String merchantTransactionId = (String) data.get("merchantTransactionId");
            String state = (String) data.get("state");
            
            log.info("Processing webhook for transaction: {}, state: {}", merchantTransactionId, state);
            
            // Handle different payment states
            switch (state) {
                case "COMPLETED":
                    handleSuccessfulPayment(merchantTransactionId, data);
                    break;
                case "FAILED":
                    handleFailedPayment(merchantTransactionId, data);
                    break;
                case "PENDING":
                    handlePendingPayment(merchantTransactionId, data);
                    break;
                default:
                    log.info("Unknown payment state: {}", state);
            }
            
            return ResponseEntity.ok(Map.of("success", true));
            
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Handle successful payment
     */
    private void handleSuccessfulPayment(String merchantTransactionId, Map<String, Object> paymentData) {
        try {
            log.info("Payment successful for transaction: {}", merchantTransactionId);
            
            // Update subscription status
            subscriptionService.updateSubscriptionStatus(merchantTransactionId, Map.of(
                "data", Map.of("state", "COMPLETED")
            ));
            
            // TODO: Send confirmation email/SMS to user
            // TODO: Activate premium features for user
            
        } catch (Exception e) {
            log.error("Error handling successful payment", e);
        }
    }
    
    /**
     * Handle failed payment
     */
    private void handleFailedPayment(String merchantTransactionId, Map<String, Object> paymentData) {
        try {
            log.info("Payment failed for transaction: {}", merchantTransactionId);
            
            // Update subscription status
            subscriptionService.updateSubscriptionStatus(merchantTransactionId, Map.of(
                "data", Map.of("state", "FAILED")
            ));
            
            // TODO: Send failure notification to user
            // TODO: Implement retry logic
            
        } catch (Exception e) {
            log.error("Error handling failed payment", e);
        }
    }
    
    /**
     * Handle pending payment
     */
    private void handlePendingPayment(String merchantTransactionId, Map<String, Object> paymentData) {
        try {
            log.info("Payment pending for transaction: {}", merchantTransactionId);
            
            // Keep subscription as pending
            // TODO: Set up polling to check payment status
            // TODO: Send pending notification to user
            
        } catch (Exception e) {
            log.error("Error handling pending payment", e);
        }
    }
    
    /**
     * Handle subscription renewal webhook
     * POST /api/webhook/phonepe/renewal
     */
    @PostMapping("/phonepe/renewal")
    public ResponseEntity<Map<String, Object>> handleRenewalWebhook(@RequestBody Map<String, Object> request) {
        try {
            String response = (String) request.get("response");
            String signature = (String) request.get("signature");
            
            log.info("PhonePe renewal webhook received: {}", request);
            
            // Validate webhook signature
            if (!phonePeService.validateWebhookSignature(response, signature)) {
                log.error("Invalid renewal webhook signature");
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid signature"));
            }
            
            // Decode the response
            String decodedResponse = new String(Base64.getDecoder().decode(response));
            Map<String, Object> webhookData = objectMapper.readValue(decodedResponse, Map.class);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
            String subscriptionId = (String) data.get("subscriptionId");
            String state = (String) data.get("state");
            
            log.info("Processing renewal webhook for subscription: {}, state: {}", subscriptionId, state);
            
            // Handle renewal payment
            if ("COMPLETED".equals(state)) {
                handleRenewalPayment(subscriptionId, data);
            } else if ("FAILED".equals(state)) {
                handleRenewalFailure(subscriptionId, data);
            }
            
            return ResponseEntity.ok(Map.of("success", true));
            
        } catch (Exception e) {
            log.error("Error processing renewal webhook", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Handle successful renewal payment
     */
    private void handleRenewalPayment(String subscriptionId, Map<String, Object> paymentData) {
        try {
            log.info("Renewal payment successful for subscription: {}", subscriptionId);
            
            // TODO: Update next billing date
            // TODO: Extend premium features
            // TODO: Send renewal confirmation to user
            
        } catch (Exception e) {
            log.error("Error handling renewal payment", e);
        }
    }
    
    /**
     * Handle renewal failure
     */
    private void handleRenewalFailure(String subscriptionId, Map<String, Object> paymentData) {
        try {
            log.info("Renewal payment failed for subscription: {}", subscriptionId);
            
            // TODO: Send failure notification to user
            // TODO: Implement retry logic
            // TODO: Consider subscription suspension after multiple failures
            
        } catch (Exception e) {
            log.error("Error handling renewal failure", e);
        }
    }
}

