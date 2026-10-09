package com.ferreteria.inventario.controller;

import com.ferreteria.inventario.dto.PaginaResponse;
import com.ferreteria.inventario.dto.ProductoMasVendido;
import com.ferreteria.inventario.dto.VentaItemRequest;
import com.ferreteria.inventario.dto.VentaResponse;
import com.ferreteria.inventario.dto.VentasPorVendedor;
import com.ferreteria.inventario.service.VentaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    // El vendedor se toma del usuario autenticado (viene del token JWT)
    @PostMapping
    public ResponseEntity<VentaResponse> registrarVenta(@RequestBody List<VentaItemRequest> items,
                                                        Principal principal) {
        VentaResponse venta = ventaService.registrarVenta(items, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(venta);
    }

    @GetMapping
    public PaginaResponse<VentaResponse> listar(@RequestParam(defaultValue = "0") int pagina,
                                                @RequestParam(defaultValue = "5") int tamanio) {
        return ventaService.listarVentas(pagina, tamanio);
    }

    @GetMapping("/{id}")
    public VentaResponse buscarPorId(@PathVariable Long id) {
        return ventaService.buscarPorId(id);
    }

    @GetMapping("/por-fechas")
    public List<VentaResponse> porFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ventaService.buscarPorFechas(desde, hasta);
    }

    // ---- Reportes (solo ADMIN, ver SecurityConfig) ----

    @GetMapping("/reportes/mas-vendidos")
    public List<ProductoMasVendido> masVendidos(@RequestParam(defaultValue = "5") int limite) {
        return ventaService.productosMasVendidos(limite);
    }

    @GetMapping("/reportes/por-vendedor")
    public List<VentasPorVendedor> porVendedor() {
        return ventaService.totalPorVendedor();
    }
}