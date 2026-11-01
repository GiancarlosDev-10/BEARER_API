package pe.techstore.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Lo que envía el sistema proveedor para actualizar el stock de un producto.
 * Ejemplo: { "productoId": 1, "stock": 50 }
 */
public record ActualizarStockRequest(

        @NotNull(message = "El productoId es obligatorio")
        Long productoId,

        @NotNull(message = "El stock es obligatorio")
        @PositiveOrZero(message = "El stock no puede ser negativo")
        Integer stock
) {
}
