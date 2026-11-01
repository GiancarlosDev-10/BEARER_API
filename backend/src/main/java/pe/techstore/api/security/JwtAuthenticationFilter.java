package pe.techstore.api.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro que se ejecuta en CADA petición, antes de llegar al controlador.
 *
 * Busca el header:   Authorization: Bearer eyJhbGciOi...
 *  - Si el token es válido -> registra al usuario y su rol como "autenticado".
 *  - Si no hay token o es inválido -> no registra a nadie. Luego Spring Security decide:
 *    si la ruta es pública, deja pasar; si es protegida, responde 401.
 *
 * "Bearer" significa "portador": quien PORTA el token tiene el acceso.
 * Por eso un token robado es peligroso, y por eso vence en pocos minutos.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // Solo actuamos si viene "Authorization: Bearer ..."
        if (header != null && header.startsWith(PREFIJO_BEARER)) {
            String token = header.substring(PREFIJO_BEARER.length()).trim();

            try {
                Claims datos = jwtService.validarToken(token);

                String usuario = datos.getSubject();
                String rol = datos.get("rol", String.class);

                // Spring Security espera los roles con el prefijo "ROLE_" (ROLE_ADMIN, ROLE_CLIENTE).
                // El rol se toma DEL TOKEN, sin consultar la base de datos: eso es "stateless".
                // Es seguro porque la firma garantiza que nadie lo modificó.
                var autenticacion = new UsernamePasswordAuthenticationToken(
                        usuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));

                // Guardamos los datos del token para que /api/usuarios/me pueda mostrar la expiración
                autenticacion.setDetails(datos);

                SecurityContextHolder.getContext().setAuthentication(autenticacion);

            } catch (ExpiredJwtException e) {
                request.setAttribute(ErroresSeguridadHandler.ATRIBUTO_MOTIVO_401,
                        "El token ha vencido. Inicia sesión nuevamente.");
            } catch (JwtException | IllegalArgumentException e) {
                request.setAttribute(ErroresSeguridadHandler.ATRIBUTO_MOTIVO_401,
                        "El token es inválido (firma incorrecta o mal formado).");
            }
        }

        // Continúa con el siguiente filtro / el controlador
        filterChain.doFilter(request, response);
    }

    /**
     * Las rutas del proveedor SOLO aceptan API Key. Este filtro no se ejecuta en ellas,
     * así que un Bearer Token (aunque sea de un ADMIN) no sirve ahí: responde 401.
     * Cada credencial tiene su propósito: Bearer para usuarios, API Key para sistemas.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith(ApiKeyAuthenticationFilter.RUTAS_PROVEEDOR);
    }
}
