package pe.techstore.api.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import pe.techstore.api.config.TechStoreProperties;
import pe.techstore.api.model.Usuario;

/**
 * Crea y verifica los tokens JWT (los "Bearer Tokens" de esta API).
 *
 * Un JWT tiene 3 partes separadas por puntos:  CABECERA.DATOS.FIRMA
 *  - CABECERA: el algoritmo de firma. jjwt lo elige según el largo de la clave:
 *    32-47 bytes -> HS256, 48-63 -> HS384, 64 o más -> HS512.
 *  - DATOS (claims): usuario ("sub"), rol, fecha de emisión ("iat") y de vencimiento ("exp").
 *    OJO: los datos solo están codificados en Base64, NO cifrados. Cualquiera puede leerlos
 *    (pruébalo en jwt.io), por eso nunca se guardan contraseñas dentro del token.
 *  - FIRMA: se calcula con la clave secreta del servidor. Si alguien modifica los datos
 *    (por ejemplo, cambia "CLIENTE" por "ADMIN"), la firma ya no coincide y el token se rechaza.
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMinutos;

    // Spring nos pasa TechStoreProperties ya lleno con los valores de application.yml
    // (que a su vez vienen de las variables de entorno JWT_SECRET y JWT_EXPIRACION_MINUTOS)
    public JwtService(TechStoreProperties propiedades) {
        String secreto = propiedades.jwt().secret();

        // HMAC-SHA exige una clave de al menos 256 bits (32 bytes). Si falta, mejor no arrancar
        // que arrancar con una clave débil o inventada.
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "Falta la variable de entorno JWT_SECRET o tiene menos de 32 caracteres. "
                    + "En local, ejecuta con el perfil 'local' (ver README).");
        }

        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMinutos = propiedades.jwt().expiracionMinutos();
    }

    /** Crea un token firmado para el usuario que acaba de iniciar sesión. */
    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant vence = ahora.plusSeconds(getExpiracionSegundos());

        return Jwts.builder()
                .subject(usuario.getUsername())          // "sub": a quién pertenece el token
                .claim("rol", usuario.getRol().name())   // dato propio: el rol
                .issuedAt(Date.from(ahora))              // "iat": cuándo se emitió
                .expiration(Date.from(vence))            // "exp": cuándo vence
                .signWith(clave)                         // firma con la clave secreta (HMAC-SHA)
                .compact();                              // lo convierte en el texto "xxx.yyy.zzz"
    }

    /**
     * Verifica el token y devuelve sus datos.
     * Lanza JwtException si la firma no coincide, el token está mal formado o ya venció
     * (ExpiredJwtException es un tipo de JwtException).
     * parseSignedClaims además rechaza tokens SIN firma (el truco de "alg": "none").
     */
    public Claims validarToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpiracionSegundos() {
        return expiracionMinutos * 60;
    }
}
