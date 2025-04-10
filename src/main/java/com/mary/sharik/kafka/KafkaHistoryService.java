package com.mary.sharik.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.mary.sharik.config.security.AuthenticatedMyUserService;
import com.mary.sharik.exception.MicroserviceExternalException;
import com.mary.sharik.exception.ValidationFailedException;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.entity.OrdersHistory;
import com.mary.sharik.model.enumClass.KafkaTopic;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
public class KafkaHistoryService {

    private final ObjectMapper objectMapper;
    private final KafkaRequesterService kafkaRequesterService;
    private final AuthenticatedMyUserService authenticatedMyUserService;

    @SneakyThrows
    public List<OrdersHistory> getWholeHistory(@Min(1) Integer page) {
        String valueJson = objectMapper.writeValueAsString(page);

        CompletableFuture<ConsumerRecord<String, String>> futureResponse = kafkaRequesterService.makeRequest(KafkaTopic.HISTORY_ALL_TOPIC.name(), valueJson);

        return (List<OrdersHistory>) futureResponse.thenApply(response -> {
            try {
                CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, OrdersHistory.class);
                return objectMapper.readValue(response.value(), listType);
            } catch (JsonProcessingException e) {
                throw new ValidationFailedException(e);
            }
        }).exceptionally(ex -> {
            throw new MicroserviceExternalException(ex.getMessage());
        }).join();
    }

    @SneakyThrows
    public OrdersHistory findHistory() {
        MyUser user = authenticatedMyUserService.getCurrentUserAuthenticated();

        return getOrdersHistoryByUserId(user.getId());
    }

    @SneakyThrows
    public OrdersHistory getOrdersHistoryByUserId(String userId) {
        String valueJson = objectMapper.writeValueAsString(userId);

        CompletableFuture<ConsumerRecord<String, String>> futureResponse = kafkaRequesterService.makeRequest(KafkaTopic.HISTORY_VIEW_TOPIC.name(), valueJson);

        return futureResponse.thenApply(response -> {
            try {
                return objectMapper.readValue(response.value(), OrdersHistory.class);
            } catch (JsonProcessingException e) {
                throw new ValidationFailedException(e);
            }
        }).exceptionally(ex -> {
            throw new MicroserviceExternalException(ex.getMessage());
        }).join();
    }
}