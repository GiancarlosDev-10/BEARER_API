package pe.techstore.api.exception;

/**
 * Se lanza cuando el usuario o la contraseña del login son incorrectos.
 * GlobalExceptionHandler la convierte en una respuesta 401 en JSON.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        // Mismo mensaje si falla el usuario o la contraseña: así un atacante
        // no puede averiguar qué nombres de usuario existen.
        super("Usuario o contraseña incorrectos");
    }
}
