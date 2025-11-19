package com.cybertec.controller;

import com.cybertec.model.Product;
import com.cybertec.model.ProductCategory;
import com.cybertec.model.ProductStatus;
import com.cybertec.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Productos (Admin)
 * Endpoints CRUD para gestión de productos
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
@Tag(name = "Productos (Admin)", description = "Gestión de productos - Requiere rol ADMIN")
@SecurityRequirement(name = "bearerAuth")   // 👈 nombre EXACTO del esquema de seguridad
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
}
    /**
     * Obtener todos los productos (Admin puede ver todos, incluidos inactivos)
     * GET /api/products
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Listar todos los productos (Admin)",
            description = "Admin puede ver productos en cualquier estado"
    )
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * Obtener producto por ID
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
 @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener producto por ID")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * Buscar productos por categoría
     * GET /api/products/category/{category}
     */
    @GetMapping("/category/{category}")
 @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Filtrar productos por categoría")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable ProductCategory category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    /**
     * Buscar productos por estado
     * GET /api/products/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
   @Operation(summary = "Filtrar productos por estado")
    public ResponseEntity<List<Product>> getProductsByStatus(@PathVariable ProductStatus status) {
        return ResponseEntity.ok(productService.getProductsByStatus(status));
    }

    // ========== CREACIÓN (Admin) ==========

    /**
     * Crear nuevo producto
     * POST /api/products
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Crear nuevo producto", description = "Solo ADMIN")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(product));
    }

    // ========== ACTUALIZACIÓN (Admin) ==========

    /**
     * Actualizar producto existente
     * PUT /api/products/{id}
     */
    @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar producto", description = "Solo ADMIN")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product productDetails) {
        return ResponseEntity.ok(productService.updateProduct(id, productDetails));
    }

    /**
     * Actualizar stock de un producto
     * PATCH /api/products/{id}/stock
     */
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
   @Operation(summary = "Actualizar stock", description = "Solo ADMIN")
    public ResponseEntity<Product> updateStock(
            @PathVariable Long id,
            @RequestParam Integer stock) {
        Product product = productService.getProductById(id);
        product.setStock(stock);
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    /**
     * Cambiar estado de un producto
     * PATCH /api/products/{id}/status
     */
    @PatchMapping("/{id}/status")
   @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar estado del producto", description = "Solo ADMIN")
    public ResponseEntity<Product> updateStatus(
            @PathVariable Long id,
            @RequestParam ProductStatus status) {
        Product product = productService.getProductById(id);
        product.setStatus(status);
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    // ========== ELIMINACIÓN (Admin) ==========

    /**
     * Eliminar producto
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
   @Operation(summary = "Eliminar producto", description = "Solo ADMIN")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // ========== ESTADÍSTICAS (Admin) ==========

    /**
     * Obtener estadísticas de inventario
     * GET /api/products/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Estadísticas de inventario", description = "Solo ADMIN")
    public ResponseEntity<Object> getInventoryStats() {
        long totalProducts = productService.getAllProducts().size();
        long activeProducts = productService.getProductsByStatus(ProductStatus.ACTIVE).size();
        long lowStockProducts = productService.getAllProducts().stream()
                .filter(Product::isLowStock)
                .count();
        long outOfStockProducts = productService.getAllProducts().stream()
                .filter(Product::isOutOfStock)
                .count();

        return ResponseEntity.ok(java.util.Map.of(
                "totalProducts", totalProducts,
                "activeProducts", activeProducts,
                "lowStockProducts", lowStockProducts,
                "outOfStockProducts", outOfStockProducts
        ));
    }
}
