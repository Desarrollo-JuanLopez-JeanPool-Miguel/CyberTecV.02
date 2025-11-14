package com.cybertec.controller;

import com.cybertec.model.Cart;
import com.cybertec.model.User;
import com.cybertec.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Controlador del Carrito de Compras
 * Requiere autenticación JWT
 */
@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
@Tag(name = "Carrito de Compras", description = "Gestión del carrito de compras del usuario")
@SecurityRequirement(name = "Bearer Authentication")
public class CartController {

     private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
     }

    // ========== VER CARRITO ==========

    /**
     * Obtener el carrito del usuario autenticado
     * GET /api/cart
     */
    @GetMapping
    @Operation(summary = "Ver mi carrito", 
               description = "Obtiene el carrito completo del usuario con todos los items y totales")
    public ResponseEntity<Cart> getMyCart(@AuthenticationPrincipal User user) {
        Cart cart = cartService.getCartByUserId(user.getId());
        return ResponseEntity.ok(cart);
    }

    // ========== AGREGAR AL CARRITO ==========

    /**
     * Agregar un producto al carrito
     * POST /api/cart/items
     * Body: { "productId": 1, "quantity": 2 }
     */
    @PostMapping("/items")
    @Operation(summary = "Agregar producto al carrito")
    public ResponseEntity<Cart> addItemToCart(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Object> request) {
        
        Long productId = Long.valueOf(request.get("productId").toString());
        Integer quantity = Integer.valueOf(request.get("quantity").toString());
        
        Cart cart = cartService.addItemToCart(user.getId(), productId, quantity);
        return ResponseEntity.ok(cart);
    }

    // ========== ACTUALIZAR CANTIDAD ==========

    /**
     * Actualizar cantidad de un producto en el carrito
     * PUT /api/cart/items/{productId}
     * Body: { "quantity": 3 }
     */
    @PutMapping("/items/{productId}")
    @Operation(summary = "Actualizar cantidad de producto")
    public ResponseEntity<Cart> updateItemQuantity(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId,
            @RequestBody Map<String, Integer> request) {
        
        Integer quantity = request.get("quantity");
        Cart cart = cartService.updateItemQuantity(user.getId(), productId, quantity);
        return ResponseEntity.ok(cart);
    }

    // ========== ELIMINAR ITEM ==========

    /**
     * Eliminar un producto del carrito
     * DELETE /api/cart/items/{productId}
     */
    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Eliminar producto del carrito")
    public ResponseEntity<Cart> removeItemFromCart(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId) {
        
        Cart cart = cartService.removeItemFromCart(user.getId(), productId);
        return ResponseEntity.ok(cart);
    }

    // ========== VACIAR CARRITO ==========

    /**
     * Vaciar el carrito completamente
     * DELETE /api/cart/clear
     */
    @DeleteMapping("/clear")
    @Operation(summary = "Vaciar carrito")
    public ResponseEntity<Cart> clearCart(@AuthenticationPrincipal User user) {
        Cart cart = cartService.clearCart(user.getId());
        return ResponseEntity.ok(cart);
    }

    // ========== DESCUENTOS Y ENVÍO ==========

    /**
     * Aplicar descuento al carrito
     * PUT /api/cart/discount
     * Body: { "discount": 50.00 }
     */
    @PutMapping("/discount")
    @Operation(summary = "Aplicar descuento")
    public ResponseEntity<Cart> applyDiscount(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, BigDecimal> request) {
        
        BigDecimal discount = request.get("discount");
        Cart cart = cartService.applyDiscount(user.getId(), discount);
        return ResponseEntity.ok(cart);
    }

    /**
     * Actualizar costo de envío
     * PUT /api/cart/shipping
     * Body: { "shipping": 15.00 }
     */
    @PutMapping("/shipping")
    @Operation(summary = "Actualizar costo de envío")
    public ResponseEntity<Cart> updateShipping(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, BigDecimal> request) {
        
        BigDecimal shipping = request.get("shipping");
        Cart cart = cartService.updateShipping(user.getId(), shipping);
        return ResponseEntity.ok(cart);
    }

    // ========== INFORMACIÓN DEL CARRITO ==========

    /**
     * Obtener resumen del carrito (sin items completos)
     * GET /api/cart/summary
     */
    @GetMapping("/summary")
    @Operation(summary = "Resumen del carrito")
    public ResponseEntity<Map<String, Object>> getCartSummary(@AuthenticationPrincipal User user) {
        Cart cart = cartService.getCartByUserId(user.getId());
        
        return ResponseEntity.ok(Map.of(
            "totalItems", cart.getTotalItems(),
            "subtotal", cart.getSubtotal(),
            "discount", cart.getDiscount(),
            "shipping", cart.getShipping(),
            "total", cart.getTotal(),
            "isEmpty", cart.isEmpty()
        ));
    }
}