package com.ferreteria.inventario.service;

import com.ferreteria.inventario.dto.VentaItemRequest;
import com.ferreteria.inventario.model.Producto;
import com.ferreteria.inventario.repository.ProductoRepository;
import com.ferreteria.inventario.repository.VentaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Pruebas del punto Soft Delete sobre el flujo principal de ventas (VentaService)
class VentaServiceTest {

    @Test
    void registrarVenta_debeRechazarProductoDesactivado() {

        // Arrange
        VentaRepository ventaRepository = Mockito.mock(VentaRepository.class);
        ProductoRepository productoRepository = Mockito.mock(ProductoRepository.class);
        VentaService ventaService = new VentaService(ventaRepository, productoRepository);

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Martillo");
        producto.setPrecio(25.50);
        producto.setStock(10);
        producto.setActivo(false); // desactivado con soft delete

        Mockito.when(productoRepository.buscarParaActualizar(1L))
                .thenReturn(Optional.of(producto));

        // Act & Assert
        ResponseStatusException excepcion = assertThrows(
            ResponseStatusException.class,
            () -> ventaService.registrarVenta(List.of(new VentaItemRequest(1L, 2)), "admin")
        );

        assertEquals(HttpStatus.CONFLICT, excepcion.getStatusCode());
        // No se descontó stock ni se guardó ninguna venta
        assertEquals(10, producto.getStock());
        Mockito.verify(ventaRepository, Mockito.never()).save(Mockito.any());
    }
}
