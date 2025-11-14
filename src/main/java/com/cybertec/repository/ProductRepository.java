package com.cybertec.repository;

import com.cybertec.model.Product;
import com.cybertec.model.ProductCategory;
import com.cybertec.model.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;
 
/**
 * Repositorio JPA de Productos
 * Spring Data JPA genera automáticamente la implementación
 * 
 * @author CyberTec Team
 * @version 2.0.0 - Database Version
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ========== BÚSQUEDAS BÁSICAS ==========

    /**
     * Buscar por SKU (único)
     */
    Optional<Product> findBySku(String sku);

    /**
     * Verificar si existe por SKU
     */
    boolean existsBySku(String sku);

    // ========== BÚSQUEDAS POR CATEGORÍA Y ESTADO ==========

    /**
     * Buscar por categoría
     */
    List<Product> findByCategory(ProductCategory category);

    /**
     * Buscar por estado
     */
    List<Product> findByStatus(ProductStatus status);

    /**
     * Buscar por categoría y estado
     */
    List<Product> findByCategoryAndStatus(ProductCategory category, ProductStatus status);

    /**
     * Buscar productos activos con stock
     */
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.stock > 0")
    List<Product> findActiveProductsWithStock();

    // ========== BÚSQUEDAS POR MARCA ==========

    /**
     * Buscar por marca
     */
    List<Product> findByBrand(String brand);

    /**
     * Buscar por marca (case-insensitive)
     */
    List<Product> findByBrandIgnoreCase(String brand);

    // ========== BÚSQUEDAS POR STOCK ==========

    /**
     * Buscar productos con stock bajo
     * (stock <= minStock)
     */
    @Query("SELECT p FROM Product p WHERE p.minStock IS NOT NULL AND p.stock <= p.minStock")
    List<Product> findLowStockProducts();

    /**
     * Buscar productos sin stock
     */
    @Query("SELECT p FROM Product p WHERE p.stock = 0")
    List<Product> findOutOfStockProducts();

    // ========== BÚSQUEDAS POR NOMBRE ==========

    /**
     * Buscar por nombre (búsqueda parcial, case-insensitive)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByNameContaining(@Param("name") String name);

    // ========== BÚSQUEDAS CON DESCUENTO ==========

    /**
     * Buscar productos con descuento
     * (originalPrice > price)
     */
    @Query("SELECT p FROM Product p WHERE p.originalPrice IS NOT NULL AND p.originalPrice > p.price")
    List<Product> findProductsWithDiscount();

    // ========== BÚSQUEDA AVANZADA ==========

    /**
     * Búsqueda avanzada con múltiples filtros
     * Busca en nombre, SKU y marca
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:status IS NULL OR p.status = :status)")
    List<Product> searchProducts(@Param("search") String search,
                                 @Param("category") ProductCategory category,
                                 @Param("status") ProductStatus status);

    // ========== ESTADÍSTICAS ==========

    /**
     * Contar por categoría
     */
    long countByCategory(ProductCategory category);

    /**
     * Contar por estado
     */
    long countByStatus(ProductStatus status);

    /**
     * Contar productos activos
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = 'ACTIVE'")
    long countActiveProducts();

    /**
     * Contar productos con stock bajo
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.minStock IS NOT NULL AND p.stock <= p.minStock")
    long countLowStockProducts();

    /**
     * Contar productos sin stock
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stock = 0")
    long countOutOfStockProducts();

    // ========== BÚSQUEDAS ORDENADAS ==========

    /**
     * Buscar productos activos ordenados por nombre
     */
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' ORDER BY p.name ASC")
    List<Product> findActiveProductsOrderByName();

    /**
     * Buscar productos más vendidos (por reviewCount)
     */
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' ORDER BY p.reviewCount DESC")
    List<Product> findTopRatedProducts();

    /**
     * Buscar productos por categoría ordenados por precio
     */
    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.status = 'ACTIVE' ORDER BY p.price ASC")
    List<Product> findByCategoryOrderByPriceAsc(@Param("category") ProductCategory category);
}