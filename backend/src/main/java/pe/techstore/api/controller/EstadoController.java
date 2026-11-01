package pe.techstore.api.controller;

import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoint público para saber si el servidor está vivo.
 *
 * Lo usan:
 *  - El frontend, para "despertar" el servidor de Render (el plan gratuito
 *    se duerme tras unos minutos sin tráfico).
 *  - El ping de cron-job.org, que lo llama cada 10 minutos para que no se duerma.
 *
 * No requiere ninguna credencial.
 */
@RestController                      // Esta clase responde peticiones HTTP y devuelve JSON
@RequestMapping("/api/public")       // Prefijo común de las rutas de esta clase
@Tag(name = "1. Público", description = "Sin credenciales")  // Agrupa los endpoints en Swagger
public class EstadoController {

    // GET /api/public/estado  ->  { "estado": "ok", "hora": "2026-11-01T13:30:00-05:00" }
    @GetMapping("/estado")
    public Map<String, Object> estado() {
        // Map.of crea un objeto clave-valor que Spring convierte a JSON automáticamente
        return Map.of(
                "estado", "ok",
                "hora", OffsetDateTime.now().toString()
        );
    }
}
