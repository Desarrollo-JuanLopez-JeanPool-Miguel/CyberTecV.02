package com.cybertec.dto;

import lombok.Data;
import java.util.Map;

@Data
public class OrderRequest {
    private Map<String, String> customerData;
    private ShippingAddress shippingAddress;
    private String paymentMethod;
    
    @Data
    public static class ShippingAddress {
        private String address;
        private String city;
        private String department;
        private String postalCode;
        private String notes;
    }
}