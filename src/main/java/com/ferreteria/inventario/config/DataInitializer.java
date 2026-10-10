package com.ferreteria.inventario.config;

import com.ferreteria.inventario.model.Usuario;
import com.ferreteria.inventario.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner crearUsuariosIniciales(UsuarioRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (!repo.existsByUsername("admin")) {
                repo.save(new Usuario("admin", encoder.encode("admin123"), "ADMIN"));
            }
            if (!repo.existsByUsername("vendedor")) {
                repo.save(new Usuario("vendedor", encoder.encode("vendedor123"), "USER"));
            }
        };
    }
}