package com.ferreteria.inventario.controller;

import com.ferreteria.inventario.model.Producto;
import com.ferreteria.inventario.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Prueba del punto Soft Delete: el PUT no debe reactivar productos desactivados
class ProductoControllerTest {

    @Test
    void actualizarProducto_noDebeReactivarUnProductoDesactivado() {

        // Arrange
        ProductoService productoService = Mockito.mock(ProductoService.class);
        ProductoController productoController = new ProductoController(productoService);

        Producto existente = new Producto();
        existente.setId(3L);
        existente.setNombre("Martillo");
        existente.setActivo(false); // desactivado con soft delete

        Producto datos = new Producto();
        datos.setNombre("Martillo pro");
        datos.setCategoria("Herramientas");
        datos.setMarca("Stanley");
        datos.setPrecio(30.00);
        datos.setStock(8);
        datos.setActivo(true); // aunque el body mande activo = true...

        Mockito.when(productoService.buscarPorId(3L)).thenReturn(Optional.of(existente));
        Mockito.when(productoService.guardarProducto(existente)).thenReturn(existente);

        // Act
        ResponseEntity<Producto> respuesta = productoController.actualizarProducto(3L, datos);

        // Assert: se actualizó el nombre, pero NO se reactivó
        assertEquals("Martillo pro", respuesta.getBody().getNombre());
        assertEquals(false, respuesta.getBody().getActivo());
    }
}
