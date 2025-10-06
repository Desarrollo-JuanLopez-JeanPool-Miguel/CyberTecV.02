package com.cybertec.ai.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request del Chat con IA
 */
public class ChatRequest {

    @NotBlank(message = "El mensaje es obligatorio")
    private String message;

    private String context; // Opcional: contexto de productos

    public ChatRequest() {}

    public ChatRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}