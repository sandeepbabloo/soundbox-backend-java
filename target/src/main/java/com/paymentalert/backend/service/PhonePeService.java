package com.paymentalert.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentalert.backend.config.PhonePeConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhonePeService {
    
    private final PhonePeConfig phonePeConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * Generate SHA256 hash for PhonePe API authentication
     */
    public String generateHash(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String data = payload + phonePeConfig.getSaltKey();
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Error generating hash", e);
            throw new RuntimeException("Error generating hash", e);
        }
    }
    
    /**
     * Generate X-VERIFY header for PhonePe API
     */
    public String generateVerifyHeader(String payload) {
        String hash = generateHash(payload);
        return hash + "###1";
    }
    
    /**
     * Create subscription setup request
     */
    public Map<String, Object> createSubscriptionSetup(Map<String, Object> subscriptionData) {
        try {
            String orderId = "ORDER_" + System.currentTimeMillis() + "_" + 
                           String.valueOf((int)(Math.random() * 1000000));
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("merchantId", phonePeConfig.getMerchant().getId());
            payload.put("merchantTransactionId", orderId);
            payload.put("amount", ((Double) subscriptionData.get("amount")) * 100); // Convert to paise
            payload.put("currency", "INR");
            payload.put("redirectUrl", phonePeConfig.getRedirectUrl());
            payload.put("redirectMode", "POST");
            payload.put("callbackUrl", phonePeConfig.getWebhookUrl());
            
            // User info
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("mobileNumber", subscriptionData.get("phoneNumber"));
            userInfo.put("userId", subscriptionData.get("userId"));
            payload.put("userInfo", userInfo);
            
            // Subscription info
            Map<String, Object> subscriptionInfo = new HashMap<>();
            subscriptionInfo.put("subscriptionId", "SUB_" + System.currentTimeMillis());
            subscriptionInfo.put("subscriptionType", "RECURRING");
            subscriptionInfo.put("billingCycle", subscriptionData.get("billingCycle"));
            subscriptionInfo.put("amount", ((Double) subscriptionData.get("amount")) * 100);
            subscriptionInfo.put("startDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            subscriptionInfo.put("endDate", calculateEndDate((String) subscriptionData.get("billingCycle")));
            subscriptionInfo.put("trialPeriod", subscriptionData.getOrDefault("trialDays", 7));
            payload.put("subscriptionInfo", subscriptionInfo);
            
            // Device info
            Map<String, Object> deviceInfo = new HashMap<>();
            deviceInfo.put("deviceType", "MOBILE");
            deviceInfo.put("os", "ANDROID");
            payload.put("deviceInfo", deviceInfo);
            
            String payloadString = objectMapper.writeValueAsString(payload);
            String base64Payload = Base64.getEncoder().encodeToString(payloadString.getBytes());
            String xVerify = generateVerifyHeader(base64Payload);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("request", base64Payload);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-VERIFY", xVerify);
            headers.set("accept", "application/json");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String url = phonePeConfig.getBaseUrl() + "/pg/v1/pay";
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("orderId", orderId);
            result.put("subscriptionId", subscriptionInfo.get("subscriptionId"));
            result.put("paymentUrl", extractPaymentUrl(response));
            result.put("transactionId", orderId);
            
            return result;
            
        } catch (Exception e) {
            log.error("PhonePe subscription setup error", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    /**
     * Check subscription status
     */
    public Map<String, Object> checkSubscriptionStatus(String merchantTransactionId) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("merchantId", phonePeConfig.getMerchant().getId());
            payload.put("merchantTransactionId", merchantTransactionId);
            
            String payloadString = objectMapper.writeValueAsString(payload);
            String base64Payload = Base64.getEncoder().encodeToString(payloadString.getBytes());
            String xVerify = generateVerifyHeader(base64Payload);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-VERIFY", xVerify);
            headers.set("accept", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = phonePeConfig.getBaseUrl() + "/pg/v1/status/" + 
                        phonePeConfig.getMerchant().getId() + "/" + merchantTransactionId;
            Map<String, Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class).getBody();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);
            return result;
            
        } catch (Exception e) {
            log.error("PhonePe status check error", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    /**
     * Execute subscription payment
     */
    public Map<String, Object> executeSubscriptionPayment(Map<String, Object> paymentData) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("merchantId", phonePeConfig.getMerchant().getId());
            payload.put("merchantTransactionId", paymentData.get("merchantTransactionId"));
            payload.put("amount", ((Double) paymentData.get("amount")) * 100);
            payload.put("currency", "INR");
            payload.put("subscriptionId", paymentData.get("subscriptionId"));
            
            String payloadString = objectMapper.writeValueAsString(payload);
            String base64Payload = Base64.getEncoder().encodeToString(payloadString.getBytes());
            String xVerify = generateVerifyHeader(base64Payload);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("request", base64Payload);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-VERIFY", xVerify);
            headers.set("accept", "application/json");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String url = phonePeConfig.getBaseUrl() + "/pg/v1/pay";
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);
            return result;
            
        } catch (Exception e) {
            log.error("PhonePe payment execution error", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    /**
     * Cancel subscription
     */
    public Map<String, Object> cancelSubscription(String subscriptionId) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("merchantId", phonePeConfig.getMerchant().getId());
            payload.put("subscriptionId", subscriptionId);
            
            String payloadString = objectMapper.writeValueAsString(payload);
            String base64Payload = Base64.getEncoder().encodeToString(payloadString.getBytes());
            String xVerify = generateVerifyHeader(base64Payload);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("request", base64Payload);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-VERIFY", xVerify);
            headers.set("accept", "application/json");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String url = phonePeConfig.getBaseUrl() + "/pg/v1/subscription/cancel";
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);
            return result;
            
        } catch (Exception e) {
            log.error("PhonePe subscription cancellation error", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    /**
     * Pause/Unpause subscription
     */
    public Map<String, Object> pauseUnpauseSubscription(String subscriptionId, String action) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("merchantId", phonePeConfig.getMerchant().getId());
            payload.put("subscriptionId", subscriptionId);
            payload.put("action", action.toUpperCase());
            
            String payloadString = objectMapper.writeValueAsString(payload);
            String base64Payload = Base64.getEncoder().encodeToString(payloadString.getBytes());
            String xVerify = generateVerifyHeader(base64Payload);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("request", base64Payload);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-VERIFY", xVerify);
            headers.set("accept", "application/json");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String url = phonePeConfig.getBaseUrl() + "/pg/v1/subscription/" + action.toLowerCase();
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);
            return result;
            
        } catch (Exception e) {
            log.error("PhonePe subscription {} error", action, e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    /**
     * Validate webhook signature
     */
    public boolean validateWebhookSignature(String payload, String signature) {
        String expectedSignature = generateHash(payload);
        return signature.equals(expectedSignature);
    }
    
    /**
     * Calculate subscription end date based on billing cycle
     */
    private String calculateEndDate(String billingCycle) {
        LocalDateTime endDate = LocalDateTime.now();
        
        switch (billingCycle) {
            case "MONTHLY":
                endDate = endDate.plusMonths(1);
                break;
            case "QUARTERLY":
                endDate = endDate.plusMonths(3);
                break;
            case "ANNUAL":
                endDate = endDate.plusYears(1);
                break;
        }
        
        return endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    /**
     * Extract payment URL from PhonePe response
     */
    @SuppressWarnings("unchecked")
    private String extractPaymentUrl(Map<String, Object> response) {
        try {
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            Map<String, Object> instrumentResponse = (Map<String, Object>) data.get("instrumentResponse");
            Map<String, Object> redirectInfo = (Map<String, Object>) instrumentResponse.get("redirectInfo");
            return (String) redirectInfo.get("url");
        } catch (Exception e) {
            log.error("Error extracting payment URL", e);
            return null;
        }
    }
}

