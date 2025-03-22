package com.mary.sharik.repository;

import com.mary.sharik.model.entity.OrdersHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OrdersHistoryRepository extends MongoRepository<OrdersHistory, String> {
    Optional<OrdersHistory> findByUserId(String userId);
}

