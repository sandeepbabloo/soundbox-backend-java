package com.paymentalert.backend.model;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String phoneNumber;
    
    @Column(nullable = false)
    private String planId;
    
    @Column(nullable = false)
    private String planName;
    
    @Column(nullable = false)
    private Double amount;
    
    @Column(nullable = false)
    private String currency = "INR";
    
    @Column(nullable = false)
    private String billingCycle;
    
    @Column(nullable = false)
    private String status = "PENDING";
    
    // PhonePe specific fields
    @Column(unique = true)
    private String phonepeSubscriptionId;
    
    @Column(unique = true)
    private String phonepeOrderId;
    
    @Column(unique = true)
    private String phonepeTransactionId;
    
    // Dates
    @Column(nullable = false)
    private LocalDateTime startDate;
    
    @Column(nullable = false)
    private LocalDateTime endDate;
    
    @Column(nullable = false)
    private LocalDateTime nextBillingDate;
    
    private LocalDateTime trialEndDate;
    
    // Payment information
    private String paymentMethod = "UPI_AUTOPAY";
    private String upiVpa;
    
    // Metadata
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;
    
    // Helper methods
    public boolean isActive() {
        return "ACTIVE".equals(status) && LocalDateTime.now().isBefore(endDate);
    }
    
    public boolean isInTrial() {
        return trialEndDate != null && LocalDateTime.now().isBefore(trialEndDate);
    }
    
    public long getDaysRemaining() {
        if (trialEndDate != null) {
            return java.time.Duration.between(LocalDateTime.now(), trialEndDate).toDays();
        }
        return 0;
    }
    
    public LocalDateTime calculateNextBillingDate() {
        LocalDateTime nextDate = nextBillingDate;
        
        switch (billingCycle) {
            case "MONTHLY":
                nextDate = nextDate.plusMonths(1);
                break;
            case "QUARTERLY":
                nextDate = nextDate.plusMonths(3);
                break;
            case "ANNUAL":
                nextDate = nextDate.plusYears(1);
                break;
        }
        
        return nextDate;
    }
    
    public boolean needsRenewal() {
        return "ACTIVE".equals(status) && LocalDateTime.now().isAfter(nextBillingDate);
    }
}
