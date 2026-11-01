package pe.techstore.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.techstore.api.model.Producto;

/**
 * Acceso a la tabla de productos.
 *
 * Solo declaramos la interfaz: Spring Data crea la implementación automáticamente
 * con métodos ya listos como findAll(), findById(id), save(producto), count()...
 * JpaRepository<Producto, Long> = trabaja con Producto, cuyo id es de tipo Long.
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
