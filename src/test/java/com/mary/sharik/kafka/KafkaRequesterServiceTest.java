package com.mary.sharik.kafka;

import com.mary.sharik.exception.MicroserviceExternalException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class KafkaRequesterServiceTest {

    private final String TEST_TOPIC = "test-topic";
    private final String TEST_VALUE = "test-payload";
    @InjectMocks
    private KafkaRequesterService kafkaRequesterService;
    @Mock
    private ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;
    @Mock
    private RequestReplyFuture<String, String, String> requestReplyFuture;
    @Mock
    private ConsumerRecord<String, String> consumerRecord;

    @Test
    void makeRequest_successWithoutErrorHeader() throws Exception {
        // Arrange
        when(replyingKafkaTemplate.sendAndReceive(any(ProducerRecord.class), any(Duration.class))).thenReturn(requestReplyFuture);
        when(requestReplyFuture.get()).thenReturn(consumerRecord);
        when(consumerRecord.headers()).thenReturn(new RecordHeaders());

        // Act
        CompletableFuture<ConsumerRecord<String, String>> future = kafkaRequesterService.makeRequest(TEST_TOPIC, TEST_VALUE);
        ConsumerRecord<String, String> result = future.get();

        // Assert
        assertEquals(consumerRecord, result);
        verify(replyingKafkaTemplate).sendAndReceive(any(ProducerRecord.class), eq(Duration.ofSeconds(5)));
    }

    @Test
    void makeRequest_responseWithExceptionHeader_shouldThrow() {
        // Arrange
        RecordHeaders headers = new RecordHeaders();
        headers.add(new RecordHeader(KafkaHeaders.EXCEPTION_MESSAGE, "Something went wrong".getBytes(StandardCharsets.UTF_8)));

        when(replyingKafkaTemplate.sendAndReceive(any(ProducerRecord.class), any(Duration.class))).thenReturn(requestReplyFuture);

        try {
            when(requestReplyFuture.get()).thenReturn(consumerRecord);
        } catch (Exception e) {
            fail("Unexpected exception");
        }

        when(consumerRecord.headers()).thenReturn(headers);

        // Act & Assert
        CompletableFuture<ConsumerRecord<String, String>> future = kafkaRequesterService.makeRequest(TEST_TOPIC, TEST_VALUE);

        CompletionException exception = assertThrows(CompletionException.class, future::join);
        assertInstanceOf(MicroserviceExternalException.class, exception.getCause());
        assertEquals("Something went wrong", exception.getCause().getMessage());
    }

    @Test
    void makeRequest_interruptedException_shouldThrowCompletionException() throws Exception {
        // Arrange
        when(replyingKafkaTemplate.sendAndReceive(any(ProducerRecord.class), any(Duration.class))).thenReturn(requestReplyFuture);
        when(requestReplyFuture.get()).thenThrow(new InterruptedException("Interrupted"));

        // Act
        CompletableFuture<ConsumerRecord<String, String>> future = kafkaRequesterService.makeRequest(TEST_TOPIC, TEST_VALUE);

        // Assert
        CompletionException exception = assertThrows(CompletionException.class, future::join);
        assertInstanceOf(InterruptedException.class, exception.getCause());
    }

    @Test
    void makeRequest_executionException_shouldThrowCompletionException() throws Exception {
        // Arrange
        when(replyingKafkaTemplate.sendAndReceive(any(ProducerRecord.class), any(Duration.class))).thenReturn(requestReplyFuture);
        when(requestReplyFuture.get()).thenThrow(new ExecutionException(new RuntimeException("Kafka failure")));

        // Act
        CompletableFuture<ConsumerRecord<String, String>> future = kafkaRequesterService.makeRequest(TEST_TOPIC, TEST_VALUE);

        // Assert
        CompletionException exception = assertThrows(CompletionException.class, future::join);
        assertInstanceOf(ExecutionException.class, exception.getCause());
    }
}
