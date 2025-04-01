package com.mary.sharik.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.mary.sharik.config.security.AuthenticatedMyUserService;
import com.mary.sharik.exceptions.MicroserviceExternalException;
import com.mary.sharik.exceptions.ValidationFailedException;
import com.mary.sharik.model.dto.request.ActionWithCartDTO;
import com.mary.sharik.model.dto.request.AddProductDTO;
import com.mary.sharik.model.dto.request.OrderDetailsDTO;
import com.mary.sharik.model.dto.request.ProductSearchFilterDTO;
import com.mary.sharik.model.dto.storage.ProductAndQuantity;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.entity.Product;
import com.mary.sharik.model.enums.KafkaTopicEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

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
                kafkaRequesterService.makeRequest(KafkaTopicEnum.CART_EMPTY_TOPIC.name(), valueJson);

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
                kafkaRequesterService.makeRequest(KafkaTopicEnum.CART_ADD_TOPIC.name(), valueJson);

        return futureResponse
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.value(), Boolean.class);
                    } catch (JsonProcessingException e) {
                        throw new ValidationFailedException(e);
                    }
                })
                .exceptionally(ex -> {
                    System.out.println("Exception in exceptionally: " + ex.getMessage());
                    throw new MicroserviceExternalException(ex.getMessage());
                }).join();
    }

    @SneakyThrows
    public boolean changeAmount(ActionWithCartDTO actionWithCartDTO) {
        MyUser user = authenticatedMyUserService.getCurrentUserAuthenticated();
        actionWithCartDTO.setUserId(user.getId());

        String valueJson = objectMapper.writeValueAsString(actionWithCartDTO);

        CompletableFuture<ConsumerRecord<String, String>> futureResponse =
                kafkaRequesterService.makeRequest(KafkaTopicEnum.CART_CHANGE_AMOUNT_TOPIC.name(), valueJson);

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
                kafkaRequesterService.makeRequest(KafkaTopicEnum.CART_ORDER_TOPIC.name(), valueJson);

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
                kafkaRequesterService.makeRequest(KafkaTopicEnum.CART_VIEW_TOPIC.name(), valueJson);

        return (List<ProductAndQuantity>) futureResponse
                .thenApply(response -> {
                    CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, ProductAndQuantity.class);
                    try {
                        return objectMapper.readValue(response.value(), listType);
                    } catch (JsonProcessingException e) {
                        throw new ValidationFailedException(e);
                    }
                })
                .exceptionally(ex -> {

                    throw new MicroserviceExternalException(ex.getMessage());
                }).join();
    }
}