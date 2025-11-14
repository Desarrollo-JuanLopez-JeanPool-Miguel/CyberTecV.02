package com.cybertec.controller;

import com.cybertec.model.Product;
import com.cybertec.model.ProductCategory;
import com.cybertec.model.ProductStatus;
import com.cybertec.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador público del catálogo de productos
 * NO requiere autenticación - Acceso público para el frontend
 * 
 * @author CyberTec Team
 */
@RestController
@RequestMapping("/api/catalog")
@CrossOrigin(origins = "*")
@Tag(name = "Catálogo Público", description = "Endpoints públicos para el catálogo de productos")
public class CatalogController {

    private final ProductService productService;

    public CatalogController(ProductService productService) {
        this.productService = productService;
    }

    // ========== ENDPOINTS PÚBLICOS ==========

    /**
     * Obtener todos los productos ACTIVOS (públicamente visibles)
     * GET /api/catalog/products
     */
    @GetMapping("/products")
    @Operation(summary = "Listar todos los productos activos", 
               description = "Retorna todos los productos con estado ACTIVE y disponibles para la venta")
    public ResponseEntity<List<Product>> getAllActiveProducts() {
        List<Product> products = productService.getAllProducts().stream()
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .toList();
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener un producto por ID
     * GET /api/catalog/products/{id}
     */
    @GetMapping("/products/{id}")
    @Operation(summary = "Obtener producto por ID")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        
        if (product.getStatus() != ProductStatus.ACTIVE) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(product);
    }

    /**
     * Buscar productos por categoría
     * GET /api/catalog/products/category/{category}
     */
    @GetMapping("/products/category/{category}")
    @Operation(summary = "Filtrar productos por categoría")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable ProductCategory category) {
        List<Product> products = productService.getAllProducts().stream()
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .filter(p -> p.getCategory() == category)
                .toList();
        return ResponseEntity.ok(products);
    }

    /**
     * Buscar productos por nombre (búsqueda)
     * GET /api/catalog/products/search?q=nvidia
     */
    @GetMapping("/products/search")
    @Operation(summary = "Buscar productos por nombre o marca")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String q) {
        List<Product> products = productService.getAllProducts().stream()
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .filter(p -> 
                    p.getName().toLowerCase().contains(q.toLowerCase()) ||
                    (p.getBrand() != null && p.getBrand().toLowerCase().contains(q.toLowerCase()))
                )
                .toList();
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener productos destacados (con mayor rating)
     * GET /api/catalog/products/featured
     */
    @GetMapping("/products/featured")
    @Operation(summary = "Obtener productos destacados")
    public ResponseEntity<List<Product>> getFeaturedProducts() {
        List<Product> products = productService.getAllProducts().stream()
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .sorted((p1, p2) -> p2.getRating().compareTo(p1.getRating()))
                .limit(6)
                .toList();
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener productos con descuento
     * GET /api/catalog/products/deals
     */
    @GetMapping("/products/deals")
    @Operation(summary = "Obtener productos con descuento")
    public ResponseEntity<List<Product>> getProductsOnSale() {
        List<Product> products = productService.getAllProducts().stream()
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .filter(Product::hasDiscount)
                .toList();
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener todas las categorías disponibles
     * GET /api/catalog/categories
     */
    @GetMapping("/categories")
    @Operation(summary = "Listar todas las categorías")
    public ResponseEntity<List<ProductCategory>> getAllCategories() {
        return ResponseEntity.ok(List.of(ProductCategory.values()));
    }

    /**
     * Obtener estadísticas del catálogo
     * GET /api/catalog/stats
     */
    @GetMapping("/stats")
    @Operation(summary = "Estadísticas del catálogo")
    public ResponseEntity<Map<String, Object>> getCatalogStats() {
        long totalProducts = productService.getAllProducts().stream()
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .count();
        
        long productsWithDiscount = productService.getAllProducts().stream()
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .filter(Product::hasDiscount)
                .count();
        
        return ResponseEntity.ok(Map.of(
            "totalProducts", totalProducts,
            "productsWithDiscount", productsWithDiscount,
            "categories", ProductCategory.values().length
        ));
    }
}