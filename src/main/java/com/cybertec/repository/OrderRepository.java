package com.cybertec.repository;

import com.cybertec.model.Order;
import com.cybertec.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA de Órdenes
 * 
 * @author CyberTec Team
 * @version 1.0.0
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // ========== BÚSQUEDAS BÁSICAS ==========

    /**
     * Buscar orden por número de orden
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Verificar si existe una orden por número
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * Buscar órdenes por usuario
     */
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.orderDate DESC")
    List<Order> findByUserId(@Param("userId") Long userId);

    /**
     * Buscar órdenes por estado
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Buscar órdenes por usuario y estado
     */
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status ORDER BY o.orderDate DESC")
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") OrderStatus status);

    // ========== BÚSQUEDAS POR FECHA ==========

    /**
     * Buscar órdenes en un rango de fechas
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    List<Order> findByOrderDateBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Buscar órdenes de un usuario en un rango de fechas
     */
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    List<Order> findByUserIdAndOrderDateBetween(@Param("userId") Long userId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    // ========== ESTADÍSTICAS ==========

    /**
     * Contar órdenes por estado
     */
    long countByStatus(OrderStatus status);

    /**
     * Contar órdenes por usuario
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    /**
     * Buscar órdenes recientes (todas ordenadas por fecha)
     */
    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC")
    List<Order> findRecentOrders();
}