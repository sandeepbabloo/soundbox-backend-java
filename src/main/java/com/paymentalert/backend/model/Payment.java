package com.paymentalert.backend.model;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private Double amount;
    
    @Column(nullable = false)
    private String currency = "INR";
    
    @Column(nullable = false)
    private String paymentMethod = "UPI_AUTOPAY";
    
    // PhonePe specific fields
    @Column(unique = true)
    private String phonepeTransactionId;
    
    @Column(unique = true)
    private String phonepeOrderId;
    
    @Column(unique = true)
    private String phonepePaymentId;
    
    // Payment status
    @Column(nullable = false)
    private String status = "PENDING";
    
    // Payment response from PhonePe
    @Column(columnDefinition = "TEXT")
    private String phonepeResponse;
    
    // Error details if payment failed
    private String errorCode;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    // Billing information
    @Column(nullable = false)
    private LocalDateTime billingDate;
    
    @Column(nullable = false)
    private LocalDateTime dueDate;
    
    // Retry information
    @Column(nullable = false)
    private Integer retryCount = 0;
    
    @Column(nullable = false)
    private Integer maxRetries = 3;
    
    // Metadata
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    private LocalDateTime paidAt;
    
    // Helper methods
    public boolean isSuccessful() {
        return "SUCCESS".equals(status);
    }
    
    public boolean isPending() {
        return "PENDING".equals(status);
    }
    
    public boolean canRetry() {
        return "FAILED".equals(status) && retryCount < maxRetries;
    }
    
    public void markAsSuccessful(String phonepeResponse) {
        this.status = "SUCCESS";
        this.phonepeResponse = phonepeResponse;
        this.paidAt = LocalDateTime.now();
    }
    
    public void markAsFailed(String errorCode, String errorMessage) {
        this.status = "FAILED";
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.retryCount += 1;
    }
}
