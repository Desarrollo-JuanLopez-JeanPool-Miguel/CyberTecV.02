package com.cybertec.ai.dto;

/**
 * Response del Chat con IA
 */
public class ChatResponse {

    private String response;
    private boolean success;
    private String error;

    public ChatResponse() {}

    public ChatResponse(String response, boolean success) {
        this.response = response;
        this.success = success;
    }

    public static ChatResponse success(String response) {
        return new ChatResponse(response, true);
    }

    public static ChatResponse error(String error) {
        ChatResponse response = new ChatResponse();
        response.setSuccess(false);
        response.setError(error);
        return response;
    }

    // Getters y Setters
    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}