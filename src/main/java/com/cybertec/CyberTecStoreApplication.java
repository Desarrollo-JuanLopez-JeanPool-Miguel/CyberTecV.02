package com.cybertec;

import com.cybertec.service.ProductService;
import com.cybertec.service.UserService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SecurityScheme(
        name = "bearerAuth",          // nombre que usa Swagger en el popup Authorize
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@OpenAPIDefinition(
        security = {
                @SecurityRequirement(name = "bearerAuth") // aplica bearerAuth por defecto a los endpoints
        }
)
@SpringBootApplication
public class CyberTecStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(CyberTecStoreApplication.class, args);
        System.out.println("\n⚡ CyberTec Backend API iniciado correctamente");
        System.out.println("📍 API: http://localhost:8080");
        System.out.println("📚 Docs: http://localhost:8080/swagger-ui.html");
    }

    @Bean
    public CommandLineRunner initData(UserService userService, ProductService productService) {
        return args -> {
            System.out.println("\n🔄 Inicializando datos de prueba...");
            userService.initializeAdminUser();
            productService.initializeSampleData();
            System.out.println("✅ Datos inicializados correctamente\n");
        };
    }
}
