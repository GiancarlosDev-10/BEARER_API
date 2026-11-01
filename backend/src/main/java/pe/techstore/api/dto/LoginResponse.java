package pe.techstore.api.dto;

/**
 * Respuesta de un login exitoso.
 * Ejemplo:
 * {
 *   "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
 *   "tokenType": "Bearer",
 *   "expiraEnSegundos": 900,
 *   "usuario": "ana",
 *   "rol": "ADMIN"
 * }
 * El cliente debe enviar luego:  Authorization: Bearer <accessToken>
 */
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiraEnSegundos,
        String usuario,
        String rol
) {
}
