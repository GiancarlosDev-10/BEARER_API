package pe.techstore.api.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Agrupa en un solo lugar las propiedades propias del proyecto (las que empiezan con "techstore.").
 *
 * @ConfigurationProperties(prefix = "techstore") hace que Spring lea application.yml
 * y llene esta clase automáticamente:
 *
 *   techstore:
 *     jwt:
 *       secret: ...               ->  propiedades.jwt().secret()
 *       expiracion-minutos: 15    ->  propiedades.jwt().expiracionMinutos()
 *     proveedor:
 *       api-key: ...              ->  propiedades.proveedor().apiKey()
 *     cors:
 *       origenes: a,b,c           ->  propiedades.cors().origenes()  (lista de 3 elementos)
 *     usuarios:
 *       admin-password: ...       ->  propiedades.usuarios().adminPassword()
 *       cliente-password: ...     ->  propiedades.usuarios().clientePassword()
 *
 * Ventajas frente a usar @Value("${...}") en cada clase:
 *  - Toda la configuración está documentada en un único archivo.
 *  - Spring convierte los tipos solo (el texto "15" pasa a ser el número 15,
 *    y "a,b,c" pasa a ser una lista).
 *  - Gracias a spring-boot-configuration-processor, el editor reconoce estas
 *    propiedades en application.yml (autocompletado y sin el aviso "Unknown property").
 *
 * Se registra con @ConfigurationPropertiesScan en TechStoreApiApplication.
 *
 * @param jwt       configuración de los tokens JWT (Bearer)
 * @param proveedor configuración de la API Key del sistema proveedor
 * @param cors      configuración de CORS (qué páginas web pueden llamar a la API)
 * @param usuarios  contraseñas de los usuarios de prueba (ana y carlos)
 */
@ConfigurationProperties(prefix = "techstore")
public record TechStoreProperties(Jwt jwt, Proveedor proveedor, Cors cors, Usuarios usuarios) {

    /**
     * Propiedades "techstore.jwt.*".
     *
     * @param secret            Clave secreta para firmar los tokens JWT (mínimo 32 caracteres).
     *                          Se lee de la variable de entorno JWT_SECRET.
     * @param expiracionMinutos Minutos de validez de un token antes de vencer.
     *                          Se lee de la variable de entorno JWT_EXPIRACION_MINUTOS.
     */
    public record Jwt(
            String secret,
            @DefaultValue("15") long expiracionMinutos   // Si no se define, 15 minutos
    ) {
    }

    /**
     * Propiedades "techstore.proveedor.*".
     *
     * @param apiKey API Key que debe enviar el sistema proveedor en el header X-API-KEY
     *               (mínimo 32 caracteres). Se lee de la variable de entorno PROVEEDOR_API_KEY.
     */
    public record Proveedor(String apiKey) {
    }

    /**
     * Propiedades "techstore.cors.*".
     *
     * @param origenes Páginas web (origen = protocolo + dominio + puerto) que pueden llamar
     *                 a la API desde el navegador. Se lee de la variable de entorno CORS_ORIGENES,
     *                 separadas por comas.
     */
    public record Cors(List<String> origenes) {
    }

    /**
     * Propiedades "techstore.usuarios.*".
     *
     * Las contraseñas de los usuarios de prueba NO están escritas en el código ni en el README:
     * si estuvieran, cualquiera que lea GitHub podría entrar como ADMIN en el servidor publicado.
     *
     * @param adminPassword   Contraseña de "ana" (rol ADMIN). Variable de entorno ADMIN_PASSWORD.
     * @param clientePassword Contraseña de "carlos" (rol CLIENTE). Variable de entorno CLIENTE_PASSWORD.
     */
    public record Usuarios(String adminPassword, String clientePassword) {
    }
}
