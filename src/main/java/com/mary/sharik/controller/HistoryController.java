package com.mary.sharik.controller;

import com.mary.sharik.kafka.KafkaHistoryService;
import com.mary.sharik.model.entity.OrdersHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class HistoryController {

    private final KafkaHistoryService kafkaHistoryService;

    @GetMapping("/history")
    public OrdersHistory getHistory() {
        return kafkaHistoryService.findHistory();
    }
}
