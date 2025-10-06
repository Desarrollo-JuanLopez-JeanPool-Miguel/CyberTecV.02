package com.cybertec.model;

/**
 * Enumeración de Categorías de Productos
 * Ubicación: src/main/java/com/cybertec/model/ProductCategory.java
 * 
 * @author CyberTec Team
 * @version 1.0.0
 */
public enum ProductCategory {
    GPU("gpu", "Tarjetas Gráficas", "🖥️"),
    CPU("cpu", "Procesadores", "⚡"),
    RAM("ram", "Memorias RAM", "🧠"),
    STORAGE("storage", "Almacenamiento", "💾"),
    MOTHERBOARD("motherboard", "Placas Madre", "🔌"),
    PSU("psu", "Fuentes de Poder", "🔋"),
    COOLING("cooling", "Refrigeración", "❄️"),
    CASE("case", "Gabinetes", "📦"),
    MONITOR("monitor", "Monitores", "🖥️"),
    PERIPHERALS("peripherals", "Periféricos", "🎧");

    private final String code;
    private final String displayName;
    private final String icon;

    ProductCategory(String code, String displayName, String icon) {
        this.code = code;
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }
    public String getIcon() { return icon; }
    
    public static ProductCategory fromCode(String code) {
        for (ProductCategory category : values()) {
            if (category.code.equalsIgnoreCase(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Categoría desconocida: " + code);
    }
}