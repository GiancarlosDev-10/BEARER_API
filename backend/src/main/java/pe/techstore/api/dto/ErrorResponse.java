package pe.techstore.api.dto;

import java.time.OffsetDateTime;

/**
 * Formato ÚNICO para todas las respuestas de error de la API.
 * Ejemplo:
 * {
 *   "status": 404,
 *   "error": "Not Found",
 *   "mensaje": "No existe el producto con id 99",
 *   "ruta": "/api/productos/99",
 *   "timestamp": "2026-11-01T13:30:00-05:00"
 * }
 * Así el frontend (y Postman) siempre saben dónde leer el mensaje.
 */
public record ErrorResponse(
        int status,
        String error,
        String mensaje,
        String ruta,
        String timestamp
) {
    // Atajo para crear el error con la hora actual
    public static ErrorResponse de(int status, String error, String mensaje, String ruta) {
        return new ErrorResponse(status, error, mensaje, ruta, OffsetDateTime.now().toString());
    }
}
