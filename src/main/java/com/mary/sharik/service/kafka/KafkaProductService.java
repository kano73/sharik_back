package com.mary.sharik.service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.mary.sharik.exceptions.MicroserviceExternalException;
import com.mary.sharik.model.dto.request.AddProductDTO;
import com.mary.sharik.model.dto.request.ProductSearchFilterDTO;
import com.mary.sharik.model.entity.Product;
import com.mary.sharik.model.enums.KafkaTopicEnum;
import lombok.RequiredArgsConstructor;
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
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class KafkaProductService {

    @Autowired
    private ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;

    public List<Product> requestProductsByFilter(ProductSearchFilterDTO filter) throws Exception {

        System.out.println("filter row: "+filter);

        // Преобразуем фильтр в JSON
        String valueJson = new ObjectMapper().writeValueAsString(filter);

        System.out.println("valueJson: "+valueJson);

        ConsumerRecord<String, String> response = makeRequest(KafkaTopicEnum.PRODUCT_BY_FILTER_TOPIC.name(),valueJson );

        // Преобразуем JSON в список объектов Product
        ObjectMapper mapper = new ObjectMapper();
        CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, Product.class);

        System.out.println("response json: " + response.toString());

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

        System.out.println("response json: " + response.toString());

        // Преобразуем JSON в список объектов Product
        return new ObjectMapper().readValue(response.value(), Product.class);
    }

    private ConsumerRecord<String, String> makeRequest(String topic, String value)
            throws
            ExecutionException,
            InterruptedException, MicroserviceExternalException {
        // Создаем сообщение с заголовком reply-topic
        ProducerRecord<String, String> record =
                new ProducerRecord<>(topic, value);

        // Добавляем заголовок с correlation ID
        String correlationId = UUID.randomUUID().toString();
        record.headers().add(new RecordHeader(KafkaHeaders.CORRELATION_ID,
                correlationId.getBytes()));
        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC,
                KafkaTopicEnum.PRODUCT_REPLY_TOPIC.name().getBytes()));

        System.out.println("record: "+record+ "\n record.value(): "+record.value());

        // Отправляем запрос и ожидаем ответ (с таймаутом 5 секунд)
        RequestReplyFuture<String, String, String> future =
                replyingKafkaTemplate.sendAndReceive(record, Duration.ofSeconds(5));

        // Получаем ответ
        ConsumerRecord<String, String> response = future.get();

        System.out.println("response: "+response);

        for (Header header : response.headers()) {
            System.out.println(header.toString());
            if(header.key().equals(KafkaHeaders.EXCEPTION_MESSAGE)) {
                throw new MicroserviceExternalException(new String(header.value(), StandardCharsets.UTF_8));
            }
        }

        return response;
    }
}