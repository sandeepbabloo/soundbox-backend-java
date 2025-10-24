package com.paymentalert.backend.controller;

import com.paymentalert.backend.model.Payment;
import com.paymentalert.backend.model.Subscription;
import com.paymentalert.backend.service.PhonePeService;
import com.paymentalert.backend.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/phonepe")
@RequiredArgsConstructor
@Slf4j
public class PhonePeController {
    
    private final PhonePeService phonePeService;
    private final SubscriptionService subscriptionService;
    
    /**
     * Create subscription setup
     * POST /api/phonepe/subscription/setup
     */
    @PostMapping("/subscription/setup")
    public ResponseEntity<Map<String, Object>> createSubscription(@RequestBody Map<String, Object> request) {
        try {
            log.info("Creating subscription for user: {}", request.get("userId"));
            
            // Create subscription setup with PhonePe
            Map<String, Object> phonepeResponse = phonePeService.createSubscriptionSetup(request);
            
            if (!(Boolean) phonepeResponse.get("success")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", phonepeResponse.get("error")
                ));
            }
            
            // Save subscription to database
            Subscription subscription = subscriptionService.createSubscription(request, phonepeResponse);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "subscriptionId", subscription.getId(),
                    "phonepeOrderId", phonepeResponse.get("orderId"),
                    "paymentUrl", phonepeResponse.get("paymentUrl"),
                    "transactionId", phonepeResponse.get("transactionId")
                )
            ));
            
        } catch (Exception e) {
            log.error("Error creating subscription", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Internal server error"
            ));
        }
    }
    
    /**
     * Check subscription status
     * GET /api/phonepe/subscription/status/{transactionId}
     */
    @GetMapping("/subscription/status/{transactionId}")
    public ResponseEntity<Map<String, Object>> checkSubscriptionStatus(@PathVariable String transactionId) {
        try {
            log.info("Checking subscription status for transaction: {}", transactionId);
            
            // Check with PhonePe
            Map<String, Object> phonepeResponse = phonePeService.checkSubscriptionStatus(transactionId);
            
            if (!(Boolean) phonepeResponse.get("success")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", phonepeResponse.get("error")
                ));
            }
            
            // Update local subscription status
            subscriptionService.updateSubscriptionStatus(transactionId, phonepeResponse);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", phonepeResponse.get("data")
            ));
            
        } catch (Exception e) {
            log.error("Error checking subscription status", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Internal server error"
            ));
        }
    }
    
    /**
     * Execute subscription payment
     * POST /api/phonepe/subscription/execute
     */
    @PostMapping("/subscription/execute")
    public ResponseEntity<Map<String, Object>> executePayment(@RequestBody Map<String, Object> request) {
        try {
            log.info("Executing payment for subscription: {}", request.get("subscriptionId"));
            
            // Execute payment with PhonePe
            Map<String, Object> phonepeResponse = phonePeService.executeSubscriptionPayment(request);
            
            if (!(Boolean) phonepeResponse.get("success")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", phonepeResponse.get("error")
                ));
            }
            
            // Create payment record
            Payment payment = subscriptionService.createPayment(request, phonepeResponse);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "paymentId", payment.getId(),
                    "transactionId", phonepeResponse.get("transactionId"),
                    "status", payment.getStatus()
                )
            ));
            
        } catch (Exception e) {
            log.error("Error executing payment", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Internal server error"
            ));
        }
    }
    
    /**
     * Cancel subscription
     * POST /api/phonepe/subscription/cancel
     */
    @PostMapping("/subscription/cancel")
    public ResponseEntity<Map<String, Object>> cancelSubscription(@RequestBody Map<String, Object> request) {
        try {
            String subscriptionId = (String) request.get("subscriptionId");
            log.info("Cancelling subscription: {}", subscriptionId);
            
            // Get subscription from database
            Subscription subscription = subscriptionService.findById(Long.valueOf(subscriptionId));
            if (subscription == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Cancel with PhonePe
            Map<String, Object> phonepeResponse = phonePeService.cancelSubscription(subscription.getPhonepeSubscriptionId());
            
            if (!(Boolean) phonepeResponse.get("success")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", phonepeResponse.get("error")
                ));
            }
            
            // Update local subscription
            subscriptionService.cancelSubscription(Long.valueOf(subscriptionId));
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Subscription cancelled successfully"
            ));
            
        } catch (Exception e) {
            log.error("Error cancelling subscription", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Internal server error"
            ));
        }
    }
    
    /**
     * Pause/Unpause subscription
     * POST /api/phonepe/subscription/{action}
     */
    @PostMapping("/subscription/{action}")
    public ResponseEntity<Map<String, Object>> pauseUnpauseSubscription(
            @PathVariable String action,
            @RequestBody Map<String, Object> request) {
        try {
            String subscriptionId = (String) request.get("subscriptionId");
            log.info("{} subscription: {}", action, subscriptionId);
            
            if (!"pause".equals(action) && !"unpause".equals(action)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Invalid action. Use pause or unpause"
                ));
            }
            
            // Get subscription from database
            Subscription subscription = subscriptionService.findById(Long.valueOf(subscriptionId));
            if (subscription == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Pause/Unpause with PhonePe
            Map<String, Object> phonepeResponse = phonePeService.pauseUnpauseSubscription(
                    subscription.getPhonepeSubscriptionId(), action);
            
            if (!(Boolean) phonepeResponse.get("success")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", phonepeResponse.get("error")
                ));
            }
            
            // Update local subscription
            subscriptionService.pauseUnpauseSubscription(Long.valueOf(subscriptionId), action);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Subscription " + action + "d successfully"
            ));
            
        } catch (Exception e) {
            log.error("Error {} subscription", action, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Internal server error"
            ));
        }
    }
}

