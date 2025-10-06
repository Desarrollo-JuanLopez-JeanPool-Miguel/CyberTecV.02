package com.cybertec.service;

import com.cybertec.model.Product;
import com.cybertec.model.ProductCategory;
import com.cybertec.model.ProductStatus;
import com.cybertec.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de Productos
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ========== CRUD ==========

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
    }

    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con SKU: " + sku));
    }

    public Product createProduct(Product product) {
        log.info("Creando producto: {}", product.getName());
        
        if (productRepository.findBySku(product.getSku()).isPresent()) {
            throw new RuntimeException("Ya existe un producto con el SKU: " + product.getSku());
        }
        
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product productDetails) {
        log.info("Actualizando producto ID: {}", id);
        
        Product product = getProductById(id);
        
        product.setName(productDetails.getName());
        product.setCategory(productDetails.getCategory());
        product.setBrand(productDetails.getBrand());
        product.setShortDescription(productDetails.getShortDescription());
        product.setSpecifications(productDetails.getSpecifications());
        product.setPrice(productDetails.getPrice());
        product.setOriginalPrice(productDetails.getOriginalPrice());
        product.setStock(productDetails.getStock());
        product.setMinStock(productDetails.getMinStock());
        product.setStatus(productDetails.getStatus());
        product.setImageUrl(productDetails.getImageUrl());
        
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        log.info("Eliminando producto ID: {}", id);
        productRepository.deleteById(id);
    }

    // ========== Búsquedas ==========

    public List<Product> getProductsByCategory(ProductCategory category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> getProductsByStatus(ProductStatus status) {
        return productRepository.findByStatus(status);
    }

    public List<Product> getActiveProducts() {
        return productRepository.findActiveProductsWithStock();
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public List<Product> getOutOfStockProducts() {
        return productRepository.findOutOfStockProducts();
    }

    public List<Product> searchProducts(String search, ProductCategory category, ProductStatus status) {
        return productRepository.searchProducts(search, category, status);
    }

    public List<Product> getProductsWithDiscount() {
        return productRepository.findProductsWithDiscount();
    }

    // ========== Stock ==========

    public Product updateStock(Long id, Integer quantity) {
        Product product = getProductById(id);
        product.setStock(quantity);
        return productRepository.save(product);
    }

    public Product increaseStock(Long id, Integer quantity) {
        Product product = getProductById(id);
        product.setStock(product.getStock() + quantity);
        return productRepository.save(product);
    }

    public Product decreaseStock(Long id, Integer quantity) {
        Product product = getProductById(id);
        int newStock = product.getStock() - quantity;
        
        if (newStock < 0) {
            throw new RuntimeException("Stock insuficiente. Stock actual: " + product.getStock());
        }
        
        product.setStock(newStock);
        return productRepository.save(product);
    }

    // ========== Estadísticas ==========

    public Map<String, Object> getInventoryStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalProducts = productRepository.count();
        long activeProducts = productRepository.countByStatus(ProductStatus.ACTIVE);
        long lowStockProducts = productRepository.findLowStockProducts().size();
        long outOfStockProducts = productRepository.findOutOfStockProducts().size();
        
        stats.put("totalProducts", totalProducts);
        stats.put("activeProducts", activeProducts);
        stats.put("lowStockProducts", lowStockProducts);
        stats.put("outOfStockProducts", outOfStockProducts);
        stats.put("draftProducts", productRepository.countByStatus(ProductStatus.DRAFT));
        
        return stats;
    }

    public Map<String, Long> getProductCountByCategory() {
        Map<String, Long> counts = new HashMap<>();
        
        for (ProductCategory category : ProductCategory.values()) {
            counts.put(category.name(), productRepository.countByCategory(category));
        }
        
        return counts;
    }

    // ========== Inicialización de datos de prueba ==========

    public void initializeSampleData() {
        if (productRepository.count() > 0) {
            return; // Ya hay datos
        }

        log.info("Inicializando productos de prueba...");

        // GPU
        Product gpu1 = new Product();
        gpu1.setName("NVIDIA RTX 4080 SUPER Gaming OC");
        gpu1.setSku("GPU-RTX4080S-001");
        gpu1.setCategory(ProductCategory.GPU);
        gpu1.setBrand("ASUS ROG");
        gpu1.setShortDescription("16GB GDDR6X • Ray Tracing • DLSS 3.0");
        gpu1.setSpecifications("16GB GDDR6X\nRay Tracing\nDLSS 3.0\nBoost Clock: 2610 MHz");
        gpu1.setPrice(new BigDecimal("1890000"));
        gpu1.setOriginalPrice(new BigDecimal("2150000"));
        gpu1.setStock(15);
        gpu1.setMinStock(5);
        gpu1.setStatus(ProductStatus.ACTIVE);
        gpu1.setRating(new BigDecimal("4.8"));
        gpu1.setReviewCount(89);
        productRepository.save(gpu1);

        // CPU
        Product cpu1 = new Product();
        cpu1.setName("AMD Ryzen 7 7800X3D");
        cpu1.setSku("CPU-R7-7800X3D");
        cpu1.setCategory(ProductCategory.CPU);
        cpu1.setBrand("AMD");
        cpu1.setShortDescription("8 Cores • 16 Threads • Gaming optimizado");
        cpu1.setSpecifications("8 Cores / 16 Threads\nSocket AM5\n3D V-Cache\nBase Clock: 4.2 GHz");
        cpu1.setPrice(new BigDecimal("620000"));
        cpu1.setStock(28);
        cpu1.setMinStock(10);
        cpu1.setStatus(ProductStatus.ACTIVE);
        cpu1.setRating(new BigDecimal("4.9"));
        cpu1.setReviewCount(145);
        productRepository.save(cpu1);

        // RAM
        Product ram1 = new Product();
        ram1.setName("Corsair Vengeance DDR5 32GB");
        ram1.setSku("RAM-COR-32G-DDR5");
        ram1.setCategory(ProductCategory.RAM);
        ram1.setBrand("Corsair");
        ram1.setShortDescription("5600MHz • RGB • 2×16GB");
        ram1.setSpecifications("32GB (2x16GB)\nDDR5 5600MHz\nRGB Lighting\nCL36");
        ram1.setPrice(new BigDecimal("350000"));
        ram1.setStock(25);
        ram1.setMinStock(8);
        ram1.setStatus(ProductStatus.ACTIVE);
        ram1.setRating(new BigDecimal("4.7"));
        ram1.setReviewCount(67);
        productRepository.save(ram1);

        log.info("Productos de prueba creados exitosamente");
    }
}