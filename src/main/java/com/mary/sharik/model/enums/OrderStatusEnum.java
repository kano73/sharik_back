package com.mary.sharik.model.enums;

public enum OrderStatusEnum {
    CONFIRMED,        // Confirmed payment
    PROCESSING,       // In progress (assembly, packaging)
    SHIPPED,          // Shipped to the customer
    DELIVERED,        // Successfully delivered

    CANCELLED,        // Cancelled by the customer
    RETURN_REQUESTED, // Return requested
    RETURNED,         // Returned and accepted
    FAILED            // Payment or order processing failed
}
