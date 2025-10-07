package com.cybertec.ai;

import com.cybertec.ai.dto.ChatRequest;
import com.cybertec.ai.dto.ChatResponse;
import com.cybertec.model.Product;
import com.cybertec.repository.ProductRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Servicio de IA con Gemini para asesoramiento de componentes
 * Ubicación: src/main/java/com/cybertec/ai/GeminiService.java
 */
@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    private final GeminiConfig config;
    private final ProductRepository productRepository;
    private final OkHttpClient httpClient;

    public GeminiService(GeminiConfig config, ProductRepository productRepository) {
        this.config = config;
        this.productRepository = productRepository;
        
        // Configurar cliente HTTP
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Procesar mensaje del usuario con contexto de productos
     */
    public ChatResponse processMessage(ChatRequest request) {
        try {
            // Validar API Key
            if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
                return ChatResponse.error("API Key de Gemini no configurada");
            }

            // Construir contexto con productos disponibles
            String productContext = buildProductContext();
            
            // Crear prompt mejorado
            String enhancedPrompt = buildEnhancedPrompt(request.getMessage(), productContext);
            
            // Llamar a Gemini API
            String response = callGeminiAPI(enhancedPrompt);
            
            return ChatResponse.success(response);
            
        } catch (Exception e) {
            log.error("Error al procesar mensaje con Gemini: ", e);
            return ChatResponse.error("Error al procesar tu consulta: " + e.getMessage());
        }
    }

    /**
     * Construir contexto de productos disponibles
     */
    private String buildProductContext() {
        List<Product> products = productRepository.findAll();
        
        StringBuilder context = new StringBuilder();
        context.append("Productos disponibles en la tienda:\n\n");
        
        for (Product product : products) {
            context.append(String.format(
                "- %s (%s)\n" +
                "  Categoría: %s\n" +
                "  Precio: $%,.0f\n" +
                "  Stock: %d unidades\n" +
                "  Especificaciones: %s\n\n",
                product.getName(),
                product.getSku(),
                product.getCategory().getDisplayName(),
                product.getPrice(),
                product.getStock(),
                product.getSpecifications() != null ? product.getSpecifications() : "N/A"
            ));
        }
        
        return context.toString();
    }

    /**
     * Construir prompt mejorado con instrucciones específicas
     */
    private String buildEnhancedPrompt(String userMessage, String productContext) {
        return String.format(
            "Eres un asistente experto en componentes de PC gaming para CyberTec Store.\n\n" +
            "Tu objetivo es:\n" +
            "1. Ayudar a los usuarios a elegir los mejores componentes\n" +
            "2. Comparar productos disponibles en nuestra tienda\n" +
            "3. Recomendar configuraciones según presupuesto y necesidades\n" +
            "4. Explicar especificaciones técnicas de forma clara\n" +
            "5. Alertar sobre compatibilidad entre componentes\n\n" +
            "IMPORTANTE: Solo recomienda productos que estén en el inventario.\n\n" +
            "%s\n\n" +
            "Pregunta del usuario: %s\n\n" +
            "Responde de forma clara, técnica pero accesible, y siempre menciona productos específicos de nuestro inventario.",
            productContext,
            userMessage
        );
    }

    /**
     * Llamar a Gemini API
     */
    private String callGeminiAPI(String prompt) throws IOException {
        // Construir JSON request
        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        
        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);

        // Configuración de generación
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.7);
        generationConfig.addProperty("topK", 40);
        generationConfig.addProperty("topP", 0.95);
        generationConfig.addProperty("maxOutputTokens", 2048);
        requestBody.add("generationConfig", generationConfig);

        // Crear request
        String url = config.getApiUrl() + "?key=" + config.getApiKey();
        RequestBody body = RequestBody.create(
            requestBody.toString(),
            MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        // Ejecutar request
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error en Gemini API: " + response.code());
            }

            String responseBody = response.body().string();
            return parseGeminiResponse(responseBody);
        }
    }

    /**
     * Parsear respuesta de Gemini
     */
    private String parseGeminiResponse(String responseBody) {
        try {
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
            
            if (candidates != null && candidates.size() > 0) {
                JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                JsonObject content = firstCandidate.getAsJsonObject("content");
                JsonArray parts = content.getAsJsonArray("parts");
                
                if (parts != null && parts.size() > 0) {
                    return parts.get(0).getAsJsonObject().get("text").getAsString();
                }
            }
            
            return "No se pudo generar una respuesta.";
            
        } catch (Exception e) {
            log.error("Error al parsear respuesta de Gemini: ", e);
            return "Error al procesar la respuesta de la IA.";
        }
    }

    /**
     * Comparar productos específicos
     */
    public ChatResponse compareProducts(String sku1, String sku2) {
        try {
            Product product1 = productRepository.findBySku(sku1)
                    .orElseThrow(() -> new RuntimeException("Producto 1 no encontrado"));
            Product product2 = productRepository.findBySku(sku2)
                    .orElseThrow(() -> new RuntimeException("Producto 2 no encontrado"));

            String comparisonPrompt = String.format(
                "Compara estos dos productos de forma detallada:\n\n" +
                "Producto 1: %s\n" +
                "Precio: $%,.0f\n" +
                "Especificaciones: %s\n\n" +
                "Producto 2: %s\n" +
                "Precio: $%,.0f\n" +
                "Especificaciones: %s\n\n" +
                "Proporciona una comparación técnica y recomienda cuál es mejor según diferentes usos (gaming, streaming, productividad).",
                product1.getName(), product1.getPrice(), product1.getSpecifications(),
                product2.getName(), product2.getPrice(), product2.getSpecifications()
            );

            String response = callGeminiAPI(comparisonPrompt);
            return ChatResponse.success(response);

        } catch (Exception e) {
            log.error("Error al comparar productos: ", e);
            return ChatResponse.error("Error al comparar productos: " + e.getMessage());
        }
    }
}