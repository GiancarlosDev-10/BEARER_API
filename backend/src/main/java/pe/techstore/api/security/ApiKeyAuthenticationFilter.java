package pe.techstore.api.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pe.techstore.api.config.TechStoreProperties;

/**
 * Filtro de API KEY para las rutas del sistema proveedor (/api/proveedor/**).
 *
 * El proveedor es OTRO SERVIDOR (no una persona): actualiza el stock de TechStore
 * automáticamente. No tiene usuario ni contraseña, ni inicia sesión. Se identifica
 * enviando en cada petición una clave fija que le entregamos:
 *
 *     X-API-KEY: <clave>
 *
 * Diferencias con el Bearer Token:
 *  - La API Key identifica a un SISTEMA; el token identifica a un USUARIO.
 *  - La API Key no vence sola (hay que cambiarla a mano si se filtra);
 *    el token vence en minutos.
 *  - Por eso la API Key solo debe vivir en servidores (variables de entorno),
 *    NUNCA en un frontend, una app móvil ni en GitHub.
 */
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String RUTAS_PROVEEDOR = "/api/proveedor/";
    public static final String HEADER_API_KEY = "X-API-KEY";

    // Guardamos la clave como bytes, listos para comparar
    private final byte[] apiKeyEsperada;

    public ApiKeyAuthenticationFilter(TechStoreProperties propiedades) {
        String apiKey = propiedades.proveedor().apiKey();

        // Igual que con JWT_SECRET: sin una clave fuerte, mejor no arrancar.
        if (apiKey == null || apiKey.length() < 32) {
            throw new IllegalStateException(
                    "Falta la variable de entorno PROVEEDOR_API_KEY o tiene menos de 32 caracteres. "
                    + "En local, ejecuta con el perfil 'local' (ver README).");
        }

        this.apiKeyEsperada = apiKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String apiKeyRecibida = request.getHeader(HEADER_API_KEY);

        if (apiKeyRecibida == null || apiKeyRecibida.isBlank()) {
            request.setAttribute(ErroresSeguridadHandler.ATRIBUTO_MOTIVO_401,
                    "Se requiere una API Key en el header '" + HEADER_API_KEY + "'.");

        } else if (esApiKeyCorrecta(apiKeyRecibida)) {
            // Clave correcta: registramos al proveedor como autenticado con el rol PROVEEDOR
            var autenticacion = new UsernamePasswordAuthenticationToken(
                    "sistema-proveedor", null, List.of(new SimpleGrantedAuthority("ROLE_PROVEEDOR")));
            SecurityContextHolder.getContext().setAuthentication(autenticacion);

        } else {
            request.setAttribute(ErroresSeguridadHandler.ATRIBUTO_MOTIVO_401, "La API Key es incorrecta.");
        }

        // Si no se autenticó, Spring Security responderá 401 (ver SecurityConfig)
        filterChain.doFilter(request, response);
    }

    /**
     * Compara las claves en TIEMPO CONSTANTE.
     *
     * Un equals() normal se detiene en el primer carácter distinto: una clave que empieza
     * bien tarda un poquito más en ser rechazada. Midiendo esos tiempos con millones de
     * intentos, un atacante podría adivinar la clave letra por letra ("timing attack").
     * MessageDigest.isEqual siempre revisa todos los bytes, así que tarda lo mismo.
     */
    private boolean esApiKeyCorrecta(String apiKeyRecibida) {
        byte[] recibida = apiKeyRecibida.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(recibida, apiKeyEsperada);
    }

    /** Este filtro SOLO actúa en las rutas del proveedor; en las demás, la API Key se ignora. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(RUTAS_PROVEEDOR);
    }
}
