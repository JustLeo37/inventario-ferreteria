package com.ferreteria.inventario.dto;

import java.math.BigDecimal;

public interface VentasPorVendedor {
    String getVendedor();
    Long getCantidadVentas();
    BigDecimal getTotalVendido();
}