package com.cybertec.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidad JPA de Carrito de Compras
 */
@Entity
@Table(name = "carts",
       indexes = {
           @Index(name = "idx_cart_user", columnList = "user_id")
       })
public class Cart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true,
                foreignKey = @ForeignKey(name = "fk_cart_user"))
    private User user;
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, 
               orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CartItem> items = new ArrayList<>();
    
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;
    
    @Column(name = "discount", precision = 12, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;
    
    @Column(name = "shipping", precision = 12, scale = 2)
    private BigDecimal shipping = BigDecimal.ZERO;
    
    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ========== CONSTRUCTORES ==========

    public Cart() {
        this.items = new ArrayList<>();
        this.subtotal = BigDecimal.ZERO;
        this.discount = BigDecimal.ZERO;
        this.shipping = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ========== GETTERS Y SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
        calculateTotals();
    }

    public BigDecimal getSubtotal() {
        calculateTotals();
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
        calculateTotals();
    }

    public BigDecimal getShipping() {
        return shipping;
    }

    public void setShipping(BigDecimal shipping) {
        this.shipping = shipping;
        calculateTotals();
    }

    public BigDecimal getTotal() {
        calculateTotals();
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    public void addItem(CartItem item) {
        CartItem existingItem = findItemByProductId(item.getProduct().getId());
        
        if (existingItem != null) {
          existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
        } else {
            items.add(item);
            item.setCart(this);
        }
             calculateTotals();
    }
   public void removeItem(Long productId) {
        items.removeIf(item -> item.getProduct().getId().equals(productId));
        calculateTotals();
    }

public void updateItemQuantity(Long productId, Integer quantity) {
        CartItem item = findItemByProductId(productId);
        if (item != null) {
            if (quantity <= 0) {
                removeItem(productId);
            } else {
                item.setQuantity(quantity);
                                calculateTotals();
            }
        }
    }

public void clear() {
        items.clear();
        subtotal = BigDecimal.ZERO;
        discount = BigDecimal.ZERO;
        shipping = BigDecimal.ZERO;
        total = BigDecimal.ZERO;
    }

    private CartItem findItemByProductId(Long productId) {
        return items.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    public void calculateTotals() {
        this.subtotal = items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);        
        this.total = this.subtotal
                .subtract(this.discount != null ? this.discount : BigDecimal.ZERO)
                .add(this.shipping != null ? this.shipping : BigDecimal.ZERO);

        if (this.total.compareTo(BigDecimal.ZERO) < 0) {
            this.total = BigDecimal.ZERO;
        }
    }
public int getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

   public boolean isEmpty() {
        return items.isEmpty();
    }
 public boolean hasProduct(Long productId) {
        return items.stream()
                .anyMatch(item -> item.getProduct().getId().equals(productId));
    }

    @PrePersist
    protected void onCreate() {
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (discount == null) discount = BigDecimal.ZERO;
        if (shipping == null) shipping = BigDecimal.ZERO;
        if (total == null) total = BigDecimal.ZERO;
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotals();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cart cart = (Cart) o;
        return Objects.equals(id, cart.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Cart{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", itemsCount=" + items.size() +
                ", total=" + total +
                '}';
    }
}