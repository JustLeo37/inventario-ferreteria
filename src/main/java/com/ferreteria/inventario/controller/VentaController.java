package com.ferreteria.inventario.controller;

import com.ferreteria.inventario.dto.PaginaResponse;
import com.ferreteria.inventario.dto.ProductoMasVendido;
import com.ferreteria.inventario.dto.VentaItemRequest;
import com.ferreteria.inventario.dto.VentaResponse;
import com.ferreteria.inventario.dto.VentasPorVendedor;
import com.ferreteria.inventario.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@Tag(name = "4. Ventas", description = "Registro transaccional de ventas, historial y reportes. Registrar y consultar: ADMIN y USER. Reportes: solo ADMIN.")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Sin token, o token inválido/expirado", content = @Content),
        @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación", content = @Content)
})
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    // El vendedor se toma del usuario autenticado (viene del token JWT)
    @Operation(summary = "Registrar venta",
            description = """
                    Roles: ADMIN, USER. El vendedor se toma del usuario del token.
                    Es transaccional: si un producto no tiene stock suficiente no se descuenta nada (rollback).""")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Productos y cantidades de la venta",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            [
                              { "productoId": 1, "cantidad": 2 },
                              { "productoId": 2, "cantidad": 1 }
                            ]""")))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venta registrada"),
            @ApiResponse(responseCode = "400", description = "Venta vacía o cantidad inválida", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe alguno de los productos", content = @Content),
            @ApiResponse(responseCode = "409", description = "Stock insuficiente: se revierte toda la venta", content = @Content)
    })
    @PostMapping
    public ResponseEntity<VentaResponse> registrarVenta(@RequestBody List<VentaItemRequest> items,
                                                        Principal principal) {
        VentaResponse venta = ventaService.registrarVenta(items, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(venta);
    }

    @Operation(summary = "Listar ventas (paginado)", description = "Historial de ventas. Roles: ADMIN, USER.")
    @ApiResponse(responseCode = "200", description = "Página de ventas")
    @GetMapping
    public PaginaResponse<VentaResponse> listar(
            @Parameter(description = "Número de página (empieza en 0)", example = "0")
            @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Ventas por página", example = "5")
            @RequestParam(defaultValue = "5") int tamanio) {
        return ventaService.listarVentas(pagina, tamanio);
    }

    @Operation(summary = "Obtener venta por id", description = "Incluye el detalle de productos. Roles: ADMIN, USER.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe la venta", content = @Content)
    })
    @GetMapping("/{id}")
    public VentaResponse buscarPorId(
            @Parameter(description = "Id de la venta", example = "1") @PathVariable Long id) {
        return ventaService.buscarPorId(id);
    }

    @Operation(summary = "Ventas entre dos fechas", description = "Fechas en formato AAAA-MM-DD. Roles: ADMIN, USER.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ventas del rango"),
            @ApiResponse(responseCode = "400", description = "Rango de fechas inválido", content = @Content)
    })
    @GetMapping("/por-fechas")
    public List<VentaResponse> porFechas(
            @Parameter(description = "Fecha inicial", example = "2026-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @Parameter(description = "Fecha final", example = "2026-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ventaService.buscarPorFechas(desde, hasta);
    }

    // ---- Reportes (solo ADMIN, ver SecurityConfig) ----

    @Operation(summary = "Reporte: productos más vendidos", description = "Rol: solo ADMIN.")
    @ApiResponse(responseCode = "200", description = "Ranking de productos más vendidos")
    @GetMapping("/reportes/mas-vendidos")
    public List<ProductoMasVendido> masVendidos(
            @Parameter(description = "Cantidad de productos del ranking", example = "5")
            @RequestParam(defaultValue = "5") int limite) {
        return ventaService.productosMasVendidos(limite);
    }

    @Operation(summary = "Reporte: total vendido por vendedor", description = "Rol: solo ADMIN.")
    @ApiResponse(responseCode = "200", description = "Cantidad de ventas y monto total por vendedor")
    @GetMapping("/reportes/por-vendedor")
    public List<VentasPorVendedor> porVendedor() {
        return ventaService.totalPorVendedor();
    }
}
