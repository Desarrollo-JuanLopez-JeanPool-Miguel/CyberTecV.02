package com.cybertec.repository;

import com.cybertec.model.Product;
import com.cybertec.model.ProductCategory;
import com.cybertec.model.ProductStatus;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Repositorio de Productos (ArrayList en memoria)
 * Ubicación: src/main/java/com/cybertec/repository/ProductRepository.java
 * 
 * @author CyberTec Team
 * @version 1.0.0
 */
@Repository
public class ProductRepository {

    private final List<Product> products = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    // ========== CRUD Operations ==========

    /**
     * Guardar o actualizar producto
     */
    public Product save(Product product) {
        if (product.getId() == null) {
            // Crear nuevo
            product.setId(idGenerator.getAndIncrement());
            products.add(product);
        } else {
            // Actualizar existente
            deleteById(product.getId());
            products.add(product);
        }
        return product;
    }

    /**
     * Buscar todos los productos
     */
    public List<Product> findAll() {
        return new ArrayList<>(products);
    }

    /**
     * Buscar por ID
     */
    public Optional<Product> findById(Long id) {
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    /**
     * Buscar por SKU
     */
    public Optional<Product> findBySku(String sku) {
        return products.stream()
                .filter(p -> p.getSku().equalsIgnoreCase(sku))
                .findFirst();
    }

    /**
     * Eliminar por ID
     */
    public void deleteById(Long id) {
        products.removeIf(p -> p.getId().equals(id));
    }

    /**
     * Verificar si existe por ID
     */
    public boolean existsById(Long id) {
        return products.stream()
                .anyMatch(p -> p.getId().equals(id));
    }

    /**
     * Contar productos
     */
    public long count() {
        return products.size();
    }

    // ========== Búsquedas y Filtros ==========

    /**
     * Buscar por categoría
     */
    public List<Product> findByCategory(ProductCategory category) {
        return products.stream()
                .filter(p -> p.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    /**
     * Buscar por estado
     */
    public List<Product> findByStatus(ProductStatus status) {
        return products.stream()
                .filter(p -> p.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    /**
     * Buscar productos activos con stock
     */
    public List<Product> findActiveProductsWithStock() {
        return products.stream()
                .filter(p -> p.getStatus().equals(ProductStatus.ACTIVE))
                .filter(p -> p.getStock() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Buscar por marca
     */
    public List<Product> findByBrand(String brand) {
        return products.stream()
                .filter(p -> p.getBrand() != null && 
                            p.getBrand().equalsIgnoreCase(brand))
                .collect(Collectors.toList());
    }

    /**
     * Buscar productos con stock bajo
     */
    public List<Product> findLowStockProducts() {
        return products.stream()
                .filter(Product::isLowStock)
                .collect(Collectors.toList());
    }

    /**
     * Buscar productos sin stock
     */
    public List<Product> findOutOfStockProducts() {
        return products.stream()
                .filter(Product::isOutOfStock)
                .collect(Collectors.toList());
    }

    /**
     * Buscar por nombre (búsqueda parcial)
     */
    public List<Product> findByNameContaining(String name) {
        return products.stream()
                .filter(p -> p.getName().toLowerCase()
                            .contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Buscar productos con descuento
     */
    public List<Product> findProductsWithDiscount() {
        return products.stream()
                .filter(p -> p.getOriginalPrice() != null && 
                            p.getOriginalPrice().compareTo(p.getPrice()) > 0)
                .collect(Collectors.toList());
    }

    /**
     * Búsqueda avanzada
     */
    public List<Product> searchProducts(String search, ProductCategory category, ProductStatus status) {
        return products.stream()
                .filter(p -> {
                    boolean matchesSearch = search == null || search.isEmpty() ||
                            p.getName().toLowerCase().contains(search.toLowerCase()) ||
                            p.getSku().toLowerCase().contains(search.toLowerCase()) ||
                            (p.getBrand() != null && p.getBrand().toLowerCase().contains(search.toLowerCase()));
                    
                    boolean matchesCategory = category == null || p.getCategory().equals(category);
                    boolean matchesStatus = status == null || p.getStatus().equals(status);
                    
                    return matchesSearch && matchesCategory && matchesStatus;
                })
                .collect(Collectors.toList());
    }

    /**
     * Buscar por categoría y estado
     */
    public List<Product> findByCategoryAndStatus(ProductCategory category, ProductStatus status) {
        return products.stream()
                .filter(p -> p.getCategory().equals(category))
                .filter(p -> p.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    // ========== Estadísticas ==========

    /**
     * Contar por categoría
     */
    public long countByCategory(ProductCategory category) {
        return products.stream()
                .filter(p -> p.getCategory().equals(category))
                .count();
    }

    /**
     * Contar por estado
     */
    public long countByStatus(ProductStatus status) {
        return products.stream()
                .filter(p -> p.getStatus().equals(status))
                .count();
    }

    /**
     * Limpiar todos los productos (útil para testing)
     */
    public void clear() {
        products.clear();
        idGenerator.set(1);
    }
}