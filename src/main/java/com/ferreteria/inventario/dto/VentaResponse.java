package com.ferreteria.inventario.dto;

import com.ferreteria.inventario.model.Venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaResponse(Long id, LocalDateTime fecha, String vendedor,
                            BigDecimal total, List<DetalleVentaResponse> detalles) {

    public record DetalleVentaResponse(String producto, Integer cantidad,
                                       BigDecimal precioUnitario, BigDecimal subtotal) {
    }

    public static VentaResponse desde(Venta v) {
        List<DetalleVentaResponse> detalles = v.getDetalles().stream()
                .map(d -> new DetalleVentaResponse(d.getProducto().getNombre(), d.getCantidad(),
                        d.getPrecioUnitario(), d.getSubtotal()))
                .toList();
        return new VentaResponse(v.getId(), v.getFecha(), v.getVendedor(), v.getTotal(), detalles);
    }
}