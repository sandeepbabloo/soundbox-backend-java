package com.paymentalert.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentalert.backend.model.Payment;
import com.paymentalert.backend.model.Subscription;
import com.paymentalert.backend.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public Subscription createSubscription(Map<String, Object> request, Map<String, Object> phonepeResponse) {
        try {
            Subscription subscription = new Subscription();
            subscription.setUserId((String) request.get("userId"));
            subscription.setPhoneNumber((String) request.get("phoneNumber"));
            subscription.setPlanId((String) request.get("planId"));
            subscription.setPlanName((String) request.get("planName"));
            subscription.setAmount((Double) request.get("amount"));
            subscription.setBillingCycle((String) request.get("billingCycle"));
            subscription.setStatus("PENDING");
            
            // PhonePe details
            subscription.setPhonepeOrderId((String) phonepeResponse.get("orderId"));
            subscription.setPhonepeSubscriptionId((String) phonepeResponse.get("subscriptionId"));
            subscription.setPhonepeTransactionId((String) phonepeResponse.get("transactionId"));
            
            // Dates
            subscription.setStartDate(LocalDateTime.now());
            subscription.setEndDate(calculateEndDate((String) request.get("billingCycle")));
            subscription.setNextBillingDate(LocalDateTime.now().plusDays(7)); // 7-day trial
            subscription.setTrialEndDate(LocalDateTime.now().plusDays(7));
            
            return subscriptionRepository.save(subscription);
            
        } catch (Exception e) {
            log.error("Error creating subscription", e);
            throw new RuntimeException("Error creating subscription", e);
        }
    }
    
    @Transactional
    public void updateSubscriptionStatus(String transactionId, Map<String, Object> phonepeResponse) {
        try {
            Optional<Subscription> subscriptionOpt = subscriptionRepository.findByPhonepeTransactionId(transactionId);
            if (subscriptionOpt.isPresent()) {
                Subscription subscription = subscriptionOpt.get();
                
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) phonepeResponse.get("data");
                String state = (String) data.get("state");
                
                if ("COMPLETED".equals(state)) {
                    subscription.setStatus("ACTIVE");
                } else if ("FAILED".equals(state)) {
                    subscription.setStatus("FAILED");
                }
                
                subscriptionRepository.save(subscription);
            }
        } catch (Exception e) {
            log.error("Error updating subscription status", e);
        }
    }
    
    @Transactional
    public Payment createPayment(Map<String, Object> request, Map<String, Object> phonepeResponse) {
        try {
            Long subscriptionId = Long.valueOf((String) request.get("subscriptionId"));
            Subscription subscription = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new RuntimeException("Subscription not found"));
            
            Payment payment = new Payment();
            payment.setSubscription(subscription);
            payment.setUserId(subscription.getUserId());
            payment.setAmount((Double) request.get("amount"));
            payment.setBillingDate(LocalDateTime.now());
            payment.setDueDate(subscription.getNextBillingDate());
            payment.setPhonepeOrderId((String) request.get("merchantTransactionId"));
            payment.setStatus("PENDING");
            
            // Save payment details
            payment.setPhonepeResponse(objectMapper.writeValueAsString(phonepeResponse));
            
            return payment;
            
        } catch (Exception e) {
            log.error("Error creating payment", e);
            throw new RuntimeException("Error creating payment", e);
        }
    }
    
    @Transactional
    public void cancelSubscription(Long subscriptionId) {
        try {
            Subscription subscription = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new RuntimeException("Subscription not found"));
            
            subscription.setStatus("CANCELLED");
            subscriptionRepository.save(subscription);
            
        } catch (Exception e) {
            log.error("Error cancelling subscription", e);
            throw new RuntimeException("Error cancelling subscription", e);
        }
    }
    
    @Transactional
    public void pauseUnpauseSubscription(Long subscriptionId, String action) {
        try {
            Subscription subscription = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new RuntimeException("Subscription not found"));
            
            if ("pause".equals(action)) {
                subscription.setStatus("PAUSED");
            } else if ("unpause".equals(action)) {
                subscription.setStatus("ACTIVE");
            }
            
            subscriptionRepository.save(subscription);
            
        } catch (Exception e) {
            log.error("Error {} subscription", action, e);
            throw new RuntimeException("Error " + action + " subscription", e);
        }
    }
    
    public Subscription findById(Long id) {
        return subscriptionRepository.findById(id).orElse(null);
    }
    
    public List<Subscription> findByUserId(String userId) {
        return subscriptionRepository.findByUserId(userId);
    }
    
    public List<Subscription> findAll() {
        return subscriptionRepository.findAll();
    }
    
    private LocalDateTime calculateEndDate(String billingCycle) {
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
        
        return endDate;
    }
}

