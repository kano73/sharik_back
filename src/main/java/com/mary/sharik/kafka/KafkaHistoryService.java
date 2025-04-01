package com.mary.sharik.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mary.sharik.config.security.AuthenticatedMyUserService;
import com.mary.sharik.exceptions.ValidationFailedException;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.entity.OrdersHistory;
import com.mary.sharik.model.enums.KafkaTopicEnum;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@RequiredArgsConstructor
@Service
public class KafkaHistoryService {

    private final ObjectMapper objectMapper;
    private final KafkaRequesterService kafkaRequesterService;
    private final AuthenticatedMyUserService authenticatedMyUserService;

    @SneakyThrows
    public OrdersHistory findHistory() {
        MyUser user = authenticatedMyUserService.getCurrentUserAuthenticated();

        return getOrdersHistoryByUserId(user.getId());
    }

    @SneakyThrows
    public OrdersHistory getOrdersHistoryByUserId(String userId){
        String valueJson = objectMapper.writeValueAsString(userId);

        CompletableFuture<ConsumerRecord<String, String>> futureResponse =
                kafkaRequesterService.makeRequest(KafkaTopicEnum.HISTORY_VIEW_TOPIC.name(), valueJson);

        return futureResponse
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.value(), OrdersHistory.class);
                    } catch (JsonProcessingException e) {
                        throw new ValidationFailedException(e);
                    }
                })
                .exceptionally(ex -> {
                    throw new CompletionException(ex);
                }).join();
    }
}