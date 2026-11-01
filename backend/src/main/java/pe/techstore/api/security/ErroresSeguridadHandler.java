package pe.techstore.api.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pe.techstore.api.dto.ErrorResponse;

/**
 * Respuestas JSON para los errores de seguridad.
 *
 * Estos errores ocurren en los filtros, ANTES de llegar al controlador,
 * por eso no los atrapa GlobalExceptionHandler y hay que configurarlos aparte.
 *
 *  401 Unauthorized = "No sé quién eres"      (sin token, token falso o vencido)
 *  403 Forbidden    = "Sé quién eres, pero no tienes permiso"  (ej. CLIENTE intentando editar)
 */
@Component
public class ErroresSeguridadHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    // Atributo de la petición donde los filtros (JWT o API Key) dejan el motivo del rechazo,
    // para que la respuesta 401 lo explique ("token vencido", "API Key incorrecta", etc.)
    public static final String ATRIBUTO_MOTIVO_401 = "techstore.motivo401";

    private final ObjectMapper objectMapper;   // Convierte objetos Java a JSON

    public ErroresSeguridadHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // ===== 401: no autenticado =====
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // Si algún filtro dejó un motivo (token vencido, API Key incorrecta...), lo usamos;
        // si no, es que no se envió ninguna credencial.
        Object motivo = request.getAttribute(ATRIBUTO_MOTIVO_401);
        String mensaje = (motivo != null)
                ? motivo.toString()
                : "Se requiere autenticación: envía el header 'Authorization: Bearer <token>'.";

        escribir(response, request, HttpStatus.UNAUTHORIZED, mensaje);
    }

    // ===== 403: autenticado pero sin permiso =====
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        escribir(response, request, HttpStatus.FORBIDDEN,
                "No tienes permiso para esta operación. Se requiere rol ADMIN.");
    }

    private void escribir(HttpServletResponse response, HttpServletRequest request,
                          HttpStatus status, String mensaje) throws IOException {

        ErrorResponse cuerpo = ErrorResponse.de(status.value(), status.getReasonPhrase(),
                mensaje, request.getRequestURI());

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), cuerpo);
    }
}
