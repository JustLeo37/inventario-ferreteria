package com.ferreteria.inventario.controller;

import com.ferreteria.inventario.model.Categoria;
import com.ferreteria.inventario.repository.CategoriaRepository;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")

public class CategoriaController {
    private final CategoriaRepository categoriaRepository;

    public CategoriaController(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    public List<Categoria> getAllCategorias() {
        return categoriaRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Categoria> guardarCategoria(
        @RequestBody Categoria categoria) {
            categoria.setId(null);
            Categoria guardada = categoriaRepository.save(categoria);

            return ResponseEntity.created(URI.create("/api/categorias/" + guardada.getId())).body(guardada);
    }
}
