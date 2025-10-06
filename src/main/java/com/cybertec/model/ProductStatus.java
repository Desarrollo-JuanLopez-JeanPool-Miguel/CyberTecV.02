package com.cybertec.model;

/**
 * Enumeración de Estados de Producto
 * Ubicación: src/main/java/com/cybertec/model/ProductStatus.java
 * 
 * @author CyberTec Team
 * @version 1.0.0
 */
public enum ProductStatus {
    /**
     * Producto activo - Visible y disponible para compra
     */
    ACTIVE("active", "Activo", "✅"),
    
    /**
     * Producto inactivo - No visible en tienda
     */
    INACTIVE("inactive", "Inactivo", "❌"),
    
    /**
     * Borrador - Producto en proceso de creación
     */
    DRAFT("draft", "Borrador", "📝");

    private final String code;
    private final String displayName;
    private final String icon;

    ProductStatus(String code, String displayName, String icon) {
        this.code = code;
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getCode() { 
        return code; 
    }
    
    public String getDisplayName() { 
        return displayName; 
    }
    
    public String getIcon() { 
        return icon; 
    }
    
    /**
     * Obtiene el estado desde un código string
     * @param code código del estado (active, inactive, draft)
     * @return ProductStatus correspondiente
     * @throws IllegalArgumentException si el código no existe
     */
    public static ProductStatus fromCode(String code) {
        for (ProductStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Estado desconocido: " + code);
    }
}