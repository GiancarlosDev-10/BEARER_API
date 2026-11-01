package pe.techstore.api.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Datos del usuario dueño del token.
 *
 *   GET /api/usuarios/me   (Authorization: Bearer <token>)
 *   -> 200 { usuario, rol, emitidoEn, expiraEn, segundosRestantes }
 *
 * Sirve para comprobar "¿quién soy según mi token?" y cuánto le queda.
 */
@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "3. Usuarios (Bearer)", description = "Requiere Bearer Token de cualquier rol")
public class UsuarioController {

    // Spring nos pasa el objeto Authentication que creó JwtAuthenticationFilter
    @GetMapping("/me")
    @Operation(summary = "Ver mis datos según el token", security = @SecurityRequirement(name = "bearerAuth"))
    public Map<String, Object> me(Authentication autenticacion) {
        // En el filtro guardamos los datos (claims) del token como "details"
        Claims datos = (Claims) autenticacion.getDetails();

        Instant expira = datos.getExpiration().toInstant();
        long segundosRestantes = Math.max(0, Duration.between(Instant.now(), expira).getSeconds());

        return Map.of(
                "usuario", datos.getSubject(),
                "rol", datos.get("rol", String.class),
                "emitidoEn", datos.getIssuedAt().toInstant().toString(),
                "expiraEn", expira.toString(),
                "segundosRestantes", segundosRestantes
        );
    }
}
