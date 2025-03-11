package com.mary.sharik.model.enums;

public enum OrderStatusEnum {
    CREATED,
    PROCESSING,       // In progress (assembly, packaging)
    SHIPPING,          // Shipped to the customer
    DELIVERED,        // Successfully delivered

    ANNULLED, // Return requested
    REFUNDED,
    CANCELLED // empty cart
}
