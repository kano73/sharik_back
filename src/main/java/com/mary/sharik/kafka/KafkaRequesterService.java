package com.mary.sharik.kafka;

import com.mary.sharik.exception.MicroserviceExternalException;
import com.mary.sharik.model.enumClass.KafkaTopic;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

@Service
public class KafkaRequesterService {

    @Autowired
    private ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;

    public CompletableFuture<ConsumerRecord<String, String>> makeRequest(String topic, String value)
            throws MicroserviceExternalException {
        // Создаем сообщение с заголовком reply-topic
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, value);

        // Добавляем заголовок с correlation ID
        String correlationId = UUID.randomUUID().toString();
        record.headers().add(new RecordHeader(KafkaHeaders.CORRELATION_ID, correlationId.getBytes()));
        record.headers().add(
                new RecordHeader(KafkaHeaders.REPLY_TOPIC, KafkaTopic.PRODUCT_REPLY_TOPIC.name().getBytes()));


        // Отправляем запрос и ожидаем ответ (с таймаутом 5 секунд)
        RequestReplyFuture<String, String, String> future =
                replyingKafkaTemplate.sendAndReceive(record, Duration.ofSeconds(5));

        // Получаем ответ
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Получаем ответ
                ConsumerRecord<String, String> response = future.get();

                for (Header header : response.headers()) {
                    if (header.key().equals(KafkaHeaders.EXCEPTION_MESSAGE)) {
                        throw new MicroserviceExternalException(new String(header.value(), StandardCharsets.UTF_8));
                    }
                }
                return response;
            } catch (InterruptedException | ExecutionException e) {
                throw new CompletionException(e);
            }
        });
    }
}
