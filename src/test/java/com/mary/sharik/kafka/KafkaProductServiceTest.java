package com.mary.sharik.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.mary.sharik.model.dto.request.AddProductDTO;
import com.mary.sharik.model.dto.request.ProductSearchFilterDTO;
import com.mary.sharik.model.dto.request.SetProductStatusDTO;
import com.mary.sharik.model.entity.Product;
import com.mary.sharik.model.enumClass.KafkaTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaProductServiceTest {

    @InjectMocks
    private KafkaProductService kafkaProductService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private KafkaRequesterService kafkaRequesterService;

    @Mock
    private ConsumerRecord<String, String> consumerRecord;

    @Mock
    private CollectionType collectionType;

    @Test
    void requestProductsByFilter_success() throws Exception {
        ProductSearchFilterDTO filter = new ProductSearchFilterDTO();
        String filterJson = "{}";
        List<Product> products = List.of(new Product());

        when(objectMapper.writeValueAsString(filter)).thenReturn(filterJson);
        when(kafkaRequesterService.makeRequest(KafkaTopic.PRODUCT_BY_FILTER_TOPIC.name(), filterJson))
                .thenReturn(CompletableFuture.completedFuture(consumerRecord));
        String TEST_LIST_JSON_RESPONSE = "[{\"userId\":\"test-user-id\",\"items\":[]}]";
        when(consumerRecord.value()).thenReturn(TEST_LIST_JSON_RESPONSE);
        when(objectMapper.getTypeFactory()).thenReturn(mock(com.fasterxml.jackson.databind.type.TypeFactory.class));
        when(objectMapper.getTypeFactory().constructCollectionType(List.class, Product.class)).thenReturn(collectionType);
        when(objectMapper.readValue(TEST_LIST_JSON_RESPONSE, collectionType)).thenReturn(products);

        List<Product> result = kafkaProductService.requestProductsByFilter(filter);

        assertEquals(1, result.size());
        verify(objectMapper).writeValueAsString(filter);
        verify(kafkaRequesterService).makeRequest(KafkaTopic.PRODUCT_BY_FILTER_TOPIC.name(), filterJson);
    }

    @Test
    void createProduct_success() throws Exception {
        AddProductDTO dto = new AddProductDTO();
        String dtoJson = "{}";

        when(objectMapper.writeValueAsString(dto)).thenReturn(dtoJson);
        when(kafkaRequesterService.makeRequest(KafkaTopic.PRODUCT_CREATE_TOPIC.name(), dtoJson))
                .thenReturn(CompletableFuture.completedFuture(consumerRecord));
        when(consumerRecord.value()).thenReturn("true");
        when(objectMapper.readValue("true", Boolean.class)).thenReturn(true);

        boolean result = kafkaProductService.createProduct(dto);

        assertTrue(result);
        verify(objectMapper).writeValueAsString(dto);
    }

    @Test
    void requestProductsById_success() throws Exception {
        String id = "123";
        Product product = new Product();

        when(objectMapper.writeValueAsString(id)).thenReturn(id);
        when(kafkaRequesterService.makeRequest(KafkaTopic.PRODUCT_BY_ID_TOPIC.name(), id))
                .thenReturn(CompletableFuture.completedFuture(consumerRecord));
        String TEST_JSON_RESPONSE = "{\"userId\":\"test-user-id\",\"items\":[]}";
        when(consumerRecord.value()).thenReturn(TEST_JSON_RESPONSE);
        when(objectMapper.readValue(TEST_JSON_RESPONSE, Product.class)).thenReturn(product);

        Product result = kafkaProductService.requestProductsById(id);

        assertNotNull(result);
        verify(objectMapper).writeValueAsString(id);
    }

    @Test
    void setProductStatus_success() throws Exception {
        SetProductStatusDTO dto = new SetProductStatusDTO();
        String dtoJson = "{}";

        when(objectMapper.writeValueAsString(dto)).thenReturn(dtoJson);
        when(kafkaRequesterService.makeRequest(KafkaTopic.PRODUCT_SET_STATUS_TOPIC.name(), dtoJson))
                .thenReturn(CompletableFuture.completedFuture(consumerRecord));
        when(consumerRecord.value()).thenReturn("true");
        when(objectMapper.readValue("true", Boolean.class)).thenReturn(true);

        boolean result = kafkaProductService.setProductStatus(dto);

        assertTrue(result);
        verify(objectMapper).writeValueAsString(dto);
    }
}