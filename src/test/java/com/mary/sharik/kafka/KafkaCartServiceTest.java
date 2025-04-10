package com.mary.sharik.kafka;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.mary.sharik.config.security.AuthenticatedMyUserService;
import com.mary.sharik.model.dto.request.ActionWithCartDTO;
import com.mary.sharik.model.dto.request.OrderDetailsDTO;
import com.mary.sharik.model.dto.storage.ProductAndQuantity;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.enumClass.KafkaTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaCartServiceTest {

    @InjectMocks
    private KafkaCartService kafkaCartService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private KafkaRequesterService kafkaRequesterService;

    @Mock
    private AuthenticatedMyUserService authenticatedMyUserService;

    @Mock
    private ConsumerRecord<String, String> consumerRecord;

    @Mock
    private CollectionType collectionType;

    private final String TEST_USER_ID = UUID.randomUUID().toString();
    private final String TEST_JSON_RESPONSE = "[{\"productId\":\"1\",\"quantity\":2}]";

    @Test
    void emptyCart_success() throws Exception {
        MyUser user = new MyUser();
        user.setId(TEST_USER_ID);

        when(authenticatedMyUserService.getCurrentUserAuthenticated()).thenReturn(user);
        when(objectMapper.writeValueAsString(TEST_USER_ID)).thenReturn(TEST_USER_ID);
        when(kafkaRequesterService.makeRequest(eq(KafkaTopic.CART_EMPTY_TOPIC.name()), anyString()))
                .thenReturn(CompletableFuture.completedFuture(consumerRecord));
        when(consumerRecord.value()).thenReturn("true");
        when(objectMapper.readValue("true", Boolean.class)).thenReturn(true);

        boolean result = kafkaCartService.emptyCart();
        assertTrue(result);
    }

    @Test
    void addToCart_success() throws Exception {
        ActionWithCartDTO dto = new ActionWithCartDTO();
        MyUser user = new MyUser();
        user.setId(TEST_USER_ID);

        when(authenticatedMyUserService.getCurrentUserAuthenticated()).thenReturn(user);
        when(objectMapper.writeValueAsString(any())).thenReturn(TEST_USER_ID);
        when(kafkaRequesterService.makeRequest(eq(KafkaTopic.CART_ADD_TOPIC.name()), anyString()))
                .thenReturn(CompletableFuture.completedFuture(consumerRecord));
        when(consumerRecord.value()).thenReturn("true");
        when(objectMapper.readValue("true", Boolean.class)).thenReturn(true);

        boolean result = kafkaCartService.addToCart(dto);
        assertTrue(result);
    }

    @Test
    void changeAmount_success() throws Exception {
        ActionWithCartDTO dto = new ActionWithCartDTO();
        MyUser user = new MyUser();
        user.setId(TEST_USER_ID);

        when(authenticatedMyUserService.getCurrentUserAuthenticated()).thenReturn(user);
        when(objectMapper.writeValueAsString(any())).thenReturn(TEST_USER_ID);
        when(kafkaRequesterService.makeRequest(eq(KafkaTopic.CART_CHANGE_AMOUNT_TOPIC.name()), anyString()))
                .thenReturn(CompletableFuture.completedFuture(consumerRecord));
        when(consumerRecord.value()).thenReturn("true");
        when(objectMapper.readValue("true", Boolean.class)).thenReturn(true);

        boolean result = kafkaCartService.changeAmount(dto);
        assertTrue(result);
    }

    @Test
    void makeOrder_success() throws Exception {
        OrderDetailsDTO dto = new OrderDetailsDTO();
        MyUser user = new MyUser();
        user.setId(TEST_USER_ID);

        when(authenticatedMyUserService.getCurrentUserAuthenticated()).thenReturn(user);
        when(objectMapper.writeValueAsString(any())).thenReturn(TEST_USER_ID);
        when(kafkaRequesterService.makeRequest(eq(KafkaTopic.CART_ORDER_TOPIC.name()), anyString()))
                .thenReturn(CompletableFuture.completedFuture(consumerRecord));
        when(consumerRecord.value()).thenReturn("true");
        when(objectMapper.readValue("true", Boolean.class)).thenReturn(true);

        boolean result = kafkaCartService.makeOrder(dto);
        assertTrue(result);
    }

    @Test
    void findCart_success() throws Exception {
        MyUser user = new MyUser();
        user.setId(TEST_USER_ID);

        ProductAndQuantity pq = new ProductAndQuantity();
        List<ProductAndQuantity> cart = List.of(pq);

        when(authenticatedMyUserService.getCurrentUserAuthenticated()).thenReturn(user);
        when(objectMapper.writeValueAsString(TEST_USER_ID)).thenReturn(TEST_USER_ID);
        when(kafkaRequesterService.makeRequest(eq(KafkaTopic.CART_VIEW_TOPIC.name()), eq(TEST_USER_ID)))
                .thenReturn(CompletableFuture.completedFuture(consumerRecord));
        when(consumerRecord.value()).thenReturn(TEST_JSON_RESPONSE);
        when(objectMapper.getTypeFactory()).thenReturn(mock(com.fasterxml.jackson.databind.type.TypeFactory.class));
        when(objectMapper.getTypeFactory().constructCollectionType(List.class, ProductAndQuantity.class)).thenReturn(collectionType);
        when(objectMapper.readValue(TEST_JSON_RESPONSE, collectionType)).thenReturn(cart);

        List<ProductAndQuantity> result = kafkaCartService.findCart();
        assertEquals(1, result.size());
    }

    @Test
    void getCartOfUserById_jsonProcessingException() throws Exception {
        when(objectMapper.writeValueAsString(TEST_USER_ID)).thenReturn(TEST_USER_ID);
        when(kafkaRequesterService.makeRequest(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(consumerRecord));
        when(consumerRecord.value()).thenReturn(TEST_JSON_RESPONSE);
        when(objectMapper.getTypeFactory()).thenReturn(mock(com.fasterxml.jackson.databind.type.TypeFactory.class));
        when(objectMapper.getTypeFactory().constructCollectionType(List.class, ProductAndQuantity.class)).thenReturn(collectionType);
        when(objectMapper.readValue(anyString(), eq(collectionType))).thenThrow(new JsonProcessingException("error") {});

        assertThrows(CompletionException.class, () -> kafkaCartService.getCartOfUserById(TEST_USER_ID));
    }
}
