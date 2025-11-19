package com.cybertec.model;

public enum OrderStatus {
    PENDING("pending", "Pendiente", "⏳"),
    CONFIRMED("confirmed", "Confirmada", "✅"),
    PROCESSING("processing", "En Proceso", "📦"),
    SHIPPED("shipped", "Enviada", "🚚"),
    DELIVERED("delivered", "Entregada", "✅"),
    CANCELLED("cancelled", "Cancelada", "❌"),
    REFUNDED("refunded", "Reembolsada", "💰");

    private final String code;
    private final String displayName;
    private final String icon;

    OrderStatus(String code, String displayName, String icon) {
        this.code = code;
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }
    public String getIcon() { return icon; }

    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Estado de orden desconocido: " + code);
    }
}