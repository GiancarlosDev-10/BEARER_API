package pe.techstore.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.techstore.api.model.Usuario;

/**
 * Acceso a la tabla de usuarios.
 *
 * findByUsername: Spring Data entiende el nombre del método y genera solo la consulta
 * "SELECT * FROM usuarios WHERE username = ?".
 * Optional = puede que encuentre el usuario o puede que no.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);
}
