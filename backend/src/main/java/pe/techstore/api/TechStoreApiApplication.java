package pe.techstore.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación.
 *
 * @SpringBootApplication le indica a Spring que busque automáticamente
 * todas las clases de este paquete (y subpaquetes) marcadas con
 * @RestController, @Configuration, etc., y las ponga a funcionar.
 */
@SpringBootApplication
public class TechStoreApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechStoreApiApplication.class, args);
    }
}
