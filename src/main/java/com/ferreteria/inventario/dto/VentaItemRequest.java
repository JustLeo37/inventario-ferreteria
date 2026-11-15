package com.ferreteria.inventario.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Producto y cantidad dentro de una venta")
public record VentaItemRequest(
        @Schema(description = "Id del producto", example = "1") Long productoId,
        @Schema(description = "Cantidad a vender (mayor a 0)", example = "2") Integer cantidad) {
}
