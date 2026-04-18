package com.cts.cts.repository;

import com.cts.cts.model.PaymentEntity;
import com.cts.cts.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByFlowToken(String flowToken);
    Optional<PaymentEntity> findByCommerceOrder(String commerceOrder);
    List<PaymentEntity> findByUserId(Long userId);
    Optional<PaymentEntity> findTopByLlcIdOrderByCreatedAtDesc(Long llcId);
    boolean existsByLlcIdAndStatus(Long llcId, PaymentStatus status);
}