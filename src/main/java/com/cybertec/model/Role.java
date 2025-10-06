package com.cybertec.model;

/**
 * Enumeración de Roles de Usuario
 * Ubicación: src/main/java/com/cybertec/model/Role.java
 * 
 * Define los roles disponibles en el sistema
 * 
 * @author CyberTec Team
 * @version 1.0.0
 */
public enum Role {
    /**
     * Usuario normal - Cliente de la tienda
     * Permisos: Ver productos, comprar, gestionar carrito
     */
    USER,
    
    /**
     * Administrador - Gestión completa del sistema
     * Permisos: Todo lo del USER + gestión de productos, usuarios, órdenes
     */
    ADMIN
}