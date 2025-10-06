package com.cybertec.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Modelo de Item del Carrito
 * Ubicación: src/main/java/com/cybertec/model/CartItem.java
 * 
 * @author CyberTec Team
 * @version 1.0.0
 */
public class CartItem {
    
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private String productBrand;
    private String productImageUrl;
    private ProductCategory productCategory;
    
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
    
    private LocalDateTime addedAt;

    // Constructores
    public CartItem() {
        this.addedAt = LocalDateTime.now();
    }

    public CartItem(Product product, Integer quantity) {
        this();
        this.productId = product.getId();
        this.productName = product.getName();
        this.productSku = product.getSku();
        this.productBrand = product.getBrand();
        this.productImageUrl = product.getImageUrl();
        this.productCategory = product.getCategory();
        this.unitPrice = product.getPrice();
        this.quantity = quantity;
        calculateTotalPrice();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }

    public String getProductBrand() { return productBrand; }
    public void setProductBrand(String productBrand) { this.productBrand = productBrand; }

    public String getProductImageUrl() { return productImageUrl; }
    public void setProductImageUrl(String productImageUrl) { this.productImageUrl = productImageUrl; }

    public ProductCategory getProductCategory() { return productCategory; }
    public void setProductCategory(ProductCategory productCategory) { this.productCategory = productCategory; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotalPrice();
    }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        calculateTotalPrice();
    }

    public BigDecimal getTotalPrice() {
        calculateTotalPrice();
        return totalPrice;
    }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

    // Métodos de utilidad
    private void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        } else {
            this.totalPrice = BigDecimal.ZERO;
        }
    }

    @Override
    public String toString() {
        return "CartItem{productId=" + productId + ", productName='" + productName + "', quantity=" + quantity + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(id, cartItem.id) && Objects.equals(productId, cartItem.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, productId);
    }
}