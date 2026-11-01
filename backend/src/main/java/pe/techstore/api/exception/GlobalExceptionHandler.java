package pe.techstore.api.exception;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import pe.techstore.api.dto.ErrorResponse;

/**
 * Atrapa los errores que ocurren en los controladores y los convierte
 * en respuestas JSON con el formato de ErrorResponse.
 *
 * Así el cliente nunca recibe un "stack trace" (el detalle técnico interno
 * del error), que podría revelar información útil para un atacante.
 *
 * Nota: los errores 401 (falta token) y 403 (sin permiso) NO pasan por aquí, porque
 * ocurren en los filtros de seguridad, antes de llegar al controlador.
 * Esos se manejan en security/ErroresSeguridadHandler.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404: el producto (u otro recurso) no existe
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    // 401: login con usuario o contraseña incorrectos
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> credencialesInvalidas(CredencialesInvalidasException ex, HttpServletRequest req) {
        return responder(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }

    // 404: la ruta no existe (ej. /api/no-existe)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> rutaNoExiste(NoResourceFoundException ex, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, "La ruta solicitada no existe", req);
    }

    // 400: los datos no cumplen las reglas (@NotBlank, @PositiveOrZero, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> datosInvalidos(MethodArgumentNotValidException ex, HttpServletRequest req) {
        // Junta todos los mensajes en uno: "precio: El precio no puede ser negativo; nombre: ..."
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return responder(HttpStatus.BAD_REQUEST, mensaje, req);
    }

    // 400: el JSON está mal escrito o un campo tiene un tipo incorrecto (ej. "precio": "abc")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido", req);
    }

    // 400: un parámetro de la URL tiene un tipo incorrecto (ej. /api/productos/abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> parametroInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, "El parámetro '" + ex.getName() + "' tiene un valor inválido", req);
    }

    // 405: el método HTTP no está permitido en esa ruta (ej. DELETE /api/productos/1)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> metodoNoPermitido(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "Método " + ex.getMethod() + " no permitido en esta ruta", req);
    }

    // 500: cualquier otro error inesperado.
    // El detalle se guarda en el log del SERVIDOR; al cliente solo le llega un mensaje genérico.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> errorInesperado(Exception ex, HttpServletRequest req) {
        log.error("Error inesperado en {}", req.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno en el servidor", req);
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus status, String mensaje, HttpServletRequest req) {
        ErrorResponse cuerpo = ErrorResponse.de(status.value(), status.getReasonPhrase(), mensaje, req.getRequestURI());
        return ResponseEntity.status(status).body(cuerpo);
    }
}
