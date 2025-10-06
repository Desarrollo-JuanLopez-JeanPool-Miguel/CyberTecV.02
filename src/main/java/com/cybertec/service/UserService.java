package com.cybertec.service;

import com.cybertec.model.Role;
import com.cybertec.model.User;
import com.cybertec.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de Usuarios
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ========== CRUD ==========

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));
    }

    public User createUser(User user) {
        log.info("Creando usuario: {}", user.getUsername());
        
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("El username ya está en uso: " + user.getUsername());
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("El email ya está en uso: " + user.getEmail());
        }
        
        // Encriptar contraseña
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        log.info("Actualizando usuario ID: {}", id);
        
        User user = getUserById(id);
        
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setEmail(userDetails.getEmail());
        user.setPhone(userDetails.getPhone());
        user.setAddress(userDetails.getAddress());
        
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        log.info("Eliminando usuario ID: {}", id);
        userRepository.deleteById(id);
    }

    // ========== Autenticación ==========

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public User changePassword(Long id, String newPassword) {
        User user = getUserById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    // ========== Búsquedas ==========

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public List<User> getAdmins() {
        return userRepository.findAdmins();
    }

    public List<User> getCustomers() {
        return userRepository.findCustomers();
    }

    public List<User> searchUsers(String search, Role role, Boolean enabled) {
        return userRepository.searchUsers(search, role, enabled);
    }

    // ========== Inicialización de usuario admin ==========

    public void initializeAdminUser() {
        if (userRepository.existsByUsername("admin")) {
            return; // Ya existe
        }

        log.info("Creando usuario admin por defecto...");

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@cybertec.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFirstName("Admin");
        admin.setLastName("CyberTec");
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        
        userRepository.save(admin);
        log.info("Usuario admin creado: username=admin, password=admin123");
    }
}