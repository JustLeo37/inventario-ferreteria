package com.ferreteria.inventario.repository;

import com.ferreteria.inventario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Métodos derivados: Spring Data genera la consulta a partir del nombre
    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);
}