
package com.ferreteria.inventario.service;

import com.ferreteria.inventario.dto.PaginaResponse;
import com.ferreteria.inventario.dto.VentaItemRequest;
import com.ferreteria.inventario.model.Categoria;
import com.ferreteria.inventario.model.DetalleVenta;
import com.ferreteria.inventario.model.Producto;
import com.ferreteria.inventario.model.Venta;
import com.ferreteria.inventario.repository.CategoriaRepository;
import com.ferreteria.inventario.repository.ProductoRepository;
import com.ferreteria.inventario.repository.VentaRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final VentaRepository ventaRepository;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository,
                           VentaRepository ventaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.ventaRepository = ventaRepository;
    }

    // ---------- Consultas (solo lectura) ----------

    @Transactional(readOnly = true)
    public List<Producto> listarProductos() {
        return productoRepository.findByActivoTrue();
    }

    @Transactional(readOnly = true)
    public PaginaResponse<Producto> listarPaginado(
            int pagina, int tamanio, String ordenarPor) {

        List<String> permitidos = List.of(
                "nombre", "precio", "stock", "marca"
        );

        String campo = permitidos.contains(ordenarPor)
                ? ordenarPor : "nombre";

        int tam = Math.min(Math.max(tamanio, 1), 50);

        var resultado = productoRepository.findByActivoTrue(
                PageRequest.of(
                        Math.max(pagina, 0),
                        tam,
                        Sort.by(campo)
                )
        );

        return PaginaResponse.desde(resultado);
    }

    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findByIdAndActivoTrue(id);
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

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe el producto con id " + id
                ));

        producto.setActivo(false);
        productoRepository.save(producto);
    }

    // ---------- Proceso transaccional de ventas ----------

    /**
     * Registra una venta con varios productos.
     * Si algún ítem falla, la transacción revierte
     * todos los descuentos de inventario y el registro.
     *
     * @return total de la venta
     */
    @Transactional
    public double registrarVenta(List<VentaItemRequest> items) {

        if (items == null || items.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La venta no tiene productos"
            );
        }

        // Obtener el usuario autenticado
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debe iniciar sesión para registrar una venta"
            );
        }

        // Crear la entidad de venta
        Venta venta = new Venta();

        venta.setFecha(LocalDateTime.now());
        venta.setVendedor(authentication.getName());

        BigDecimal total = BigDecimal.ZERO;

        for (VentaItemRequest item : items) {

            // Validar los datos del producto
            if (item.productoId() == null
                    || item.cantidad() == null
                    || item.cantidad() < 1) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cada ítem requiere productoId y una cantidad mayor o igual a 1"
                );
            }

            // Buscar el producto
            Producto producto = productoRepository.findById(item.productoId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "No existe el producto con id " + item.productoId()
                    ));

            // Comprobar si está activo
            if (!producto.isActivo()) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "El producto '" + producto.getNombre()
                                + "' está desactivado y no se puede vender"
                );
            }

            // Comprobar el stock disponible
            int disponible = producto.getStock() == null
                    ? 0 : producto.getStock();

            if (disponible < item.cantidad()) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Stock insuficiente para '" + producto.getNombre()
                                + "' (disponible: " + disponible
                                + ", solicitado: " + item.cantidad() + ")"
                );
            }

            // Descontar las unidades del inventario
            producto.setStock(disponible - item.cantidad());

            productoRepository.save(producto);

            // Obtener el precio unitario
            BigDecimal precio = producto.getPrecio() == null
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(producto.getPrecio());

            // Crear el detalle de la venta
            DetalleVenta detalle = new DetalleVenta(
                    producto,
                    item.cantidad(),
                    precio
            );

            venta.agregarDetalle(detalle);

            // Acumular el subtotal
            total = total.add(detalle.getSubtotal());
        }

        // Asignar el total y guardar la venta con sus detalles
        venta.setTotal(total);

        ventaRepository.save(venta);

        return total.doubleValue();
    }
}
