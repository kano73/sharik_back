package com.mary.sharik.model.enumClass;

public enum OrderStatusEnum {
    CANCELLED, // empty cart
    CREATED,
    PROCESSING,       // In progress (assembly, packaging)
    SHIPPING,          // Shipped to the customer
    DELIVERED,        // Successfully delivered

    ANNULLED, // Return requested
    REFUNDED
}
