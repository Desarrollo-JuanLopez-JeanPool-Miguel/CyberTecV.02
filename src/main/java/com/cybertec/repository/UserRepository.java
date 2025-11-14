package com.cybertec.repository;

import com.cybertec.model.Role;
import com.cybertec.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



import java.util.List;
import java.util.Optional;


/**
 * Repositorio JPA de Usuarios
 * Spring Data JPA genera automáticamente la implementación
 * 
 * @author CyberTec Team
 * @version 2.0.0 - Database Version
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ========== BÚSQUEDAS BÁSICAS ==========
    // Spring Data JPA genera estos métodos automáticamente por el nombre

    /**
     * Buscar por username
     */
    Optional<User> findByUsername(String username);

    /**
     * Buscar por email
     */
    Optional<User> findByEmail(String email);

    /**
     * Verificar si existe por username
     */
    boolean existsByUsername(String username);

    /**
     * Verificar si existe por email
     */
    boolean existsByEmail(String email);

    // ========== BÚSQUEDAS POR ROL ==========

    /**
     * Buscar usuarios por rol
     */
    List<User> findByRole(Role role);

    /**
     * Buscar usuarios por estado (habilitado/deshabilitado)
     */
    List<User> findByEnabled(boolean enabled);

    /**
     * Buscar usuarios por rol y estado
     */
    List<User> findByRoleAndEnabled(Role role, boolean enabled);

    // ========== BÚSQUEDAS AVANZADAS ==========

    /**
     * Buscar usuarios por nombre (firstName o lastName)
     * Búsqueda case-insensitive
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByNameContaining(@Param("name") String name);

    /**
     * Búsqueda avanzada con múltiples filtros
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:search IS NULL OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:enabled IS NULL OR u.enabled = :enabled)")
    List<User> searchUsers(@Param("search") String search,
                          @Param("role") Role role,
                          @Param("enabled") Boolean enabled);

    // ========== ESTADÍSTICAS ==========

    /**
     * Contar usuarios por rol
     */
    long countByRole(Role role);

    /**
     * Contar usuarios por estado
     */
    long countByEnabled(boolean enabled);

    /**
     * Buscar administradores
     */
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN'")
    List<User> findAdmins();

    /**
     * Buscar clientes (usuarios normales)
     */
    @Query("SELECT u FROM User u WHERE u.role = 'USER'")
    List<User> findCustomers();
}