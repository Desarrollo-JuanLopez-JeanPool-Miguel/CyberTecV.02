package com.cybertec.dto;

import com.cybertec.model.ShippingAddress;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para crear una orden desde el carrito
 */
public class CreateOrderRequest {

    @NotNull(message = "La dirección de envío es obligatoria")
    private ShippingAddress shippingAddress;

    @NotBlank(message = "El método de pago es obligatorio")
    private String paymentMethod;

    private String notes;

    // ========== CONSTRUCTORES ==========

    public CreateOrderRequest() {}

    public CreateOrderRequest(ShippingAddress shippingAddress, String paymentMethod) {
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
    }

    // ========== GETTERS Y SETTERS ==========

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(ShippingAddress shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}