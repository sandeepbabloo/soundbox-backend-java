package com.paymentalert.backend.repository;

import com.paymentalert.backend.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    List<Subscription> findByUserId(String userId);
    
    List<Subscription> findByStatus(String status);
    
    Optional<Subscription> findByPhonepeSubscriptionId(String phonepeSubscriptionId);
    
    Optional<Subscription> findByPhonepeOrderId(String phonepeOrderId);
    
    Optional<Subscription> findByPhonepeTransactionId(String phonepeTransactionId);
    
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.nextBillingDate <= CURRENT_TIMESTAMP")
    List<Subscription> findSubscriptionsNeedingRenewal();
    
    @Query("SELECT s FROM Subscription s WHERE s.userId = :userId AND s.status IN ('ACTIVE', 'PAUSED')")
    List<Subscription> findActiveSubscriptionsByUserId(String userId);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = 'ACTIVE'")
    Long countActiveSubscriptions();
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = 'CANCELLED'")
    Long countCancelledSubscriptions();
}

