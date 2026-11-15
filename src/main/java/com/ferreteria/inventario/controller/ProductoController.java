package com.ferreteria.inventario.controller;

import com.ferreteria.inventario.dto.PaginaResponse;
import com.ferreteria.inventario.dto.VentaItemRequest;
import com.ferreteria.inventario.model.Producto;
import com.ferreteria.inventario.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "3. Productos", description = "CRUD de productos y consultas JPQL. Consultas: ADMIN y USER. Crear, editar y eliminar: solo ADMIN.")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Sin token, o token inválido/expirado", content = @Content),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación", content = @Content)
})
public class ProductoController {

    private static final String EJEMPLO_PRODUCTO = """
            {
              "nombre": "Martillo de acero",
              "marca": "Stanley",
              "precio": 35.5,
              "stock": 20,
              "categoriaRelacionada": { "id": 1 }
            }""";

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Operation(summary = "Listar productos", description = "Devuelve todos los productos. Roles: ADMIN, USER.")
    @ApiResponse(responseCode = "200", description = "Lista de productos")
    @GetMapping
    public List<Producto> listarProductos() {
        return productoService.listarProductos();
    }

    @Operation(summary = "Listar productos paginados y ordenados", description = "Roles: ADMIN, USER. Tamaño máximo de página: 50.")
    @ApiResponse(responseCode = "200", description = "Página de productos")
    @GetMapping("/paginado")
    public PaginaResponse<Producto> listarPaginado(
            @Parameter(description = "Número de página (empieza en 0)", example = "0")
            @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Cantidad de productos por página (1 a 50)", example = "5")
            @RequestParam(defaultValue = "5") int tamanio,
            @Parameter(description = "Campo de orden: nombre, precio, stock o marca", example = "precio")
            @RequestParam(defaultValue = "nombre") String ordenarPor) {
        return productoService.listarPaginado(pagina, tamanio, ordenarPor);
    }

    // ---- Endpoints que usan las consultas JPQL ----

    @Operation(summary = "Buscar productos por nombre", description = "Búsqueda parcial por nombre (JPQL). Roles: ADMIN, USER.")
    @ApiResponse(responseCode = "200", description = "Productos encontrados")
    @GetMapping("/buscar")
    public List<Producto> buscarPorNombre(
            @Parameter(description = "Texto a buscar en el nombre", example = "martillo")
            @RequestParam String nombre) {
        return productoService.buscarPorNombre(nombre);
    }

    @Operation(summary = "Listar productos de una categoría", description = "Filtra por nombre de categoría (JPQL). Roles: ADMIN, USER.")
    @ApiResponse(responseCode = "200", description = "Productos de la categoría")
    @GetMapping("/categoria/{nombre}")
    public List<Producto> buscarPorCategoria(
            @Parameter(description = "Nombre de la categoría", example = "Herramientas")
            @PathVariable String nombre) {
        return productoService.buscarPorCategoria(nombre);
    }

    @Operation(summary = "Productos con stock bajo", description = "Productos con stock menor o igual al umbral (JPQL). Roles: ADMIN, USER.")
    @ApiResponse(responseCode = "200", description = "Productos con stock bajo")
    @GetMapping("/stock-bajo")
    public List<Producto> stockBajo(
            @Parameter(description = "Umbral de stock", example = "5")
            @RequestParam(defaultValue = "5") Integer umbral) {
        return productoService.listarStockBajo(umbral);
    }

    @Operation(summary = "Valor total del inventario", description = "Suma de precio × stock de todos los productos (JPQL). Roles: ADMIN, USER.")
    @ApiResponse(responseCode = "200", description = "Valor total",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            { "valorTotal": 1250.5 }""")))
    @GetMapping("/valor-inventario")
    public Map<String, Double> valorInventario() {
        return Map.of("valorTotal", productoService.valorTotalInventario());
    }

    // ---- Proceso transaccional ----

    @Operation(summary = "Registrar venta (versión anterior)", deprecated = true,
            description = "Se mantiene por compatibilidad. Use POST /api/ventas, que además guarda el historial de la venta. Roles: ADMIN, USER.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta registrada y stock descontado"),
            @ApiResponse(responseCode = "400", description = "Venta vacía o cantidad inválida", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe alguno de los productos", content = @Content),
            @ApiResponse(responseCode = "409", description = "Stock insuficiente: no se descuenta nada (rollback)", content = @Content)
    })
    @PostMapping("/venta")
    public Map<String, Object> registrarVenta(@RequestBody List<VentaItemRequest> items) {
        double total = productoService.registrarVenta(items);
        return Map.of("mensaje", "Venta registrada correctamente", "total", total);
    }

    // ---- CRUD ----

    @Operation(summary = "Obtener producto por id", description = "Roles: ADMIN, USER.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe el producto", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Producto> buscarPorId(
            @Parameter(description = "Id del producto", example = "1") @PathVariable Long id) {
        return productoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear producto",
            description = "Rol: ADMIN. El campo 'categoria' (texto) se completa solo a partir de 'categoriaRelacionada.id'.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del producto",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = EJEMPLO_PRODUCTO)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (nombre/marca vacíos, precio o stock negativos, categoría inexistente)", content = @Content)
    })
    @PostMapping
    public Producto guardarProducto(@Valid @RequestBody Producto producto) {
        return productoService.guardarProducto(producto);
    }

    @Operation(summary = "Actualizar producto", description = "Rol: ADMIN.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos actualizados del producto",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = EJEMPLO_PRODUCTO)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe el producto", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(
            @Parameter(description = "Id del producto", example = "1") @PathVariable Long id,
            @Valid @RequestBody Producto producto) {

        return productoService.buscarPorId(id)
                .map(productoExistente -> {

                    productoExistente.setNombre(producto.getNombre());
                    productoExistente.setCategoria(producto.getCategoria());
                    productoExistente.setMarca(producto.getMarca());
                    productoExistente.setPrecio(producto.getPrecio());
                    productoExistente.setStock(producto.getStock());
                    if (producto.getCategoriaRelacionada() != null) {
                        productoExistente.setCategoriaRelacionada(producto.getCategoriaRelacionada());
                    }

                    return ResponseEntity.ok(
                            productoService.guardarProducto(productoExistente)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Desactivar producto",
            description = "Borrado lógico (soft delete): el producto se desactiva y sale del catálogo, "
                    + "pero se conserva para el historial de ventas. Rol: ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto desactivado"),
            @ApiResponse(responseCode = "404", description = "No existe el producto", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(
            @Parameter(description = "Id del producto", example = "1") @PathVariable Long id) {

        if (productoService.buscarPorId(id).isPresent()) {
            productoService.eliminarProducto(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
