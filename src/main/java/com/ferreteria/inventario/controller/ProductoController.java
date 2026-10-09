package com.ferreteria.inventario.controller;

import com.ferreteria.inventario.dto.VentaItemRequest;
import com.ferreteria.inventario.model.Producto;
import com.ferreteria.inventario.service.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<Producto> listarProductos() {
        return productoService.listarProductos();
    }

    // ---- Endpoints que usan las consultas JPQL ----

    @GetMapping("/buscar")
    public List<Producto> buscarPorNombre(@RequestParam String nombre) {
        return productoService.buscarPorNombre(nombre);
    }

    @GetMapping("/categoria/{nombre}")
    public List<Producto> buscarPorCategoria(@PathVariable String nombre) {
        return productoService.buscarPorCategoria(nombre);
    }

    @GetMapping("/stock-bajo")
    public List<Producto> stockBajo(@RequestParam(defaultValue = "5") Integer umbral) {
        return productoService.listarStockBajo(umbral);
    }

    @GetMapping("/valor-inventario")
    public Map<String, Double> valorInventario() {
        return Map.of("valorTotal", productoService.valorTotalInventario());
    }

    // ---- Proceso transaccional ----

    @PostMapping("/venta")
    public Map<String, Object> registrarVenta(@RequestBody List<VentaItemRequest> items) {
        double total = productoService.registrarVenta(items);
        return Map.of("mensaje", "Venta registrada correctamente", "total", total);
    }

    // ---- CRUD ----

    @GetMapping("/{id}")
    public ResponseEntity<Producto> buscarPorId(@PathVariable Long id) {
        return productoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Producto guardarProducto(@RequestBody Producto producto) {
        return productoService.guardarProducto(producto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(
            @PathVariable Long id,
            @RequestBody Producto producto) {

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {

        if (productoService.buscarPorId(id).isPresent()) {
            productoService.eliminarProducto(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}