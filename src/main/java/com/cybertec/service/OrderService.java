package com.cybertec.service;

import com.cybertec.model.*;
import com.cybertec.repository.OrderRepository;
import com.cybertec.repository.ProductRepository;
import com.cybertec.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de Órdenes de Compra
 * Gestiona la creación, actualización y consulta de órdenes
 * 
 * @author CyberTec Team
 * @version 1.0.0
 */
@Service
@Transactional
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository, 
                       UserRepository userRepository,
                       ProductRepository productRepository,
                       CartService cartService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    // ========== CRUD BÁSICO ==========

    /**
     * Obtener todas las órdenes
     */
    public List<Order> getAllOrders() {
        log.debug("Obteniendo todas las órdenes");
        return orderRepository.findAll();
    }

    /**
     * Obtener orden por ID
     */
    public Order getOrderById(Long id) {
        log.debug("Obteniendo orden ID: {}", id);
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + id));
    }

    /**
     * Obtener orden por número de orden
     */
    public Order getOrderByOrderNumber(String orderNumber) {
        log.debug("Obteniendo orden con número: {}", orderNumber);
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con número: " + orderNumber));
    }

    /**
     * Obtener órdenes de un usuario
     */
    public List<Order> getOrdersByUserId(Long userId) {
        log.debug("Obteniendo órdenes del usuario ID: {}", userId);
        return orderRepository.findByUserId(userId);
    }

    /**
     * Obtener órdenes por estado
     */
    public List<Order> getOrdersByStatus(OrderStatus status) {
        log.debug("Obteniendo órdenes con estado: {}", status);
        return orderRepository.findByStatus(status);
    }

    /**
     * Obtener órdenes de un usuario por estado
     */
    public List<Order> getOrdersByUserIdAndStatus(Long userId, OrderStatus status) {
        log.debug("Obteniendo órdenes del usuario {} con estado {}", userId, status);
        return orderRepository.findByUserIdAndStatus(userId, status);
    }

    // ========== CREACIÓN DE ÓRDENES ==========

    /**
     * Crear orden desde el carrito del usuario
     */
    public Order createOrderFromCart(Long userId, ShippingAddress shippingAddress, String paymentMethod) {
        log.info("Creando orden para usuario ID: {}", userId);
        
        // 1. Obtener usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // 2. Obtener carrito
        Cart cart = cartService.getCartByUserId(userId);
        
        // 3. Validar que el carrito no esté vacío
        if (cart.isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }
        
        // 4. Validar stock de todos los productos
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException(
                    String.format("Stock insuficiente para %s. Disponible: %d, Solicitado: %d",
                                 product.getName(), product.getStock(), cartItem.getQuantity())
                );
            }
        }
        
        // 5. Crear orden
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        
        // 6. Agregar items de orden desde el carrito
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getUnitPrice());
            
            order.addItem(orderItem);
        }
        
        // 7. Calcular total
        order.calculateTotalAmount();
        
        // 8. Guardar orden
        Order savedOrder = orderRepository.save(order);
        
        // 9. Reducir stock de productos
        for (OrderItem item : savedOrder.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }
        
        // 10. Limpiar carrito
        cartService.clearCart(userId);
        
        log.info("Orden {} creada exitosamente para usuario {}", savedOrder.getOrderNumber(), userId);
        return savedOrder;
    }

    /**
     * Crear orden manualmente (para admin)
     */
    public Order createOrder(Order order) {
        log.info("Creando orden manual");
        
        // Validar usuario
        if (order.getUser() == null || order.getUser().getId() == null) {
            throw new RuntimeException("Debe especificar un usuario para la orden");
        }
        
        User user = userRepository.findById(order.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        order.setUser(user);
        
        // Validar stock
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProduct().getId()));
            
            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException(
                    String.format("Stock insuficiente para %s", product.getName())
                );
            }
            
            item.setProduct(product);
            item.setOrder(order);
        }
        
        // Calcular total
        order.calculateTotalAmount();
        
        // Guardar orden
        Order savedOrder = orderRepository.save(order);
        
        // Reducir stock
        for (OrderItem item : savedOrder.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }
        
        log.info("Orden {} creada exitosamente", savedOrder.getOrderNumber());
        return savedOrder;
    }

    // ========== ACTUALIZACIÓN DE ÓRDENES ==========

    /**
     * Actualizar estado de orden
     */
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.info("Actualizando estado de orden {} a {}", orderId, newStatus);
        
        Order order = getOrderById(orderId);
        OrderStatus oldStatus = order.getStatus();
        
        // Validar transición de estado
        validateStatusTransition(oldStatus, newStatus);
        
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        
        log.info("Estado de orden {} actualizado de {} a {}", 
                 orderId, oldStatus, newStatus);
        
        return updatedOrder;
    }

    /**
     * Actualizar información de pago
     */
    public Order updatePaymentInfo(Long orderId, String paymentMethod, String transactionId) {
        log.info("Actualizando información de pago para orden {}", orderId);
        
        Order order = getOrderById(orderId);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentTransactionId(transactionId);
        
        return orderRepository.save(order);
    }

    /**
     * Actualizar orden completa
     */
    public Order updateOrder(Long id, Order orderDetails) {
        log.info("Actualizando orden ID: {}", id);
        
        Order order = getOrderById(id);
        
        // Solo actualizar campos permitidos
        if (orderDetails.getShippingAddress() != null) {
            order.setShippingAddress(orderDetails.getShippingAddress());
        }
        
        if (orderDetails.getPaymentMethod() != null) {
            order.setPaymentMethod(orderDetails.getPaymentMethod());
        }
        
        if (orderDetails.getNotes() != null) {
            order.setNotes(orderDetails.getNotes());
        }
        
        order.setUpdatedAt(LocalDateTime.now());
        
        return orderRepository.save(order);
    }

    // ========== CANCELACIÓN Y REEMBOLSO ==========

    /**
     * Cancelar orden
     */
    public Order cancelOrder(Long orderId, String reason) {
        log.info("Cancelando orden {}, razón: {}", orderId, reason);
        
        Order order = getOrderById(orderId);
        
        // Solo se pueden cancelar órdenes pendientes o confirmadas
        if (order.getStatus() != OrderStatus.PENDING && 
            order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException(
                "No se puede cancelar una orden con estado: " + order.getStatus()
            );
        }
        
        // Restaurar stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        if (reason != null) {
            order.setNotes(order.getNotes() != null ? 
                          order.getNotes() + "\nCancelación: " + reason : 
                          "Cancelación: " + reason);
        }
        
        Order cancelledOrder = orderRepository.save(order);
        
        log.info("Orden {} cancelada exitosamente", orderId);
        return cancelledOrder;
    }

    /**
     * Procesar reembolso
     */
    public Order refundOrder(Long orderId) {
        log.info("Procesando reembolso para orden {}", orderId);
        
        Order order = getOrderById(orderId);
        
        // Validar que la orden pueda ser reembolsada
        if (order.getStatus() != OrderStatus.DELIVERED && 
            order.getStatus() != OrderStatus.CANCELLED) {
            throw new RuntimeException(
                "Solo se pueden reembolsar órdenes entregadas o canceladas"
            );
        }
        
        order.setStatus(OrderStatus.REFUNDED);
        
        return orderRepository.save(order);
    }

    // ========== ELIMINACIÓN ==========

    /**
     * Eliminar orden (solo admin, con precaución)
     */
    public void deleteOrder(Long id) {
        log.warn("Eliminando orden ID: {}", id);
        
        Order order = getOrderById(id);
        
        // Solo permitir eliminar órdenes canceladas
        if (order.getStatus() != OrderStatus.CANCELLED) {
            throw new RuntimeException(
                "Solo se pueden eliminar órdenes canceladas"
            );
        }
        
        orderRepository.deleteById(id);
    }

    // ========== BÚSQUEDAS AVANZADAS ==========

    /**
     * Buscar órdenes por rango de fechas
     */
    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Buscando órdenes entre {} y {}", startDate, endDate);
        return orderRepository.findByOrderDateBetween(startDate, endDate);
    }

    /**
     * Buscar órdenes por usuario y rango de fechas
     */
    public List<Order> getOrdersByUserIdAndDateRange(Long userId, 
                                                     LocalDateTime startDate, 
                                                     LocalDateTime endDate) {
        log.debug("Buscando órdenes de usuario {} entre {} y {}", userId, startDate, endDate);
        return orderRepository.findByUserIdAndOrderDateBetween(userId, startDate, endDate);
    }

    /**
     * Obtener órdenes recientes de un usuario
     * CORREGIDO: Ahora usa limit() en el stream
     */
    public List<Order> getRecentOrdersByUserId(Long userId, int limit) {
        log.debug("Obteniendo las {} órdenes más recientes del usuario {}", limit, userId);
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .limit(limit)
                .toList();
    }

    // ========== ESTADÍSTICAS ==========

    /**
     * Contar órdenes por estado
     */
    public long countOrdersByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    /**
     * Contar órdenes de un usuario
     */
    public long countOrdersByUserId(Long userId) {
        return orderRepository.countByUserId(userId);
    }

    /**
     * Calcular total de ventas
     */
    public BigDecimal calculateTotalSales() {
        List<Order> orders = orderRepository.findByStatus(OrderStatus.DELIVERED);
        return orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcular ventas en un rango de fechas
     */
    public BigDecimal calculateSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        return orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ========== VALIDACIONES ==========

    /**
     * Validar transición de estado
     */
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Reglas de transición de estado
        switch (currentStatus) {
            case PENDING:
                if (newStatus != OrderStatus.CONFIRMED && 
                    newStatus != OrderStatus.CANCELLED) {
                    throw new RuntimeException(
                        "Estado PENDING solo puede cambiar a CONFIRMED o CANCELLED"
                    );
                }
                break;
                
            case CONFIRMED:
                if (newStatus != OrderStatus.PROCESSING && 
                    newStatus != OrderStatus.CANCELLED) {
                    throw new RuntimeException(
                        "Estado CONFIRMED solo puede cambiar a PROCESSING o CANCELLED"
                    );
                }
                break;
                
            case PROCESSING:
                if (newStatus != OrderStatus.SHIPPED && 
                    newStatus != OrderStatus.CANCELLED) {
                    throw new RuntimeException(
                        "Estado PROCESSING solo puede cambiar a SHIPPED o CANCELLED"
                    );
                }
                break;
                
            case SHIPPED:
                if (newStatus != OrderStatus.DELIVERED) {
                    throw new RuntimeException(
                        "Estado SHIPPED solo puede cambiar a DELIVERED"
                    );
                }
                break;
                
            case DELIVERED:
                if (newStatus != OrderStatus.REFUNDED) {
                    throw new RuntimeException(
                        "Estado DELIVERED solo puede cambiar a REFUNDED"
                    );
                }
                break;
                
            case CANCELLED:
            case REFUNDED:
                throw new RuntimeException(
                    "No se puede cambiar el estado de una orden " + currentStatus
                );
                
            default:
                throw new RuntimeException("Estado desconocido: " + currentStatus);
        }
    }
}