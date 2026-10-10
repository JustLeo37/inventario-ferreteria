package com.ferreteria.inventario.service;

import com.ferreteria.inventario.dto.PaginaResponse;
import com.ferreteria.inventario.dto.ProductoMasVendido;
import com.ferreteria.inventario.dto.VentaItemRequest;
import com.ferreteria.inventario.dto.VentaResponse;
import com.ferreteria.inventario.dto.VentasPorVendedor;
import com.ferreteria.inventario.model.DetalleVenta;
import com.ferreteria.inventario.model.Producto;
import com.ferreteria.inventario.model.Venta;
import com.ferreteria.inventario.repository.ProductoRepository;
import com.ferreteria.inventario.repository.VentaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;

    public VentaService(VentaRepository ventaRepository, ProductoRepository productoRepository) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Registra una venta completa: valida cada ítem, descuenta el stock y guarda
     * la cabecera con sus detalles. Si UN ítem falla, @Transactional revierte
     * todo (no queda stock descontado ni venta a medias).
     */
    @Transactional
    public VentaResponse registrarVenta(List<VentaItemRequest> items, String vendedor) {
        if (items == null || items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La venta no tiene productos");
        }

        Venta venta = new Venta();
        venta.setFecha(LocalDateTime.now());
        venta.setVendedor(vendedor);

        BigDecimal total = BigDecimal.ZERO;

        for (VentaItemRequest item : items) {
            if (item.productoId() == null || item.cantidad() == null || item.cantidad() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cada ítem requiere productoId y una cantidad mayor o igual a 1");
            }

            // Bloqueo pesimista sobre la fila del producto
            Producto producto = productoRepository.buscarParaActualizar(item.productoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "No existe el producto con id " + item.productoId()));

            int disponible = producto.getStock() == null ? 0 : producto.getStock();
            if (disponible < item.cantidad()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Stock insuficiente para '" + producto.getNombre()
                                + "' (disponible: " + disponible
                                + ", solicitado: " + item.cantidad() + ")");
            }

            producto.setStock(disponible - item.cantidad());
            productoRepository.save(producto);

            BigDecimal precio = producto.getPrecio() == null
                    ? BigDecimal.ZERO : BigDecimal.valueOf(producto.getPrecio());

            DetalleVenta detalle = new DetalleVenta(producto, item.cantidad(), precio);
            venta.agregarDetalle(detalle);
            total = total.add(detalle.getSubtotal());
        }

        venta.setTotal(total);
        return VentaResponse.desde(ventaRepository.save(venta));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<VentaResponse> listarVentas(int pagina, int tamanio) {
        int tam = Math.min(Math.max(tamanio, 1), 50);
        var resultado = ventaRepository.findAll(
                PageRequest.of(Math.max(pagina, 0), tam, Sort.by("fecha").descending()));
        return PaginaResponse.desde(resultado.map(VentaResponse::desde));
    }

    @Transactional(readOnly = true)
    public VentaResponse buscarPorId(Long id) {
        return ventaRepository.findById(id)
                .map(VentaResponse::desde)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe la venta con id " + id));
    }

    @Transactional(readOnly = true)
    public List<VentaResponse> buscarPorFechas(LocalDate desde, LocalDate hasta) {
        if (hasta.isBefore(desde)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha 'hasta' no puede ser anterior a 'desde'");
        }
        return ventaRepository
                .buscarPorRangoFechas(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay())
                .stream().map(VentaResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoMasVendido> productosMasVendidos(int limite) {
        return ventaRepository.productosMasVendidos(PageRequest.of(0, Math.min(Math.max(limite, 1), 50)));
    }

    @Transactional(readOnly = true)
    public List<VentasPorVendedor> totalPorVendedor() {
        return ventaRepository.totalPorVendedor();
    }
}