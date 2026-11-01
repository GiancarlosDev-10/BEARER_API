package pe.techstore.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Un usuario que puede iniciar sesión en el panel de TechStore.
 *
 * IMPORTANTE: la contraseña NUNCA se guarda tal cual. Se guarda su "hash" BCrypt:
 * un texto irreversible como "$2a$10$Xk3...". Al iniciar sesión, BCrypt compara
 * la contraseña escrita con ese hash, sin necesidad de "descifrarlo".
 * Si alguien roba la base de datos, no obtiene las contraseñas reales.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)   // Guarda el rol como texto ("ADMIN"), no como número
    @Column(nullable = false)
    private Rol rol;

    protected Usuario() {
    }

    public Usuario(String username, String passwordHash, Rol rol) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Rol getRol() {
        return rol;
    }
}
