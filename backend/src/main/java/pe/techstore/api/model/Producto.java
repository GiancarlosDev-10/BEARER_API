package pe.techstore.api.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Un producto del catálogo de TechStore.
 *
 * @Entity indica que cada objeto Producto se guarda como una fila
 * en la tabla "productos" de la base de datos.
 *
 * Los campos usan tipos "objeto" (Integer, BigDecimal) y no primitivos (int, double)
 * porque necesitamos que puedan quedar en null: eso es lo que pasa con un PUT
 * que no envía todos los campos (ver ProductoController).
 */
@Entity
@Table(name = "productos")
public class Producto {

    @Id                                                   // Clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // La BD asigna el id: 1, 2, 3...
    private Long id;

    private String nombre;

    // BigDecimal es el tipo correcto para dinero (double tiene errores de redondeo)
    @Column(precision = 10, scale = 2)
    private BigDecimal precio;

    private Integer stock;

    private String categoria;

    // JPA necesita un constructor vacío
    protected Producto() {
    }

    public Producto(String nombre, BigDecimal precio, Integer stock, String categoria) {
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
    }

    // ===== Getters y setters (Jackson los usa para convertir a/desde JSON) =====

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}
