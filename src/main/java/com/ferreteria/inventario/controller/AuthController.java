package com.ferreteria.inventario.controller;

import com.ferreteria.inventario.dto.LoginRequest;
import com.ferreteria.inventario.dto.RegisterRequest;
import com.ferreteria.inventario.model.Usuario;
import com.ferreteria.inventario.repository.UsuarioRepository;
import com.ferreteria.inventario.security.JwtUtil;
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