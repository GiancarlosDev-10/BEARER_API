package pe.techstore.api.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Configuración de Swagger UI (http://localhost:8080/swagger-ui.html).
 *
 * Declara los DOS tipos de credenciales de la API, para que el botón "Authorize"
 * de Swagger permita escribirlas y probar los endpoints protegidos:
 *
 *  - bearerAuth: el token JWT que devuelve /auth/login.
 *                Swagger lo envía como  "Authorization: Bearer <token>".
 *  - apiKey:     la clave del sistema proveedor.
 *                Swagger la envía como  "X-API-KEY: <clave>".
 *
 * Cada endpoint indica cuál necesita con @SecurityRequirement (ver los controladores).
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "TechStore API",
        version = "1.0.0",
        description = "Taller API Keys vs. Bearer Tokens. "
                + "Usuarios de prueba: ana / Admin123! (ADMIN) y carlos / Cliente123! (CLIENTE)."))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token obtenido en POST /auth/login (pega solo el token, sin la palabra Bearer).")
@SecurityScheme(
        name = "apiKey",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-API-KEY",
        description = "API Key del sistema proveedor (solo para /api/proveedor/**).")
public class OpenApiConfig {
}
