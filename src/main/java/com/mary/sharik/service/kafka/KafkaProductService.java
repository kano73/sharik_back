package com.mary.sharik.service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.mary.sharik.model.dto.request.AddProductDTO;
import com.mary.sharik.model.dto.request.ProductSearchFilterDTO;
import com.mary.sharik.model.entity.Product;
import com.mary.sharik.model.enums.KafkaTopicEnum;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class KafkaProductService {

    private ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;

    public List<Product> requestProductsByFilter(ProductSearchFilterDTO filter) throws Exception {
        // Преобразуем фильтр в JSON
        String valueJson = new ObjectMapper().writeValueAsString(filter);

        ConsumerRecord<String, String> response = makeRequest(KafkaTopicEnum.PRODUCT_BY_FILTER_TOPIC.name(),valueJson );

        // Преобразуем JSON в список объектов Product
        ObjectMapper mapper = new ObjectMapper();
        CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, Product.class);

        return mapper.readValue(response.value(), listType);
    }

    public Boolean createProduct(AddProductDTO dto) throws Exception {
        // Преобразуем фильтр в JSON
        String valueJson = new ObjectMapper().writeValueAsString(dto);

        ConsumerRecord<String, String> response = makeRequest(KafkaTopicEnum.PRODUCT_CREATE_TOPIC.name(),valueJson );

        // Преобразуем JSON в список объектов Product
        return new ObjectMapper().readValue(response.value(), Boolean.class);
    }

    public Product requestProductsById(String id) throws Exception {
        // Преобразуем фильтр в JSON
        String valueJson = new ObjectMapper().writeValueAsString(id);

        ConsumerRecord<String, String> response = makeRequest(KafkaTopicEnum.PRODUCT_BY_ID_TOPIC.name(), valueJson);

        // Преобразуем JSON в список объектов Product
        return new ObjectMapper().readValue(response.value(), Product.class);
    }

    private ConsumerRecord<String, String> makeRequest(String topic, String value)
            throws
                ExecutionException,
                InterruptedException{
        // Создаем сообщение с заголовком reply-topic
        ProducerRecord<String, String> record =
                new ProducerRecord<>(topic, value);

        // Добавляем заголовок с correlation ID
        String correlationId = UUID.randomUUID().toString();
        record.headers().add(new RecordHeader(KafkaHeaders.CORRELATION_ID,
                correlationId.getBytes()));
        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC,
                KafkaTopicEnum.PRODUCT_REPLY_TOPIC.name().getBytes()));

        // Отправляем запрос и ожидаем ответ (с таймаутом 5 секунд)
        RequestReplyFuture<String, String, String> future =
                replyingKafkaTemplate.sendAndReceive(record, Duration.ofSeconds(5));

        // Получаем ответ
        return future.get();
    }
}