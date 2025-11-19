package com.cybertec.controller;

import com.cybertec.dto.CreateOrderRequest;
import com.cybertec.model.Order;
import com.cybertec.model.OrderStatus;
import com.cybertec.model.User;
import com.cybertec.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador de Órdenes
 * Gestiona las órdenes de compra del sistema
 * 
 * @author CyberTec Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@Tag(name = "Órdenes", description = "Gestión de órdenes de compra")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ========== ENDPOINTS PARA USUARIOS ==========

    /**
     * Crear orden desde el carrito
     * POST /api/orders
     */
    @PostMapping
    @Operation(summary = "Crear orden desde carrito", 
               description = "Crea una nueva orden con los items del carrito del usuario")
    public ResponseEntity<?> createOrderFromCart(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateOrderRequest request) {
        try {
            log.info("Usuario {} creando orden", user.getUsername());
            
            Order order = orderService.createOrderFromCart(
                user.getId(),
                request.getShippingAddress(),
                request.getPaymentMethod()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
            
        } catch (Exception e) {
            log.error("Error al crear orden: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtener órdenes del usuario autenticado
     * GET /api/orders
     */
    @GetMapping
    @Operation(summary = "Ver mis órdenes", 
               description = "Obtiene todas las órdenes del usuario autenticado")
    public ResponseEntity<?> getMyOrders(@AuthenticationPrincipal User user) {
        try {
            log.debug("Usuario {} consultando sus órdenes", user.getUsername());
            
            List<Order> orders = orderService.getOrdersByUserId(user.getId());
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            log.error("Error al obtener órdenes: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtener una orden específica del usuario
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Ver detalle de orden", 
               description = "Obtiene los detalles de una orden específica")
    public ResponseEntity<?> getOrderById(
            @AuthenticationPrincipal User user,
            @PathVariable Long orderId) {
        try {
            log.debug("Usuario {} consultando orden {}", user.getUsername(), orderId);
            
            Order order = orderService.getOrderById(orderId);
            
            // Verificar que la orden pertenezca al usuario (o sea admin)
            if (!order.getUser().getId().equals(user.getId()) && !user.isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "No tienes permiso para ver esta orden"));
            }
            
            return ResponseEntity.ok(order);
            
        } catch (RuntimeException e) {
            log.error("Error al obtener orden: ", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtener órdenes por estado
     * GET /api/orders/status/{status}
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Ver órdenes por estado", 
               description = "Filtra las órdenes del usuario por estado")
    public ResponseEntity<?> getOrdersByStatus(
            @AuthenticationPrincipal User user,
            @PathVariable OrderStatus status) {
        try {
            log.debug("Usuario {} consultando órdenes con estado {}", user.getUsername(), status);
            
            List<Order> orders = orderService.getOrdersByUserIdAndStatus(user.getId(), status);
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            log.error("Error al obtener órdenes por estado: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Cancelar orden
     * DELETE /api/orders/{orderId}
     */
    @DeleteMapping("/{orderId}")
    @Operation(summary = "Cancelar orden", 
               description = "Cancela una orden pendiente o confirmada")
    public ResponseEntity<?> cancelOrder(
            @AuthenticationPrincipal User user,
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            log.info("Usuario {} cancelando orden {}", user.getUsername(), orderId);
            
            Order order = orderService.getOrderById(orderId);
            
            // Verificar que la orden pertenezca al usuario
            if (!order.getUser().getId().equals(user.getId()) && !user.isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "No tienes permiso para cancelar esta orden"));
            }
            
            String reason = request != null ? request.get("reason") : "Cancelado por el usuario";
            Order cancelledOrder = orderService.cancelOrder(orderId, reason);
            
            return ResponseEntity.ok(cancelledOrder);
            
        } catch (Exception e) {
            log.error("Error al cancelar orden: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== ENDPOINTS PARA ADMIN ==========

    /**
     * Obtener todas las órdenes (ADMIN)
     * GET /api/orders/admin/all
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ver todas las órdenes (Admin)", 
               description = "Solo ADMIN puede ver todas las órdenes del sistema")
    public ResponseEntity<?> getAllOrders() {
        try {
            log.debug("Admin consultando todas las órdenes");
            
            List<Order> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            log.error("Error al obtener todas las órdenes: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtener órdenes por usuario (ADMIN)
     * GET /api/orders/admin/user/{userId}
     */
    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ver órdenes de un usuario (Admin)")
    public ResponseEntity<?> getOrdersByUser(@PathVariable Long userId) {
        try {
            log.debug("Admin consultando órdenes del usuario {}", userId);
            
            List<Order> orders = orderService.getOrdersByUserId(userId);
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            log.error("Error al obtener órdenes del usuario: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Actualizar estado de orden (ADMIN)
     * PATCH /api/orders/{orderId}/status
     */
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar estado de orden (Admin)")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {
        try {
            log.info("Admin actualizando estado de orden {}", orderId);
            
            String statusStr = request.get("status");
            if (statusStr == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El campo 'status' es obligatorio"));
            }
            
            OrderStatus newStatus = OrderStatus.valueOf(statusStr.toUpperCase());
            Order order = orderService.updateOrderStatus(orderId, newStatus);
            
            return ResponseEntity.ok(order);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Estado inválido: " + request.get("status")));
        } catch (Exception e) {
            log.error("Error al actualizar estado: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Actualizar información de pago (ADMIN)
     * PATCH /api/orders/{orderId}/payment
     */
    @PatchMapping("/{orderId}/payment")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar información de pago (Admin)")
    public ResponseEntity<?> updatePaymentInfo(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {
        try {
            log.info("Admin actualizando info de pago para orden {}", orderId);
            
            String paymentMethod = request.get("paymentMethod");
            String transactionId = request.get("transactionId");
            
            Order order = orderService.updatePaymentInfo(orderId, paymentMethod, transactionId);
            
            return ResponseEntity.ok(order);
            
        } catch (Exception e) {
            log.error("Error al actualizar info de pago: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Procesar reembolso (ADMIN)
     * POST /api/orders/{orderId}/refund
     */
    @PostMapping("/{orderId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Procesar reembolso (Admin)")
    public ResponseEntity<?> refundOrder(@PathVariable Long orderId) {
        try {
            log.info("Admin procesando reembolso para orden {}", orderId);
            
            Order order = orderService.refundOrder(orderId);
            
            return ResponseEntity.ok(order);
            
        } catch (Exception e) {
            log.error("Error al procesar reembolso: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Eliminar orden (ADMIN)
     * DELETE /api/orders/admin/{orderId}
     */
    @DeleteMapping("/admin/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar orden (Admin)", 
               description = "Solo se pueden eliminar órdenes canceladas")
    public ResponseEntity<?> deleteOrder(@PathVariable Long orderId) {
        try {
            log.warn("Admin eliminando orden {}", orderId);
            
            orderService.deleteOrder(orderId);
            
            return ResponseEntity.ok(Map.of("message", "Orden eliminada exitosamente"));
            
        } catch (Exception e) {
            log.error("Error al eliminar orden: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== ESTADÍSTICAS (ADMIN) ==========

    /**
     * Obtener estadísticas de órdenes (ADMIN)
     * GET /api/orders/admin/stats
     */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estadísticas de órdenes (Admin)")
    public ResponseEntity<?> getOrderStats() {
        try {
            log.debug("Admin consultando estadísticas de órdenes");
            
            Map<String, Object> stats = new HashMap<>();
            
            stats.put("totalOrders", orderService.getAllOrders().size());
            stats.put("pendingOrders", orderService.countOrdersByStatus(OrderStatus.PENDING));
            stats.put("confirmedOrders", orderService.countOrdersByStatus(OrderStatus.CONFIRMED));
            stats.put("processingOrders", orderService.countOrdersByStatus(OrderStatus.PROCESSING));
            stats.put("shippedOrders", orderService.countOrdersByStatus(OrderStatus.SHIPPED));
            stats.put("deliveredOrders", orderService.countOrdersByStatus(OrderStatus.DELIVERED));
            stats.put("cancelledOrders", orderService.countOrdersByStatus(OrderStatus.CANCELLED));
            stats.put("totalSales", orderService.calculateTotalSales());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error al obtener estadísticas: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtener ventas por rango de fechas (ADMIN)
     * GET /api/orders/admin/sales
     */
    @GetMapping("/admin/sales")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ventas por rango de fechas (Admin)")
    public ResponseEntity<?> getSalesByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            log.debug("Admin consultando ventas desde {} hasta {}", startDate, endDate);
            
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            
            List<Order> orders = orderService.getOrdersByDateRange(start, end);
            var totalSales = orderService.calculateSalesByDateRange(start, end);
            
            Map<String, Object> result = new HashMap<>();
            result.put("orders", orders);
            result.put("totalOrders", orders.size());
            result.put("totalSales", totalSales);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error al obtener ventas por rango: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}