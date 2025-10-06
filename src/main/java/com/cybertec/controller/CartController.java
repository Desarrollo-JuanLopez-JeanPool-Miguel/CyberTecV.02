package com.cybertec.controller;

import com.cybertec.model.Cart;
import com.cybertec.model.User;
import com.cybertec.service.CartService;
import com.cybertec.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Controller de Carrito
 */
@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"})
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);
    
    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    /**
     * GET /api/cart
     * Obtener carrito del usuario actual
     */
    @GetMapping
    public ResponseEntity<Cart> getCart(Authentication authentication) {
        log.info("GET /api/cart");
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    /**
     * POST /api/cart/items
     * Agregar item al carrito
     */
    @PostMapping("/items")
    public ResponseEntity<Cart> addItem(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        log.info("POST /api/cart/items");
        
        Long userId = getUserIdFromAuth(authentication);
        Long productId = Long.valueOf(request.get("productId").toString());
        Integer quantity = Integer.valueOf(request.get("quantity").toString());
        
        Cart cart = cartService.addItemToCart(userId, productId, quantity);
        return ResponseEntity.ok(cart);
    }

    /**
     * DELETE /api/cart/items/{productId}
     * Remover item del carrito
     */
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Cart> removeItem(
            @PathVariable Long productId,
            Authentication authentication) {
        log.info("DELETE /api/cart/items/{}", productId);
        
        Long userId = getUserIdFromAuth(authentication);
        Cart cart = cartService.removeItemFromCart(userId, productId);
        return ResponseEntity.ok(cart);
    }

    /**
     * PATCH /api/cart/items/{productId}
     * Actualizar cantidad de un item
     */
    @PatchMapping("/items/{productId}")
    public ResponseEntity<Cart> updateItemQuantity(
            @PathVariable Long productId,
            @RequestBody Map<String, Integer> request,
            Authentication authentication) {
        log.info("PATCH /api/cart/items/{}", productId);
        
        Long userId = getUserIdFromAuth(authentication);
        Integer quantity = request.get("quantity");
        
        Cart cart = cartService.updateItemQuantity(userId, productId, quantity);
        return ResponseEntity.ok(cart);
    }

    /**
     * DELETE /api/cart
     * Limpiar carrito
     */
    @DeleteMapping
    public ResponseEntity<Cart> clearCart(Authentication authentication) {
        log.info("DELETE /api/cart");
        
        Long userId = getUserIdFromAuth(authentication);
        Cart cart = cartService.clearCart(userId);
        return ResponseEntity.ok(cart);
    }

    /**
     * PATCH /api/cart/discount
     * Aplicar descuento
     */
    @PatchMapping("/discount")
    public ResponseEntity<Cart> applyDiscount(
            @RequestBody Map<String, BigDecimal> request,
            Authentication authentication) {
        log.info("PATCH /api/cart/discount");
        
        Long userId = getUserIdFromAuth(authentication);
        BigDecimal discount = request.get("discount");
        
        Cart cart = cartService.applyDiscount(userId, discount);
        return ResponseEntity.ok(cart);
    }

    /**
     * GET /api/cart/checkout
     * Preparar checkout
     */
    @GetMapping("/checkout")
    public ResponseEntity<Cart> prepareCheckout(Authentication authentication) {
        log.info("GET /api/cart/checkout");
        
        Long userId = getUserIdFromAuth(authentication);
        Cart cart = cartService.prepareCheckout(userId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Obtener ID del usuario autenticado
     */
    private Long getUserIdFromAuth(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        return user.getId();
    }
}