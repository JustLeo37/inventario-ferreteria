package com.ferreteria.inventario.service;

import com.ferreteria.inventario.dto.PaginaResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.ferreteria.inventario.dto.VentaItemRequest;
import com.ferreteria.inventario.model.Categoria;
import com.ferreteria.inventario.model.Producto;
import com.ferreteria.inventario.repository.CategoriaRepository;
import com.ferreteria.inventario.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // ---------- Consultas (solo lectura) ----------

    @Transactional(readOnly = true)
    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }
        @Transactional(readOnly = true)
    public PaginaResponse<Producto> listarPaginado(int pagina, int tamanio, String ordenarPor) {
        List<String> permitidos = List.of("nombre", "precio", "stock", "marca");
        String campo = permitidos.contains(ordenarPor) ? ordenarPor : "nombre";
        int tam = Math.min(Math.max(tamanio, 1), 50);

        var resultado = productoRepository.findAll(
                PageRequest.of(Math.max(pagina, 0), tam, Sort.by(campo)));
        return PaginaResponse.desde(resultado);
    }

    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String texto) {
        return productoRepository.buscarPorNombre(texto);
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarPorCategoria(String nombreCategoria) {
        return productoRepository.buscarPorNombreCategoria(nombreCategoria);
    }

    @Transactional(readOnly = true)
    public List<Producto> listarStockBajo(Integer umbral) {
        return productoRepository.buscarStockBajo(umbral);
    }

    @Transactional(readOnly = true)
    public Double valorTotalInventario() {
        return productoRepository.calcularValorInventario();
    }

    // ---------- Escritura ----------

    @Transactional
    public Producto guardarProducto(Producto producto) {

        if (producto.getCategoriaRelacionada() != null) {

            Long categoriaId = producto.getCategoriaRelacionada().getId();

            if (categoriaId == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Debe proporcionar el ID de la categoria"
                );
            }

            Categoria categoria = categoriaRepository.findById(categoriaId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "La categoria indicada no existe"
                    ));

            producto.setCategoriaRelacionada(categoria);
            producto.setCategoria(categoria.getNombre());
        }

        return productoRepository.save(producto);
    }

    @Transactional
    public void eliminarProducto(Long id) {
        productoRepository.deleteById(id);
    }

    /**
     * Registra una venta con varios productos. Si UN solo ítem falla
     * (producto inexistente o stock insuficiente) se lanza una excepción
     * y @Transactional revierte TODOS los descuentos ya hechos (atomicidad).
     *
     * @return total de la venta
     */
    @Transactional
    public double registrarVenta(List<VentaItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La venta no tiene productos");
        }

        double total = 0;

        for (VentaItemRequest item : items) {
            if (item.productoId() == null || item.cantidad() == null || item.cantidad() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cada ítem requiere productoId y una cantidad mayor o igual a 1");
            }

            Producto producto = productoRepository.findById(item.productoId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "No existe el producto con id " + item.productoId()));

            int disponible = producto.getStock() == null ? 0 : producto.getStock();

            if (disponible < item.cantidad()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Stock insuficiente para '" + producto.getNombre()
                                + "' (disponible: " + disponible
                                + ", solicitado: " + item.cantidad() + ")");
            }

            producto.setStock(disponible - item.cantidad());
            productoRepository.save(producto);

            double precio = producto.getPrecio() == null ? 0 : producto.getPrecio();
            total += precio * item.cantidad();
        }

        return total;
    }
}