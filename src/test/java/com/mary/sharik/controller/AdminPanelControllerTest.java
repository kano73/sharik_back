package com.mary.sharik.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mary.sharik.kafka.KafkaCartService;
import com.mary.sharik.kafka.KafkaHistoryService;
import com.mary.sharik.kafka.KafkaProductService;
import com.mary.sharik.model.dto.request.AddProductDTO;
import com.mary.sharik.model.dto.request.MyUserSearchFilterDTO;
import com.mary.sharik.model.dto.request.SetProductStatusDTO;
import com.mary.sharik.model.dto.responce.MyUserPublicInfoDTO;
import com.mary.sharik.model.dto.storage.ProductAndQuantity;
import com.mary.sharik.model.entity.OrdersHistory;
import com.mary.sharik.model.entity.Product;
import com.mary.sharik.model.enumClass.Role;
import com.mary.sharik.service.MyUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminPanelController.class)
@Import({SecurityTestConfig.class, AdminPanelController.class})
class AdminPanelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MyUserService myUserService;

    @Autowired
    private KafkaCartService kafkaCartService;

    @Autowired
    private KafkaHistoryService kafkaHistoryService;

    @Autowired
    private KafkaProductService kafkaProductService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsersInfo_shouldReturnUserInfo() throws Exception {
        // Arrange
        MyUserPublicInfoDTO userDto = new MyUserPublicInfoDTO("1", "adminovich", "Admin", "address", "admin@example.com", Role.ADMIN);

        when(myUserService.getUsersInfoById("1")).thenReturn(userDto);

        // Act & Assert
        mockMvc.perform(get("/admin/profile_of").param("id", "1").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.id").value("1")).andExpect(jsonPath("$.email").value("admin@example.com")).andExpect(jsonPath("$.firstName").value("adminovich")).andExpect(jsonPath("$.lastName").value("Admin"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getActiveCart_shouldReturnProductList() throws Exception {
        // Arrange
        List<ProductAndQuantity> cartItems = new ArrayList<>();
        ProductAndQuantity item = new ProductAndQuantity();
        Product product = new Product();
        product.setId("id1");
        item.setProduct(product);
        item.setQuantity(2);
        cartItems.add(item);

        when(kafkaCartService.getCartOfUserById("1")).thenReturn(cartItems);

        // Act & Assert
        mockMvc.perform(get("/admin/cart_of").param("id", "1").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$[0].product.id").value("id1")).andExpect(jsonPath("$[0].quantity").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getHistory_shouldReturnOrdersHistory() throws Exception {
        // Arrange
        OrdersHistory history = new OrdersHistory();
        history.setUserId("1");

        when(kafkaHistoryService.getOrdersHistoryByUserId("1")).thenReturn(history);

        // Act & Assert
        mockMvc.perform(get("/admin/history_of").param("id", "1").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.userId").value("1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllHistory_shouldReturnHistoryList() throws Exception {
        // Arrange
        List<OrdersHistory> historyList = new ArrayList<>();
        OrdersHistory history1 = new OrdersHistory();
        history1.setUserId("user1");

        OrdersHistory history2 = new OrdersHistory();
        history2.setUserId("user2");

        historyList.add(history1);
        historyList.add(history2);

        when(kafkaHistoryService.getWholeHistory(1)).thenReturn(historyList);

        // Act & Assert
        mockMvc.perform(get("/admin/all_histories").param("page", "1").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$[0].userId").value("user1")).andExpect(jsonPath("$[1].userId").value("user2"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_shouldReturnUsersList() throws Exception {
        // Arrange
        List<MyUserPublicInfoDTO> usersList = new ArrayList<>();
        MyUserPublicInfoDTO userDto1 = new MyUserPublicInfoDTO("1", "adminovich", "Admin", "address", "admin1@example.com", Role.ADMIN);

        MyUserPublicInfoDTO userDto2 = new MyUserPublicInfoDTO("2", "adminovich", "Admin", "address", "admin2@example.com", Role.ADMIN);

        usersList.add(userDto1);
        usersList.add(userDto2);

        MyUserSearchFilterDTO filter = new MyUserSearchFilterDTO();
        filter.setEmail("user");

        when(myUserService.getUsersByFilters(any(MyUserSearchFilterDTO.class))).thenReturn(usersList);

        // Act & Assert
        mockMvc.perform(post("/admin/all_users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(filter))).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value("1")).andExpect(jsonPath("$[0].email").value("admin1@example.com")).andExpect(jsonPath("$[1].id").value("2")).andExpect(jsonPath("$[1].email").value("admin2@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void setProductStatus_shouldReturnTrue() throws Exception {
        // Arrange
        SetProductStatusDTO dto = new SetProductStatusDTO();
        dto.setProductId("product1");
        dto.setStatus(true);

        when(kafkaProductService.setProductStatus(any(SetProductStatusDTO.class))).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/admin/set_product_status").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto))).andDo(print()).andExpect(status().isOk()).andExpect(content().string("true"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addProduct_shouldReturnTrue() throws Exception {
        // Arrange
        AddProductDTO dto = new AddProductDTO();
        dto.setName("New Product");
        dto.setPrice(99.99);
        dto.setDescription("Description");

        when(kafkaProductService.createProduct(any(AddProductDTO.class))).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/admin/create_product").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto))).andDo(print()).andExpect(status().isOk()).andExpect(content().string("true"));
    }

    @Test
    @WithMockUser(roles = "USER")
        // Пользователь без роли ADMIN
    void accessDeniedForNonAdminUsers() throws Exception {
        mockMvc.perform(get("/admin/profile_of").param("id", "1")).andDo(print()).andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedAccessDenied() throws Exception {
        mockMvc.perform(get("/admin/profile_of").param("id", "1")).andDo(print()).andExpect(status().isForbidden());
    }
}