package pe.techstore.api.dto;

/**
 * Respuesta al proveedor después de actualizar el stock.
 * Ejemplo: { "productoId": 1, "stockAnterior": 120, "stockNuevo": 50 }
 */
public record StockResponse(
        Long productoId,
        Integer stockAnterior,
        Integer stockNuevo
) {
}
