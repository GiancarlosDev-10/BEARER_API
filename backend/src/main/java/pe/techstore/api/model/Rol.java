package pe.techstore.api.model;

/**
 * Roles posibles de un usuario.
 *  - ADMIN:   puede crear y modificar productos (como Ana).
 *  - CLIENTE: solo puede ver el catálogo y su propio perfil (como Carlos).
 */
public enum Rol {
    ADMIN,
    CLIENTE
}
