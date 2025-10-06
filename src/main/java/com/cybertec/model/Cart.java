package com.cybertec.model;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Modelo de Carrito de Compras
 * Ubicación: src/main/java/com/cybertec/model/Cart.java
 * 
 * @author CyberTec Team
 * @version 1.0.0
 */
public class Cart {
    
    private Long id;
    
    @NotNull(message = "El usuario es obligatorio")
    private Long userId;
    
    private List<CartItem> items;
    
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal shipping;
    private BigDecimal total;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructores
    public Cart() {
        this.items = new ArrayList<>();
        this.subtotal = BigDecimal.ZERO;
        this.discount = BigDecimal.ZERO;
        this.shipping = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Cart(Long id, Long userId) {
        this();
        this.id = id;
        this.userId = userId;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { 
        this.items = items;
        calculateTotals();
    }

    public BigDecimal getSubtotal() { 
        calculateTotals();
        return subtotal; 
    }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { 
        this.discount = discount;
        calculateTotals();
    }

    public BigDecimal getShipping() { return shipping; }
    public void setShipping(BigDecimal shipping) { 
        this.shipping = shipping;
        calculateTotals();
    }

    public BigDecimal getTotal() { 
        calculateTotals();
        return total; 
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Métodos de utilidad

    /**
     * Agregar un item al carrito
     */
    public void addItem(CartItem item) {
        // Buscar si ya existe el producto
        CartItem existingItem = findItemByProductId(item.getProductId());
        
        if (existingItem != null) {
            // Si existe, incrementar cantidad
            existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
        } else {
            // Si no existe, agregar nuevo
            this.items.add(item);
        }
        
        this.updatedAt = LocalDateTime.now();
        calculateTotals();
    }

    /**
     * Remover un item del carrito
     */
    public void removeItem(Long productId) {
        this.items.removeIf(item -> item.getProductId().equals(productId));
        this.updatedAt = LocalDateTime.now();
        calculateTotals();
    }

    /**
     * Actualizar cantidad de un item
     */
    public void updateItemQuantity(Long productId, Integer quantity) {
        CartItem item = findItemByProductId(productId);
        if (item != null) {
            if (quantity <= 0) {
                removeItem(productId);
            } else {
                item.setQuantity(quantity);
                this.updatedAt = LocalDateTime.now();
                calculateTotals();
            }
        }
    }

    /**
     * Limpiar el carrito
     */
    public void clear() {
        this.items.clear();
        this.subtotal = BigDecimal.ZERO;
        this.discount = BigDecimal.ZERO;
        this.shipping = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Buscar item por ID de producto
     */
    private CartItem findItemByProductId(Long productId) {
        return items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Calcular totales del carrito
     */
    private void calculateTotals() {
        // Calcular subtotal
        this.subtotal = items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calcular total: subtotal - descuento + envío
        this.total = this.subtotal
                .subtract(this.discount)
                .add(this.shipping);
        
        // No permitir totales negativos
        if (this.total.compareTo(BigDecimal.ZERO) < 0) {
            this.total = BigDecimal.ZERO;
        }
    }

    /**
     * Obtener cantidad total de items
     */
    public int getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    /**
     * Verificar si el carrito está vacío
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Verificar si tiene un producto específico
     */
    public boolean hasProduct(Long productId) {
        return items.stream()
                .anyMatch(item -> item.getProductId().equals(productId));
    }

    @Override
    public String toString() {
        return "Cart{" +
                "id=" + id +
                ", userId=" + userId +
                ", itemsCount=" + items.size() +
                ", total=" + total +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cart cart = (Cart) o;
        return Objects.equals(id, cart.id) && Objects.equals(userId, cart.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId);
    }
}