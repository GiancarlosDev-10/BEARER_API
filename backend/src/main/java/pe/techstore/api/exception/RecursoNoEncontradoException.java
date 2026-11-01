package pe.techstore.api.exception;

/**
 * Se lanza cuando se busca algo que no existe (por ejemplo, un producto con id 99).
 * GlobalExceptionHandler la convierte en una respuesta 404 en JSON.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
