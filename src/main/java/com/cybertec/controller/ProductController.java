package com.cybertec.controller;

import com.cybertec.model.Product;
import com.cybertec.model.ProductCategory;
import com.cybertec.model.ProductStatus;
import com.cybertec.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller de Productos
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"})
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * GET /api/products
     * Obtener todos los productos
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        log.info("GET /api/products");
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * GET /api/products/{id}
     * Obtener producto por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        log.info("GET /api/products/{}", id);
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * GET /api/products/sku/{sku}
     * Obtener producto por SKU
     */
    @GetMapping("/sku/{sku}")
    public ResponseEntity<Product> getProductBySku(@PathVariable String sku) {
        log.info("GET /api/products/sku/{}", sku);
        return ResponseEntity.ok(productService.getProductBySku(sku));
    }

    /**
     * GET /api/products/category/{category}
     * Obtener productos por categoría
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        log.info("GET /api/products/category/{}", category);
        ProductCategory productCategory = ProductCategory.fromCode(category);
        return ResponseEntity.ok(productService.getProductsByCategory(productCategory));
    }

    /**
     * GET /api/products/status/{status}
     * Obtener productos por estado
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Product>> getProductsByStatus(@PathVariable String status) {
        log.info("GET /api/products/status/{}", status);
        ProductStatus productStatus = ProductStatus.fromCode(status);
        return ResponseEntity.ok(productService.getProductsByStatus(productStatus));
    }

    /**
     * GET /api/products/active
     * Obtener productos activos con stock
     */
    @GetMapping("/active")
    public ResponseEntity<List<Product>> getActiveProducts() {
        log.info("GET /api/products/active");
        return ResponseEntity.ok(productService.getActiveProducts());
    }

    /**
     * GET /api/products/discounts
     * Obtener productos con descuento
     */
    @GetMapping("/discounts")
    public ResponseEntity<List<Product>> getProductsWithDiscount() {
        log.info("GET /api/products/discounts");
        return ResponseEntity.ok(productService.getProductsWithDiscount());
    }

    /**
     * GET /api/products/search
     * Búsqueda avanzada
     */
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        log.info("GET /api/products/search - query: {}, category: {}, status: {}", query, category, status);
        
        ProductCategory productCategory = category != null ? ProductCategory.fromCode(category) : null;
        ProductStatus productStatus = status != null ? ProductStatus.fromCode(status) : null;
        
        return ResponseEntity.ok(productService.searchProducts(query, productCategory, productStatus));
    }

    /**
     * POST /api/products
     * Crear producto (Solo ADMIN)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        log.info("POST /api/products - Creating: {}", product.getName());
        Product created = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/products/{id}
     * Actualizar producto (Solo ADMIN)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody Product product) {
        log.info("PUT /api/products/{}", id);
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    /**
     * DELETE /api/products/{id}
     * Eliminar producto (Solo ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long id) {
        log.info("DELETE /api/products/{}", id);
        productService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "Producto eliminado exitosamente"));
    }

    /**
     * PATCH /api/products/{id}/stock
     * Actualizar stock (Solo ADMIN)
     */
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        log.info("PATCH /api/products/{}/stock", id);
        Integer quantity = request.get("quantity");
        return ResponseEntity.ok(productService.updateStock(id, quantity));
    }

    /**
     * GET /api/products/stats
     * Estadísticas de inventario (Solo ADMIN)
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getInventoryStats() {
        log.info("GET /api/products/stats");
        return ResponseEntity.ok(productService.getInventoryStats());
    }

    /**
     * GET /api/products/stats/category
     * Conteo por categoría (Solo ADMIN)
     */
    @GetMapping("/stats/category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getProductCountByCategory() {
        log.info("GET /api/products/stats/category");
        return ResponseEntity.ok(productService.getProductCountByCategory());
    }

    /**
     * GET /api/products/low-stock
     * Productos con stock bajo (Solo ADMIN)
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Product>> getLowStockProducts() {
        log.info("GET /api/products/low-stock");
        return ResponseEntity.ok(productService.getLowStockProducts());
    }
}