package com.cybertec.controller;

import com.cybertec.model.Role;
import com.cybertec.model.User;
import com.cybertec.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller de Usuarios
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"})
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/users/me
     * Obtener información del usuario actual
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        log.info("GET /api/users/me");
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        
        // No enviar password
        user.setPassword(null);
        
        return ResponseEntity.ok(user);
    }

    /**
     * GET /api/users
     * Obtener todos los usuarios (Solo ADMIN)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("GET /api/users");
        List<User> users = userService.getAllUsers();
        
        // Remover passwords
        users.forEach(u -> u.setPassword(null));
        
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/{id}
     * Obtener usuario por ID (Solo ADMIN)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("GET /api/users/{}", id);
        User user = userService.getUserById(id);
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    /**
     * PUT /api/users/{id}
     * Actualizar usuario (Solo ADMIN o el mismo usuario)
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody User user,
            Authentication authentication) {
        log.info("PUT /api/users/{}", id);
        
        // Verificar que sea admin o el mismo usuario
        String currentUsername = authentication.getName();
        User currentUser = userService.getUserByUsername(currentUsername);
        
        if (!currentUser.isAdmin() && !currentUser.getId().equals(id)) {
            return ResponseEntity.status(403).build();
        }
        
        User updated = userService.updateUser(id, user);
        updated.setPassword(null);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/users/{id}
     * Eliminar usuario (Solo ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "Usuario eliminado exitosamente"));
    }

    /**
     * GET /api/users/role/{role}
     * Obtener usuarios por rol (Solo ADMIN)
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String role) {
        log.info("GET /api/users/role/{}", role);
        Role userRole = Role.valueOf(role.toUpperCase());
        List<User> users = userService.getUsersByRole(userRole);
        users.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/search
     * Búsqueda de usuarios (Solo ADMIN)
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> searchUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean enabled) {
        log.info("GET /api/users/search - query: {}, role: {}, enabled: {}", query, role, enabled);
        
        Role userRole = role != null ? Role.valueOf(role.toUpperCase()) : null;
        List<User> users = userService.searchUsers(query, userRole, enabled);
        users.forEach(u -> u.setPassword(null));
        
        return ResponseEntity.ok(users);
    }

    /**
     * PATCH /api/users/{id}/password
     * Cambiar contraseña
     */
    @PatchMapping("/{id}/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        log.info("PATCH /api/users/{}/password", id);
        
        // Verificar que sea el mismo usuario
        String currentUsername = authentication.getName();
        User currentUser = userService.getUserByUsername(currentUsername);
        
        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }
        
        String newPassword = request.get("newPassword");
        userService.changePassword(id, newPassword);
        
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente"));
    }
}