package com.ferreteria.inventario.controller;

import com.ferreteria.inventario.model.Producto;
import com.ferreteria.inventario.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Prueba del punto Soft Delete:
// el PUT no debe actualizar productos desactivados
class ProductoControllerTest {

    @Test
    void actualizarProducto_noDebeReactivarUnProductoDesactivado() {

        // Arrange: preparar los objetos necesarios
        ProductoService productoService =
                Mockito.mock(ProductoService.class);

        ProductoController productoController =
                new ProductoController(productoService);

        Producto datos = new Producto();
        datos.setNombre("Martillo pro");
        datos.setCategoria("Herramientas");
        datos.setMarca("Stanley");
        datos.setPrecio(30.00);
        datos.setStock(8);
        datos.setActivo(true); // Intentamos enviar activo = true

        // El producto está desactivado, por eso no aparece en la búsqueda
        Mockito.when(productoService.buscarPorId(3L))
                .thenReturn(Optional.empty());

        // Act: intentar actualizar el producto
        ResponseEntity<Producto> respuesta =
                productoController.actualizarProducto(3L, datos);

        // Assert: verificar que el controlador responda 404
        assertEquals(404, respuesta.getStatusCode().value());

        // Verificar que no se intente guardar el producto
        Mockito.verify(productoService, Mockito.never())
                .guardarProducto(Mockito.any(Producto.class));
    }
}