package com.cybertec.repository;

import com.cybertec.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



import java.util.Optional;



/**
 * Repositorio JPA de Carritos
 * Ya no usa ArrayList, usa base de datos
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Buscar carrito por ID de usuario
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.user.id = :userId")
    Optional<Cart> findByUserId(@Param("userId") Long userId);

    /**
     * Verificar si existe carrito por ID de usuario
     */
    @Query("SELECT COUNT(c) > 0 FROM Cart c WHERE c.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);

    /**
     * Eliminar carrito por ID de usuario
     */
    void deleteByUserId(Long userId);
}