package pe.techstore.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Datos que se reciben al ACTUALIZAR un producto (PUT y PATCH).
 *
 * Ningún campo es obligatorio: si un campo no viene en el JSON, llega como null.
 * Lo que cambia es cómo lo interpreta cada método:
 *   - PUT   -> null significa "déjalo vacío" (reemplazo total).
 *   - PATCH -> null significa "no lo toques" (actualización parcial).
 *
 * Las reglas de abajo solo se revisan si el campo SÍ viene
 * (por ejemplo, si envían un precio, no puede ser negativo).
 */
public record ActualizarProductoRequest(

        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
        BigDecimal precio,

        @PositiveOrZero(message = "El stock no puede ser negativo")
        Integer stock,

        @Size(max = 50, message = "La categoría no puede superar 50 caracteres")
        String categoria
) {
}
