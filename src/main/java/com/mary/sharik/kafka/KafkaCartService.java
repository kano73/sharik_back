package com.mary.sharik.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.mary.sharik.config.security.AuthenticatedMyUserService;
import com.mary.sharik.exception.MicroserviceExternalException;
import com.mary.sharik.exception.ValidationFailedException;
import com.mary.sharik.model.dto.request.ActionWithCartDTO;
import com.mary.sharik.model.dto.request.OrderDetailsDTO;
import com.mary.sharik.model.dto.storage.ProductAndQuantity;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.enumClass.KafkaTopic;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class KafkaCartService {

    private final ObjectMapper objectMapper;
    private final KafkaRequesterService kafkaRequesterService;
    private final AuthenticatedMyUserService authenticatedMyUserService;

    @SneakyThrows
    public boolean emptyCart(){
        MyUser user = authenticatedMyUserService.getCurrentUserAuthenticated();

        String valueJson = objectMapper.writeValueAsString(user.getId());

        CompletableFuture<ConsumerRecord<String, String>> futureResponse =
                kafkaRequesterService.makeRequest(KafkaTopic.CART_EMPTY_TOPIC.name(), valueJson);

        return futureResponse
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.value(), Boolean.class);
                    } catch (JsonProcessingException e) {
                        throw new ValidationFailedException(e);
                    }
                })
                .exceptionally(ex -> {
                    throw new MicroserviceExternalException(ex.getMessage());
                }).join();
    }

    @SneakyThrows
    public boolean addToCart(ActionWithCartDTO actionWithCartDTO) {
        MyUser user = authenticatedMyUserService.getCurrentUserAuthenticated();
        actionWithCartDTO.setUserId(user.getId());

        String valueJson = objectMapper.writeValueAsString(actionWithCartDTO);

        CompletableFuture<ConsumerRecord<String, String>> futureResponse =
                kafkaRequesterService.makeRequest(KafkaTopic.CART_ADD_TOPIC.name(), valueJson);

        return futureResponse
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.value(), Boolean.class);
                    } catch (JsonProcessingException e) {
                        throw new ValidationFailedException(e);
                    }
                })
                .exceptionally(ex -> {
                    throw new MicroserviceExternalException(ex.getMessage());
                }).join();
    }

    @SneakyThrows
    public boolean changeAmount(ActionWithCartDTO actionWithCartDTO) {
        MyUser user = authenticatedMyUserService.getCurrentUserAuthenticated();
        actionWithCartDTO.setUserId(user.getId());

        String valueJson = objectMapper.writeValueAsString(actionWithCartDTO);

        CompletableFuture<ConsumerRecord<String, String>> futureResponse =
                kafkaRequesterService.makeRequest(KafkaTopic.CART_CHANGE_AMOUNT_TOPIC.name(), valueJson);

        return futureResponse
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.value(), Boolean.class);
                    } catch (JsonProcessingException e) {
                        throw new ValidationFailedException(e);
                    }
                })
                .exceptionally(ex -> {
                    throw new MicroserviceExternalException(ex.getMessage());
                }).join();
    }

    @SneakyThrows
    public boolean makeOrder(OrderDetailsDTO orderDetailsDTO) {
        MyUser user = authenticatedMyUserService.getCurrentUserAuthenticated();
        orderDetailsDTO.setUserId(user.getId());

        String valueJson = objectMapper.writeValueAsString(orderDetailsDTO);

        CompletableFuture<ConsumerRecord<String, String>> futureResponse =
                kafkaRequesterService.makeRequest(KafkaTopic.CART_ORDER_TOPIC.name(), valueJson);

        return futureResponse
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.value(), Boolean.class);
                    } catch (JsonProcessingException e) {
                        throw new ValidationFailedException(e);
                    }
                })
                .exceptionally(ex -> {
                    throw new MicroserviceExternalException(ex.getMessage());
                }).join();
    }

    @SneakyThrows
    public List<ProductAndQuantity> findCart() {
        MyUser user = authenticatedMyUserService.getCurrentUserAuthenticated();
        return getCartOfUserById(user.getId());
    }

    @SneakyThrows
    public List<ProductAndQuantity> getCartOfUserById(@NotBlank String id) {
        String valueJson = objectMapper.writeValueAsString(id);

        CompletableFuture<ConsumerRecord<String, String>> futureResponse =
                kafkaRequesterService.makeRequest(KafkaTopic.CART_VIEW_TOPIC.name(), valueJson);

        return (List<ProductAndQuantity>) futureResponse
                .thenApply(response -> {
                    CollectionType listType = objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, ProductAndQuantity.class);
                    try {
                        return objectMapper.readValue(response.value(), listType);
                    } catch (JsonProcessingException e) {
                        log.error(String.valueOf(e));
                        throw new ValidationFailedException(e);
                    }
                })
                .exceptionally(ex -> {
                    throw new MicroserviceExternalException(ex.getMessage());
                }).join();
    }
}