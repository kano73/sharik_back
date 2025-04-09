package com.mary.sharik.kafka;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.mary.sharik.config.security.AuthenticatedMyUserService;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.entity.OrdersHistory;
import com.mary.sharik.model.enumClass.KafkaTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KafkaHistoryServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private KafkaRequesterService kafkaRequesterService;

    @Mock
    private AuthenticatedMyUserService authenticatedMyUserService;

    @InjectMocks
    private KafkaHistoryService kafkaHistoryService;

    @Mock
    private ConsumerRecord<String, String> consumerRecord;

    @Mock
    private CollectionType collectionType;

    private final String TEST_USER_ID = "test-user-id";
    private final String TEST_JSON_RESPONSE = "{\"userId\":\"test-user-id\",\"items\":[]}";
    private final String TEST_LIST_JSON_RESPONSE = "[{\"userId\":\"test-user-id\",\"items\":[]}]";
    private final Integer TEST_PAGE = 1;



    @Test
    void getWholeHistory_Success() throws JsonProcessingException {
        // Arrange
        String pageJson = "0";
        OrdersHistory ordersHistory = new OrdersHistory();
        ordersHistory.setUserId(TEST_USER_ID);
        List<OrdersHistory> historyList = List.of(ordersHistory);

        when(objectMapper.writeValueAsString(TEST_PAGE)).thenReturn(pageJson);
        when(kafkaRequesterService.makeRequest(KafkaTopic.HISTORY_ALL_TOPIC.name(), pageJson))
                .thenReturn(CompletableFuture.completedFuture(consumerRecord));
        when(consumerRecord.value()).thenReturn(TEST_LIST_JSON_RESPONSE);
        when(objectMapper.getTypeFactory()).thenReturn(mock(com.fasterxml.jackson.databind.type.TypeFactory.class));
        when(objectMapper.getTypeFactory().constructCollectionType(List.class, OrdersHistory.class)).thenReturn(collectionType);
        when(objectMapper.readValue(TEST_LIST_JSON_RESPONSE, collectionType)).thenReturn(historyList);

        // Act
        List<OrdersHistory> result = kafkaHistoryService.getWholeHistory(TEST_PAGE);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_USER_ID, result.getFirst().getUserId());
        verify(objectMapper).writeValueAsString(TEST_PAGE);
        verify(kafkaRequesterService).makeRequest(KafkaTopic.HISTORY_ALL_TOPIC.name(), pageJson);
        verify(objectMapper).readValue(TEST_LIST_JSON_RESPONSE, collectionType);
    }

    @Test
    void getWholeHistory_JsonProcessingException() throws JsonProcessingException {
        // Arrange
        String pageJson = "1";
        when(objectMapper.writeValueAsString(TEST_PAGE)).thenReturn(pageJson);
        when(kafkaRequesterService.makeRequest(KafkaTopic.HISTORY_ALL_TOPIC.name(), pageJson))
                .thenReturn(CompletableFuture.completedFuture(consumerRecord));
        when(consumerRecord.value()).thenReturn(TEST_LIST_JSON_RESPONSE);
        when(objectMapper.getTypeFactory()).thenReturn(mock(com.fasterxml.jackson.databind.type.TypeFactory.class));
        when(objectMapper.getTypeFactory().constructCollectionType(List.class, OrdersHistory.class)).thenReturn(collectionType);
        when(objectMapper.readValue(anyString(), eq(collectionType))).thenThrow(new JsonProcessingException("Test exception") {});

        // Act & Assert
        assertThrows(CompletionException.class, () -> kafkaHistoryService.getWholeHistory(TEST_PAGE));
        verify(objectMapper).writeValueAsString(TEST_PAGE);
        verify(kafkaRequesterService).makeRequest(KafkaTopic.HISTORY_ALL_TOPIC.name(), pageJson);
    }

    @Test
    void getWholeHistory_CompletableFutureException() throws JsonProcessingException {
        // Arrange
        String pageJson = "1";
        CompletableFuture<ConsumerRecord<String, String>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Test exception"));

        when(objectMapper.writeValueAsString(TEST_PAGE)).thenReturn(pageJson);
        when(kafkaRequesterService.makeRequest(KafkaTopic.HISTORY_ALL_TOPIC.name(), pageJson))
                .thenReturn(failedFuture);

        // Act & Assert
        assertThrows(CompletionException.class, () -> kafkaHistoryService.getWholeHistory(TEST_PAGE));
        verify(objectMapper).writeValueAsString(TEST_PAGE);
        verify(kafkaRequesterService).makeRequest(KafkaTopic.HISTORY_ALL_TOPIC.name(), pageJson);
    }

    @Test
    void findHistory_Success() throws JsonProcessingException {
        // Arrange
        MyUser user = new MyUser();
        user.setId(TEST_USER_ID);
        OrdersHistory ordersHistory = new OrdersHistory();
        ordersHistory.setUserId(TEST_USER_ID);

        when(authenticatedMyUserService.getCurrentUserAuthenticated()).thenReturn(user);
        when(objectMapper.writeValueAsString(TEST_USER_ID)).thenReturn(TEST_USER_ID);
        when(kafkaRequesterService.makeRequest(KafkaTopic.HISTORY_VIEW_TOPIC.name(), TEST_USER_ID))
                .thenReturn(CompletableFuture.completedFuture(consumerRecord));
        when(consumerRecord.value()).thenReturn(TEST_JSON_RESPONSE);
        when(objectMapper.readValue(TEST_JSON_RESPONSE, OrdersHistory.class)).thenReturn(ordersHistory);

        // Act
        OrdersHistory result = kafkaHistoryService.findHistory();

        // Assert
        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.getUserId());
        verify(authenticatedMyUserService).getCurrentUserAuthenticated();
        verify(objectMapper).writeValueAsString(TEST_USER_ID);
        verify(kafkaRequesterService).makeRequest(KafkaTopic.HISTORY_VIEW_TOPIC.name(), TEST_USER_ID);
        verify(objectMapper).readValue(TEST_JSON_RESPONSE, OrdersHistory.class);
    }

    @Test
    void getOrdersHistoryByUserId_Success() throws JsonProcessingException {
        // Arrange
        OrdersHistory ordersHistory = new OrdersHistory();
        ordersHistory.setUserId(TEST_USER_ID);

        when(objectMapper.writeValueAsString(TEST_USER_ID)).thenReturn(TEST_USER_ID);
        when(kafkaRequesterService.makeRequest(KafkaTopic.HISTORY_VIEW_TOPIC.name(), TEST_USER_ID))
                .thenReturn(CompletableFuture.completedFuture(consumerRecord));
        when(consumerRecord.value()).thenReturn(TEST_JSON_RESPONSE);
        when(objectMapper.readValue(TEST_JSON_RESPONSE, OrdersHistory.class)).thenReturn(ordersHistory);

        // Act
        OrdersHistory result = kafkaHistoryService.getOrdersHistoryByUserId(TEST_USER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.getUserId());
        verify(objectMapper).writeValueAsString(TEST_USER_ID);
        verify(kafkaRequesterService).makeRequest(KafkaTopic.HISTORY_VIEW_TOPIC.name(), TEST_USER_ID);
        verify(objectMapper).readValue(TEST_JSON_RESPONSE, OrdersHistory.class);
    }

    @Test
    void getOrdersHistoryByUserId_JsonProcessingException() throws JsonProcessingException {
        // Arrange
        when(objectMapper.writeValueAsString(TEST_USER_ID)).thenReturn(TEST_USER_ID);
        when(kafkaRequesterService.makeRequest(KafkaTopic.HISTORY_VIEW_TOPIC.name(), TEST_USER_ID))
                .thenReturn(CompletableFuture.completedFuture(consumerRecord));
        when(consumerRecord.value()).thenReturn(TEST_JSON_RESPONSE);
        when(objectMapper.readValue(anyString(), eq(OrdersHistory.class))).thenThrow(new JsonProcessingException("Test exception") {});

        // Act & Assert
        assertThrows(CompletionException.class, () -> kafkaHistoryService.getOrdersHistoryByUserId(TEST_USER_ID));
        verify(objectMapper).writeValueAsString(TEST_USER_ID);
        verify(kafkaRequesterService).makeRequest(KafkaTopic.HISTORY_VIEW_TOPIC.name(), TEST_USER_ID);
    }

    @Test
    void getOrdersHistoryByUserId_CompletableFutureException() throws JsonProcessingException {
        // Arrange
        CompletableFuture<ConsumerRecord<String, String>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Test exception"));

        when(objectMapper.writeValueAsString(TEST_USER_ID)).thenReturn(TEST_USER_ID);
        when(kafkaRequesterService.makeRequest(KafkaTopic.HISTORY_VIEW_TOPIC.name(), TEST_USER_ID))
                .thenReturn(failedFuture);

        // Act & Assert
        assertThrows(CompletionException.class, () -> kafkaHistoryService.getOrdersHistoryByUserId(TEST_USER_ID));
        verify(objectMapper).writeValueAsString(TEST_USER_ID);
        verify(kafkaRequesterService).makeRequest(KafkaTopic.HISTORY_VIEW_TOPIC.name(), TEST_USER_ID);
    }
}