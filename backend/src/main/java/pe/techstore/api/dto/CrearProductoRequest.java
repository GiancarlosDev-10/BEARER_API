package pe.techstore.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Datos que se reciben al CREAR un producto (POST).
 * Aquí todos los campos son obligatorios: no tiene sentido crear un producto a medias.
 *
 * Un "record" es una clase corta de Java para transportar datos:
 * Java genera solo el constructor y los métodos nombre(), precio(), etc.
 * DTO = Data Transfer Object (objeto para transportar datos entre cliente y servidor).
 */
public record CrearProductoRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
        // La columna de la BD es precision = 10, scale = 2: hasta 99 999 999.99.
        // Sin esta regla, un precio más grande pasaba la validación y fallaba en la BD (error 500).
        @Digits(integer = 8, fraction = 2, message = "El precio admite máximo 8 dígitos enteros y 2 decimales")
        BigDecimal precio,

        @NotNull(message = "El stock es obligatorio")
        @PositiveOrZero(message = "El stock no puede ser negativo")
        Integer stock,

        @NotBlank(message = "La categoría es obligatoria")
        @Size(max = 50, message = "La categoría no puede superar 50 caracteres")
        String categoria
) {
}
