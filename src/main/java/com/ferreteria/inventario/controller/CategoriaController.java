package com.ferreteria.inventario.controller;

import com.ferreteria.inventario.model.Categoria;
import com.ferreteria.inventario.repository.CategoriaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "2. Categorías", description = "Consulta (ADMIN, USER) y registro (solo ADMIN) de categorías")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Sin token, o token inválido/expirado", content = @Content),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación", content = @Content)
})
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    public CategoriaController(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Operation(summary = "Listar categorías", description = "Roles permitidos: ADMIN, USER.")
    @ApiResponse(responseCode = "200", description = "Lista de categorías")
    @GetMapping
    public List<Categoria> getAllCategorias() {
        return categoriaRepository.findAll();
    }

    @Operation(summary = "Crear categoría", description = "Rol permitido: ADMIN. El id se genera automáticamente.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Nombre de la nueva categoría",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            { "nombre": "Herramientas" }""")))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoría creada"),
            @ApiResponse(responseCode = "400", description = "JSON inválido", content = @Content),
            @ApiResponse(responseCode = "409", description = "Ya existe una categoría con ese nombre", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Categoria> guardarCategoria(@RequestBody Categoria categoria) {
        categoria.setId(null);
        Categoria guardada = categoriaRepository.save(categoria);

        return ResponseEntity.created(URI.create("/api/categorias/" + guardada.getId())).body(guardada);
    }
}
