package com.ferreteria.inventario.controller;

import com.ferreteria.inventario.dto.LoginRequest;
import com.ferreteria.inventario.dto.RegisterRequest;
import com.ferreteria.inventario.model.Usuario;
import com.ferreteria.inventario.repository.UsuarioRepository;
import com.ferreteria.inventario.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "1. Autenticación", description = "Login y registro de usuarios. Endpoints públicos (no requieren token).")
@SecurityRequirements() // quita el candado: estos endpoints son públicos
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "Iniciar sesión",
            description = "Valida usuario y contraseña con BCrypt y devuelve un JWT firmado (HS256) válido por 1 hora.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login correcto: devuelve el token",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.firma",
                              "tipo": "Bearer",
                              "username": "admin",
                              "rol": "ADMIN",
                              "expiraEnMs": 3600000
                            }"""))),
            @ApiResponse(responseCode = "400", description = "Falta el usuario o la contraseña", content = @Content),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas", content = @Content)
    })
    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));

            Usuario usuario = usuarioRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

            String token = jwtUtil.generarToken(usuario.getUsername(), usuario.getRol());

            return Map.of(
                    "token", token,
                    "tipo", "Bearer",
                    "username", usuario.getUsername(),
                    "rol", usuario.getRol(),
                    "expiraEnMs", jwtUtil.getExpirationMs());

        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
    }

    @Operation(summary = "Registrar usuario", description = "Crea un usuario nuevo con rol USER (vendedor).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (usuario 3-50 caracteres, contraseña 6-100)", content = @Content),
            @ApiResponse(responseCode = "409", description = "El usuario ya existe", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya existe");
        }

        Usuario nuevo = new Usuario(request.username(),
                passwordEncoder.encode(request.password()), "USER");
        usuarioRepository.save(nuevo);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Usuario registrado correctamente", "username", nuevo.getUsername()));
    }
}
