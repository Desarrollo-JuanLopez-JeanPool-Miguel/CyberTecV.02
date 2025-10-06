package com.cybertec.repository;

import com.cybertec.model.Role;
import com.cybertec.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Repositorio de Usuarios (ArrayList en memoria)
 * Ubicación: src/main/java/com/cybertec/repository/UserRepository.java
 * 
 * @author CyberTec Team
 * @version 1.0.0
 */
@Repository
public class UserRepository {

    private final List<User> users = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    // ========== CRUD Operations ==========

    /**
     * Guardar o actualizar usuario
     */
    public User save(User user) {
        if (user.getId() == null) {
            // Crear nuevo
            user.setId(idGenerator.getAndIncrement());
            users.add(user);
        } else {
            // Actualizar existente
            deleteById(user.getId());
            users.add(user);
        }
        return user;
    }

    /**
     * Buscar todos los usuarios
     */
    public List<User> findAll() {
        return new ArrayList<>(users);
    }

    /**
     * Buscar por ID
     */
    public Optional<User> findById(Long id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    /**
     * Buscar por username
     */
    public Optional<User> findByUsername(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    /**
     * Buscar por email
     */
    public Optional<User> findByEmail(String email) {
        return users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    /**
     * Eliminar por ID
     */
    public void deleteById(Long id) {
        users.removeIf(u -> u.getId().equals(id));
    }

    /**
     * Verificar si existe por ID
     */
    public boolean existsById(Long id) {
        return users.stream()
                .anyMatch(u -> u.getId().equals(id));
    }

    /**
     * Verificar si existe por username
     */
    public boolean existsByUsername(String username) {
        return users.stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
    }

    /**
     * Verificar si existe por email
     */
    public boolean existsByEmail(String email) {
        return users.stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }

    /**
     * Contar usuarios
     */
    public long count() {
        return users.size();
    }

    // ========== Búsquedas y Filtros ==========

    /**
     * Buscar por rol
     */
    public List<User> findByRole(Role role) {
        return users.stream()
                .filter(u -> u.getRole().equals(role))
                .collect(Collectors.toList());
    }

    /**
     * Buscar usuarios activos
     */
    public List<User> findByEnabled(boolean enabled) {
        return users.stream()
                .filter(u -> u.isEnabled() == enabled)
                .collect(Collectors.toList());
    }

    /**
     * Buscar administradores
     */
    public List<User> findAdmins() {
        return users.stream()
                .filter(User::isAdmin)
                .collect(Collectors.toList());
    }

    /**
     * Buscar clientes (usuarios normales)
     */
    public List<User> findCustomers() {
        return users.stream()
                .filter(User::isUser)
                .collect(Collectors.toList());
    }

    /**
     * Buscar por nombre (búsqueda parcial en firstName o lastName)
     */
    public List<User> findByNameContaining(String name) {
        String lowerName = name.toLowerCase();
        return users.stream()
                .filter(u -> 
                    u.getFirstName().toLowerCase().contains(lowerName) ||
                    u.getLastName().toLowerCase().contains(lowerName) ||
                    u.getFullName().toLowerCase().contains(lowerName)
                )
                .collect(Collectors.toList());
    }

    /**
     * Búsqueda avanzada
     */
    public List<User> searchUsers(String search, Role role, Boolean enabled) {
        return users.stream()
                .filter(u -> {
                    boolean matchesSearch = search == null || search.isEmpty() ||
                            u.getUsername().toLowerCase().contains(search.toLowerCase()) ||
                            u.getEmail().toLowerCase().contains(search.toLowerCase()) ||
                            u.getFullName().toLowerCase().contains(search.toLowerCase());
                    
                    boolean matchesRole = role == null || u.getRole().equals(role);
                    boolean matchesEnabled = enabled == null || u.isEnabled() == enabled;
                    
                    return matchesSearch && matchesRole && matchesEnabled;
                })
                .collect(Collectors.toList());
    }

    // ========== Estadísticas ==========

    /**
     * Contar por rol
     */
    public long countByRole(Role role) {
        return users.stream()
                .filter(u -> u.getRole().equals(role))
                .count();
    }

    /**
     * Contar usuarios activos
     */
    public long countByEnabled(boolean enabled) {
        return users.stream()
                .filter(u -> u.isEnabled() == enabled)
                .count();
    }

    /**
     * Limpiar todos los usuarios (útil para testing)
     */
    public void clear() {
        users.clear();
        idGenerator.set(1);
    }
}