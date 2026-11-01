package pe.techstore.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Punto de entrada de la aplicación.
 *
 * @SpringBootApplication le indica a Spring que busque automáticamente
 * todas las clases de este paquete (y subpaquetes) marcadas con
 * @RestController, @Configuration, etc., y las ponga a funcionar.
 *
 * "exclude" desactiva el usuario de prueba que Spring Security crea por defecto
 * (con una contraseña aleatoria impresa en consola). No lo necesitamos:
 * nuestros usuarios están en la base de datos y el login lo hace AuthController.
 *
 * @ConfigurationPropertiesScan busca las clases con @ConfigurationProperties
 * (como config/TechStoreProperties) y las llena con los valores de application.yml.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class TechStoreApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechStoreApiApplication.class, args);
    }
}
