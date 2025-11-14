package com.cybertec.service;

import com.cybertec.model.Cart;
import com.cybertec.model.CartItem;
import com.cybertec.model.Product;
import com.cybertec.model.User;
import com.cybertec.repository.CartRepository;
import com.cybertec.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Servicio de Carrito de Compras
 */
@Service
@Transactional
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);
    
    private final CartRepository cartRepository;
    private final ProductService productService;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository, ProductService productService, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.productService = productService;
        this.userRepository = userRepository;
    }

    // ========== CRUD ==========

    public Cart getCartById(Long id) {
        return cartRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado con ID: " + id));
    }

    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createCartForUser(userId));
    }

    private Cart createCartForUser(Long userId) {
        log.info("Creando carrito para usuario ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Cart cart = new Cart();
        cart.setUser(user);
        
        return cartRepository.save(cart);
    }

    // ========== Operaciones del carrito ==========

    public Cart addItemToCart(Long userId, Long productId, Integer quantity) {
        log.info("Agregando producto {} al carrito del usuario {}", productId, userId);
        
        Cart cart = getCartByUserId(userId);
        Product product = productService.getProductById(productId);
        
        // Validar stock
        if (product.getStock() < quantity) {
            throw new RuntimeException("Stock insuficiente. Disponible: " + product.getStock());
        }
        
        // Crear item
        CartItem item = new CartItem(product, quantity);
        cart.addItem(item);
        
        return cartRepository.save(cart);
    }

    public Cart removeItemFromCart(Long userId, Long productId) {
        log.info("Removiendo producto {} del carrito del usuario {}", productId, userId);
        
        Cart cart = getCartByUserId(userId);
        cart.removeItem(productId);
        
        return cartRepository.save(cart);
    }

    public Cart updateItemQuantity(Long userId, Long productId, Integer quantity) {
        log.info("Actualizando cantidad del producto {} a {} en carrito del usuario {}", 
                 productId, quantity, userId);
        
        Cart cart = getCartByUserId(userId);
        Product product = productService.getProductById(productId);
        
        // Validar stock
        if (product.getStock() < quantity) {
            throw new RuntimeException("Stock insuficiente. Disponible: " + product.getStock());
        }
        
        cart.updateItemQuantity(productId, quantity);
        
        return cartRepository.save(cart);
    }

    public Cart clearCart(Long userId) {
        log.info("Limpiando carrito del usuario {}", userId);
        
        Cart cart = getCartByUserId(userId);
        cart.clear();
        
        return cartRepository.save(cart);
    }

    // ========== Descuentos y Envío ==========

    public Cart applyDiscount(Long userId, BigDecimal discount) {
        Cart cart = getCartByUserId(userId);
        cart.setDiscount(discount);
        return cartRepository.save(cart);
    }

    public Cart updateShipping(Long userId, BigDecimal shipping) {
        Cart cart = getCartByUserId(userId);
        cart.setShipping(shipping);
        return cartRepository.save(cart);
    }

    // ========== Checkout (preparación) ==========

    public Cart prepareCheckout(Long userId) {
        Cart cart = getCartByUserId(userId);
        
        if (cart.isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }
        
        // Validar stock de todos los items
        for (CartItem item : cart.getItems()) {
            Product product = productService.getProductById(item.getProduct().getId());
            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException(
                    String.format("Stock insuficiente para %s. Disponible: %d, Solicitado: %d",
                                 product.getName(), product.getStock(), item.getQuantity())
                );
            }
        }
        
        return cart;
    }
}