package pe.techstore.api.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import pe.techstore.api.model.Producto;
import pe.techstore.api.model.Rol;
import pe.techstore.api.model.Usuario;
import pe.techstore.api.repository.ProductoRepository;
import pe.techstore.api.repository.UsuarioRepository;

/**
 * Carga datos de ejemplo cada vez que arranca el servidor.
 *
 * Como la base H2 vive en memoria, empieza vacía en cada arranque.
 *
 * @PostConstruct = Spring ejecuta cargarDatos() apenas crea este objeto, DURANTE el arranque
 * y ANTES de que el servidor empiece a aceptar peticiones en el puerto 8080.
 *
 * ¿Por qué no CommandLineRunner? Porque se ejecuta DESPUÉS de abrir el puerto: durante
 * unos milisegundos (lo que tarda BCrypt) la tabla de usuarios estaría vacía y un login
 * inmediato respondería 401 aunque la contraseña fuera correcta. Pasa justo cuando Render
 * "despierta" el servidor y alguien inicia sesión en seguida.
 */
@Component
public class DatosIniciales {

    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Spring nos "inyecta" estos objetos automáticamente por el constructor
    public DatosIniciales(ProductoRepository productoRepository,
                          UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void cargarDatos() {
        cargarUsuarios();
        cargarProductos();
    }

    private void cargarUsuarios() {
        if (usuarioRepository.count() > 0) {
            return; // Ya hay datos, no duplicar
        }

        // passwordEncoder.encode(...) convierte la contraseña en un hash BCrypt.
        // En la base de datos queda algo como "$2a$10$...", nunca "Admin123!".
        usuarioRepository.saveAll(List.of(
                new Usuario("ana", passwordEncoder.encode("Admin123!"), Rol.ADMIN),
                new Usuario("carlos", passwordEncoder.encode("Cliente123!"), Rol.CLIENTE)
        ));
    }

    private void cargarProductos() {
        if (productoRepository.count() > 0) {
            return;
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
