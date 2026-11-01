package pe.techstore.api.controller;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import pe.techstore.api.dto.ActualizarStockRequest;
import pe.techstore.api.dto.StockResponse;
import pe.techstore.api.exception.RecursoNoEncontradoException;
import pe.techstore.api.model.Producto;
import pe.techstore.api.repository.ProductoRepository;

/**
 * Endpoint para el SISTEMA PROVEEDOR (comunicación servidor a servidor).
 *
 *   PUT /api/proveedor/stock
 *   Header: X-API-KEY: <clave del proveedor>
 *   Body:   { "productoId": 1, "stock": 50 }
 *   -> 200 { "productoId": 1, "stockAnterior": 120, "stockNuevo": 50 }
 *   -> 401 sin API Key o con API Key incorrecta (también si se envía un Bearer Token)
 *
 * La verificación de la API Key la hace ApiKeyAuthenticationFilter, antes de llegar aquí.
 */
@RestController
@RequestMapping("/api/proveedor")
@Tag(name = "4. Proveedor (API Key)", description = "Sistema externo que actualiza stock. Solo acepta API Key.")
public class ProveedorController {

    private final ProductoRepository productoRepository;

    public ProveedorController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @PutMapping("/stock")
    @Operation(summary = "Actualizar stock de un producto", security = @SecurityRequirement(name = "apiKey"))
    public StockResponse actualizarStock(@Valid @RequestBody ActualizarStockRequest datos) {
        Producto producto = productoRepository.findById(datos.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el producto con id " + datos.productoId()));

        Integer stockAnterior = producto.getStock();
        producto.setStock(datos.stock());
        productoRepository.save(producto);

        return new StockResponse(producto.getId(), stockAnterior, producto.getStock());
    }
}
