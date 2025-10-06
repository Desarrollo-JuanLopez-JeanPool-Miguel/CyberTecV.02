package com.cybertec.controller;

import com.cybertec.dto.AuthResponse;
import com.cybertec.dto.LoginRequest;
import com.cybertec.dto.RegisterRequest;
import com.cybertec.model.Role;
import com.cybertec.model.User;
import com.cybertec.security.JwtUtil;
import com.cybertec.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller de Autenticación
 */
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"})
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * POST /api/auth/register
     * Registrar nuevo usuario
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            log.info("Registrando nuevo usuario: {}", request.getUsername());

            // Crear usuario
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setPhone(request.getPhone());
            user.setAddress(request.getAddress());
            user.setRole(Role.USER); // Por defecto USER
            user.setEnabled(true);

            User savedUser = userService.createUser(user);

            // Generar token
            String token = jwtUtil.generateToken(savedUser.getUsername(), savedUser.getRole().name());

            AuthResponse response = new AuthResponse(
                    token,
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getRole().name()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            log.error("Error al registrar usuario: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * POST /api/auth/login
     * Iniciar sesión
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Intento de login: {}", request.getUsername());

            // Buscar usuario
            User user = userService.getUserByUsername(request.getUsername());

            // Verificar contraseña
            if (!userService.checkPassword(request.getPassword(), user.getPassword())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Credenciales inválidas");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // Verificar si está habilitado
            if (!user.isEnabled()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Usuario deshabilitado");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Generar token
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

            AuthResponse response = new AuthResponse(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole().name()
            );

            log.info("Login exitoso para usuario: {}", user.getUsername());
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al hacer login: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Credenciales inválidas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    /**
     * GET /api/auth/validate
     * Validar token
     */
    @GetMapping("/validate")
public ResponseEntity<?> validate(Authentication auth) {
    if (auth == null || !auth.isAuthenticated()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("valid", false));
    }

    var authorities = auth.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

    return ResponseEntity.ok(Map.of(
            "valid", true,
            "username", auth.getName(),
            "authorities", authorities
    ));
}
}

