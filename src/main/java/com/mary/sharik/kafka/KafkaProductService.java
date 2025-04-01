package com.mary.sharik.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.mary.sharik.exceptions.ValidationFailedException;
import com.mary.sharik.model.dto.request.AddProductDTO;
import com.mary.sharik.model.dto.request.ProductSearchFilterDTO;
import com.mary.sharik.model.entity.OrdersHistory;
import com.mary.sharik.model.entity.Product;
import com.mary.sharik.model.enums.KafkaTopicEnum;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@RequiredArgsConstructor
@Service
public class KafkaProductService {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private KafkaRequesterService kafkaRequesterService;

    public List<Product> requestProductsByFilter(ProductSearchFilterDTO filter) throws Exception {
        // Преобразуем фильтр в JSON
        String valueJson = objectMapper.writeValueAsString(filter);

        CompletableFuture<ConsumerRecord<String, String>> futureResponse =
                kafkaRequesterService.makeRequest(KafkaTopicEnum.PRODUCT_BY_FILTER_TOPIC.name(), valueJson);

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
                    throw new CompletionException(ex);
                }).join();
    }

    public Boolean createProduct(AddProductDTO dto) throws Exception {
        // Преобразуем фильтр в JSON
        String valueJson = objectMapper.writeValueAsString(dto);

        CompletableFuture<ConsumerRecord<String, String>> futureResponse =
                kafkaRequesterService.makeRequest(KafkaTopicEnum.PRODUCT_CREATE_TOPIC.name(), valueJson);

        return futureResponse
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.value(), Boolean.class);
                    } catch (JsonProcessingException e) {
                        throw new ValidationFailedException(e);
                    }
                })
                .exceptionally(ex -> {
                    throw new CompletionException(ex);
                }).join();
    }

    public Product requestProductsById(String id) throws Exception {
        // Преобразуем фильтр в JSON
        String valueJson = objectMapper.writeValueAsString(id);

        CompletableFuture<ConsumerRecord<String, String>> futureResponse =
                kafkaRequesterService.makeRequest(KafkaTopicEnum.PRODUCT_BY_ID_TOPIC.name(), valueJson);

        return futureResponse
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.value(), Product.class);
                    } catch (JsonProcessingException e) {
                        throw new ValidationFailedException(e);
                    }
                })
                .exceptionally(ex -> {
                    throw new CompletionException(ex);
                }).join();
    }

}