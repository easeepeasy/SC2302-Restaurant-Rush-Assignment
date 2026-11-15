package restaurant.model;

// Represents the different stages an order goes through, from placement to payment.
public enum OrderStatus {
    PLACED,
    PREPARING,
    READY,
    SERVED,
    PAID,
    CANCELLED
}
