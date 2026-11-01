package pe.techstore.api.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import pe.techstore.api.dto.LoginRequest;
import pe.techstore.api.dto.LoginResponse;
import pe.techstore.api.exception.CredencialesInvalidasException;
import pe.techstore.api.model.Usuario;
import pe.techstore.api.repository.UsuarioRepository;
import pe.techstore.api.security.JwtService;

/**
 * Login: cambia usuario + contraseña por un Bearer Token.
 *
 *   POST /auth/login   { "usuario": "ana", "password": "Admin123!" }
 *   -> 200 { accessToken, tokenType: "Bearer", expiraEnSegundos, usuario, rol }
 *   -> 401 si el usuario o la contraseña son incorrectos
 *
 * La contraseña se envía UNA sola vez (aquí). Desde ese momento, el cliente
 * usa el token, que vence en pocos minutos.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "2. Login", description = "Cambia usuario y contraseña por un Bearer Token")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Hash de una contraseña cualquiera, usado cuando el usuario NO existe (ver login()).
    private final String hashFalso;

    public AuthController(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.hashFalso = passwordEncoder.encode("usuario-inexistente");
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener un token JWT")
    public LoginResponse login(@Valid @RequestBody LoginRequest datos) {
        Usuario usuario = usuarioRepository.findByUsername(datos.usuario()).orElse(null);

        // Aunque el usuario no exista, igual ejecutamos BCrypt (con un hash falso).
        // Así la respuesta tarda lo mismo en ambos casos y nadie puede adivinar
        // qué usuarios existen midiendo el tiempo de respuesta.
        String hashGuardado = (usuario != null) ? usuario.getPasswordHash() : hashFalso;
        boolean passwordCorrecta = passwordEncoder.matches(datos.password(), hashGuardado);

        if (usuario == null || !passwordCorrecta) {
            throw new CredencialesInvalidasException();   // -> 401
        }

        String token = jwtService.generarToken(usuario);

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpiracionSegundos(),
                usuario.getUsername(),
                usuario.getRol().name()
        );
    }
}
