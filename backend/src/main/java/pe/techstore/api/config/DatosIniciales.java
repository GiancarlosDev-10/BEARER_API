package pe.techstore.api.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import pe.techstore.api.model.Producto;
import pe.techstore.api.repository.ProductoRepository;

/**
 * Carga datos de ejemplo cada vez que arranca el servidor.
 *
 * CommandLineRunner = Spring ejecuta el método run() una vez, al terminar de arrancar.
 * Como la base H2 vive en memoria, empieza vacía en cada arranque.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final ProductoRepository productoRepository;

    // Spring nos "inyecta" el repositorio automáticamente por el constructor
    public DatosIniciales(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String... args) {
        if (productoRepository.count() > 0) {
            return; // Ya hay datos, no duplicar
        }

        productoRepository.saveAll(List.of(
                new Producto("Mouse Logitech M170", new BigDecimal("49.90"), 120, "perifericos"),
                new Producto("Teclado Redragon K552", new BigDecimal("159.00"), 35, "perifericos"),
                new Producto("Audífonos JBL Tune 520", new BigDecimal("229.00"), 40, "audio"),
                new Producto("Monitor LG 24\"", new BigDecimal("599.00"), 15, "monitores"),
                new Producto("Laptop Lenovo IdeaPad", new BigDecimal("2499.00"), 8, "laptops")
        ));
    }
}
