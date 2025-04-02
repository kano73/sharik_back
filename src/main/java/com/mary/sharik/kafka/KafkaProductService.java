package com.mary.sharik.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.mary.sharik.exception.MicroserviceExternalException;
import com.mary.sharik.exception.ValidationFailedException;
import com.mary.sharik.model.dto.request.AddProductDTO;
import com.mary.sharik.model.dto.request.ProductSearchFilterDTO;
import com.mary.sharik.model.dto.request.SetProductStatusDTO;
import com.mary.sharik.model.entity.Product;
import com.mary.sharik.model.enumClass.KafkaTopic;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
public class KafkaProductService {

    private final ObjectMapper objectMapper;
    private final KafkaRequesterService kafkaRequesterService;

    @SneakyThrows
    public List<Product> requestProductsByFilter(ProductSearchFilterDTO filter) {
        // Преобразуем фильтр в JSON
        String valueJson = objectMapper.writeValueAsString(filter);

        CompletableFuture<ConsumerRecord<String, String>> futureResponse =
                kafkaRequesterService.makeRequest(KafkaTopic.PRODUCT_BY_FILTER_TOPIC.name(), valueJson);

        return (List<Product>) futureResponse
                .thenApply(response -> {

                    CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, Product.class);
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

    @SneakyThrows
    public Boolean createProduct(AddProductDTO dto) {
        // Преобразуем фильтр в JSON
        String valueJson = objectMapper.writeValueAsString(dto);

        CompletableFuture<ConsumerRecord<String, String>> futureResponse =
                kafkaRequesterService.makeRequest(KafkaTopic.PRODUCT_CREATE_TOPIC.name(), valueJson);

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
    public Product requestProductsById(String id) {
        // Преобразуем фильтр в JSON
        String valueJson = objectMapper.writeValueAsString(id);

        CompletableFuture<ConsumerRecord<String, String>> futureResponse =
                kafkaRequesterService.makeRequest(KafkaTopic.PRODUCT_BY_ID_TOPIC.name(), valueJson);

        return futureResponse
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.value(), Product.class);
                    } catch (JsonProcessingException e) {
                        throw new ValidationFailedException(e);
                    }
                })
                .exceptionally(ex -> {
                    throw new MicroserviceExternalException(ex.getMessage());
                }).join();
    }

    @SneakyThrows
    public boolean setProductStatus(@Valid @NotNull SetProductStatusDTO dto) {
        // Преобразуем фильтр в JSON
        String valueJson = objectMapper.writeValueAsString(dto);

        CompletableFuture<ConsumerRecord<String, String>> futureResponse =
                kafkaRequesterService.makeRequest(KafkaTopic.PRODUCT_SET_STATUS_TOPIC.name(), valueJson);

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
}