package pe.techstore.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Datos que envía el cliente para iniciar sesión.
 * Ejemplo: { "usuario": "ana", "password": "Admin123!" }
 */
public record LoginRequest(

        @NotBlank(message = "El usuario es obligatorio")
        String usuario,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
