package pe.techstore.api.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import pe.techstore.api.dto.ActualizarProductoRequest;
import pe.techstore.api.dto.CrearProductoRequest;
import pe.techstore.api.exception.RecursoNoEncontradoException;
import pe.techstore.api.model.Producto;
import pe.techstore.api.repository.ProductoRepository;

/**
 * Endpoints del catálogo de productos.
 *
 *   GET   /api/productos        -> lista todo el catálogo        (público)
 *   GET   /api/productos/{id}   -> un producto                   (público)
 *   POST  /api/productos        -> crea un producto              (solo ADMIN, Bearer Token)
 *   PUT   /api/productos/{id}   -> REEMPLAZA el producto entero  (solo ADMIN, Bearer Token)
 *   PATCH /api/productos/{id}   -> cambia solo algunos campos    (solo ADMIN, Bearer Token)
 *
 * Nota: la seguridad (quién puede usar cada endpoint) NO se escribe aquí,
 * sino en config/SecurityConfig. Las anotaciones @Operation/@SecurityRequirement
 * solo documentan la API en Swagger; no protegen nada por sí mismas.
 */
@RestController
@RequestMapping("/api/productos")
@Tag(name = "3. Productos (Bearer)", description = "Lectura pública; crear y modificar requiere Bearer Token de ADMIN")
public class ProductoController {

    private final ProductoRepository productoRepository;

    public ProductoController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // GET /api/productos -> 200 con la lista
    @GetMapping
    @Operation(summary = "Listar catálogo (público)")
    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    // GET /api/productos/1 -> 200 con el producto, o 404 si no existe
    @GetMapping("/{id}")
    @Operation(summary = "Ver un producto (público)")
    public Producto obtener(@PathVariable Long id) {
        return buscarOFallar(id);
    }

    // POST /api/productos -> 201 Created + header Location con la URL del nuevo producto
    // @Valid hace que se revisen las reglas de CrearProductoRequest (si fallan -> 400)
    @PostMapping
    @Operation(summary = "Crear producto (ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Producto> crear(@Valid @RequestBody CrearProductoRequest datos) {
        Producto nuevo = new Producto(datos.nombre(), datos.precio(), datos.stock(), datos.categoria());
        Producto guardado = productoRepository.save(nuevo);

        // Construye la URL del recurso creado, ej: http://localhost:8080/api/productos/6
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(guardado.getId())
                .toUri();

        return ResponseEntity.created(ubicacion).body(guardado);
    }

    /*
     * PUT = REEMPLAZO TOTAL.
     *
     * Se copian TODOS los campos del JSON recibido al producto, incluso los que
     * no se enviaron (que llegan como null). Esto es INTENCIONAL: reproduce el
     * "caso de Ana" visto en clase.
     *
     * Ana quería cambiar solo el precio del Mouse y envió:
     *     PUT /api/productos/1   { "precio": 39.90 }
     * Resultado: el precio cambió... pero nombre, stock y categoría quedaron en null.
     * ¡El producto quedó "borrado" a medias!
     *
     * Lección: PUT significa "este es el producto completo nuevo".
     * Para cambiar un solo campo, se debe usar PATCH.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Reemplazar producto completo (ADMIN). Campos no enviados quedan en null",
            security = @SecurityRequirement(name = "bearerAuth"))
    public Producto reemplazar(@PathVariable Long id, @Valid @RequestBody ActualizarProductoRequest datos) {
        Producto producto = buscarOFallar(id);

        producto.setNombre(datos.nombre());         // si no vino -> null
        producto.setPrecio(datos.precio());         // si no vino -> null
        producto.setStock(datos.stock());           // si no vino -> null
        producto.setCategoria(datos.categoria());   // si no vino -> null

        return productoRepository.save(producto);
    }

    /*
     * PATCH = ACTUALIZACIÓN PARCIAL.
     *
     * Solo se modifican los campos que vienen en el JSON; los demás se conservan.
     *     PATCH /api/productos/1   { "precio": 39.90 }
     * Resultado: solo cambia el precio. Nombre, stock y categoría siguen igual.
     */
    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar solo los campos enviados (ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public Producto actualizarParcial(@PathVariable Long id, @Valid @RequestBody ActualizarProductoRequest datos) {
        Producto producto = buscarOFallar(id);

        if (datos.nombre() != null) {
            producto.setNombre(datos.nombre());
        }
        if (datos.precio() != null) {
            producto.setPrecio(datos.precio());
        }
        if (datos.stock() != null) {
            producto.setStock(datos.stock());
        }
        if (datos.categoria() != null) {
            producto.setCategoria(datos.categoria());
        }

        return productoRepository.save(producto);
    }

    // Busca el producto o lanza un error que termina en 404
    private Producto buscarOFallar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto con id " + id));
    }
}
