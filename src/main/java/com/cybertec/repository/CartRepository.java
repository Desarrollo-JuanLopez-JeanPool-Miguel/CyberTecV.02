package com.cybertec.repository;

import com.cybertec.model.Cart;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Repositorio de Carritos (ArrayList en memoria)
 * Ubicación: src/main/java/com/cybertec/repository/CartRepository.java
 * 
 * @author CyberTec Team
 * @version 1.0.0
 */
@Repository
public class CartRepository {

    private final List<Cart> carts = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    // ========== CRUD Operations ==========

    /**
     * Guardar o actualizar carrito
     */
    public Cart save(Cart cart) {
        if (cart.getId() == null) {
            // Crear nuevo
            cart.setId(idGenerator.getAndIncrement());
            carts.add(cart);
        } else {
            // Actualizar existente
            deleteById(cart.getId());
            carts.add(cart);
        }
        return cart;
    }

    /**
     * Buscar todos los carritos
     */
    public List<Cart> findAll() {
        return new ArrayList<>(carts);
    }

    /**
     * Buscar por ID
     */
    public Optional<Cart> findById(Long id) {
        return carts.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }

    /**
     * Buscar por ID de usuario
     */
    public Optional<Cart> findByUserId(Long userId) {
        return carts.stream()
                .filter(c -> c.getUserId().equals(userId))
                .findFirst();
    }

    /**
     * Eliminar por ID
     */
    public void deleteById(Long id) {
        carts.removeIf(c -> c.getId().equals(id));
    }

    /**
     * Eliminar por ID de usuario
     */
    public void deleteByUserId(Long userId) {
        carts.removeIf(c -> c.getUserId().equals(userId));
    }

    /**
     * Verificar si existe por ID
     */
    public boolean existsById(Long id) {
        return carts.stream()
                .anyMatch(c -> c.getId().equals(id));
    }

    /**
     * Verificar si existe por ID de usuario
     */
    public boolean existsByUserId(Long userId) {
        return carts.stream()
                .anyMatch(c -> c.getUserId().equals(userId));
    }

    /**
     * Contar carritos
     */
    public long count() {
        return carts.size();
    }

    /**
     * Limpiar todos los carritos (útil para testing)
     */
    public void clear() {
        carts.clear();
        idGenerator.set(1);
    }
}