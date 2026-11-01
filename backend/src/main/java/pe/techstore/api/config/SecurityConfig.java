package pe.techstore.api.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import pe.techstore.api.security.ApiKeyAuthenticationFilter;
import pe.techstore.api.security.ErroresSeguridadHandler;
import pe.techstore.api.security.JwtAuthenticationFilter;

/**
 * Configuración central de seguridad: AQUÍ se decide quién puede entrar a cada ruta.
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final ApiKeyAuthenticationFilter apiKeyFilter;
    private final ErroresSeguridadHandler erroresSeguridad;
    private final TechStoreProperties propiedades;

    // true solo con el perfil "local" (ver application-local.yml)
    @Value("${spring.h2.console.enabled:false}")
    private boolean consolaH2Activa;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter,
                          ApiKeyAuthenticationFilter apiKeyFilter,
                          ErroresSeguridadHandler erroresSeguridad,
                          TechStoreProperties propiedades) {
        this.jwtFilter = jwtFilter;
        this.apiKeyFilter = apiKeyFilter;
        this.erroresSeguridad = erroresSeguridad;
        this.propiedades = propiedades;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS: usa la configuración del método corsConfigurationSource() de abajo
            .cors(Customizer.withDefaults())

            // CSRF desactivado: el ataque CSRF aprovecha que el navegador envía las COOKIES
            // de sesión automáticamente. Esta API no usa cookies: el token se envía a mano
            // en el header Authorization, que un sitio malicioso no puede agregar por su cuenta.
            .csrf(AbstractHttpConfigurer::disable)

            // STATELESS: el servidor no guarda sesiones ni crea cookies de sesión.
            // Cada petición debe traer su propio token.
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Desactivamos el formulario de login y el login "básico" que Spring trae por defecto
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)

            // ===== Reglas de acceso (se revisan de arriba hacia abajo) =====
            .authorizeHttpRequests(auth -> {
                // Público: estado del servidor, login y catálogo (solo lectura)
                auth.requestMatchers(HttpMethod.GET, "/api/public/**").permitAll();
                auth.requestMatchers(HttpMethod.POST, "/auth/login").permitAll();
                auth.requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/*").permitAll();

                // Público: documentación Swagger (para probar la API desde el navegador)
                auth.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll();

                // Solo ADMIN (Bearer Token): crear y modificar productos
                auth.requestMatchers(HttpMethod.POST, "/api/productos").hasRole("ADMIN");
                auth.requestMatchers(HttpMethod.PUT, "/api/productos/*").hasRole("ADMIN");
                auth.requestMatchers(HttpMethod.PATCH, "/api/productos/*").hasRole("ADMIN");

                // Cualquier usuario con token válido (ADMIN o CLIENTE)
                auth.requestMatchers("/api/usuarios/**").authenticated();

                // Solo el sistema proveedor (API Key). Este rol solo lo asigna ApiKeyAuthenticationFilter.
                auth.requestMatchers("/api/proveedor/**").hasRole("PROVEEDOR");

                // Ruta interna de Spring para mostrar errores
                auth.requestMatchers("/error").permitAll();

                // Consola de H2: solo existe en el perfil local
                if (consolaH2Activa) {
                    auth.requestMatchers("/h2-console/**").permitAll();
                }

                // Todo lo demás requiere autenticación (por defecto, cerrado)
                auth.anyRequest().authenticated();
            })

            // Respuestas JSON para 401 y 403
            .exceptionHandling(e -> e
                .authenticationEntryPoint(erroresSeguridad)   // 401
                .accessDeniedHandler(erroresSeguridad))       // 403

            // Nuestros filtros se ejecutan antes del filtro estándar de usuario/contraseña.
            // Cada uno actúa solo en sus rutas: API Key en /api/proveedor/**, JWT en el resto.
            .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // La consola H2 se muestra dentro de un <iframe> del mismo sitio
        if (consolaH2Activa) {
            http.headers(h -> h.frameOptions(f -> f.sameOrigin()));
        }

        return http.build();
    }

    /**
     * CORS (Cross-Origin Resource Sharing).
     *
     * Por seguridad, el NAVEGADOR bloquea que una página (ej. giancarlosdev-10.github.io)
     * lea respuestas de otro dominio (ej. nuestra API en Render), salvo que la API diga
     * explícitamente "esa página está autorizada". Eso es lo que configuramos aquí.
     *
     * Ojo: CORS solo lo aplican los navegadores. Postman, curl o un servidor lo ignoran;
     * por eso CORS NO reemplaza a la autenticación.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Solo los orígenes de CORS_ORIGENES. Nunca "*".
        config.setAllowedOrigins(propiedades.cors().origenes());

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "OPTIONS"));

        // Headers que el frontend puede enviar. X-API-KEY NO está a propósito:
        // la API Key es solo para servidores; ningún navegador debería enviarla.
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // Permite al frontend leer el header Location de la respuesta 201 (POST)
        config.setExposedHeaders(List.of("Location"));

        // No usamos cookies, así que no se permiten credenciales del navegador
        config.setAllowCredentials(false);

        // El navegador puede recordar este permiso 1 hora (menos peticiones OPTIONS)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // BCrypt: algoritmo lento a propósito para guardar contraseñas.
    // Que sea lento dificulta probar millones de contraseñas si roban la base de datos.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
