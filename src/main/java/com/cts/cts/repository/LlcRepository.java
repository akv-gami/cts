package com.cts.cts.repository;

import com.cts.cts.model.LlcEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LlcRepository extends JpaRepository<LlcEntity, Long> {
    List<LlcEntity> findByUserId(Long userId);
}