package com.ferreteria.inventario.service;

import com.ferreteria.inventario.model.Producto;
import com.ferreteria.inventario.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductoServiceTest {

    @Test
    void buscarPorId_debeDevolverProductoExistente() { //Prueba 1

        // Arrange
        ProductoRepository productoRepository = Mockito.mock(ProductoRepository.class);
        ProductoService productoService = new ProductoService(productoRepository);

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Martillo");
        producto.setCategoria("Herramientas");
        producto.setMarca("Stanley");
        producto.setPrecio(25.50);
        producto.setStock(10);

        Mockito.when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        // Act
        Optional<Producto> resultado = productoService.buscarPorId(1L);

        // Assert
        assertEquals("Martillo", resultado.get().getNombre());
    }

    @Test
    void buscarPorId_debeDevolverVacioSiNoExiste() { //Prueba 2

        // Arrange
        ProductoRepository productoRepository = Mockito.mock(ProductoRepository.class);
        ProductoService productoService = new ProductoService(productoRepository);

        Mockito.when(productoRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act
        Optional<Producto> resultado = productoService.buscarPorId(999L);

        // Assert
        assertEquals(Optional.empty(), resultado);
    }

    @Test
    void listarProductos_debeDevolverListaDeProductos() { //Prueba 3

        // Arrange
        ProductoRepository productoRepository = Mockito.mock(ProductoRepository.class);
        ProductoService productoService = new ProductoService(productoRepository);

        Producto producto1 = new Producto();
        producto1.setId(1L);
        producto1.setNombre("Martillo");

        Producto producto2 = new Producto();
        producto2.setId(2L);
        producto2.setNombre("Destornillador");

        Mockito.when(productoRepository.findAll())
                .thenReturn(List.of(producto1, producto2));

        // Act
        List<Producto> resultado = productoService.listarProductos();

        // Assert
        assertEquals(2, resultado.size());
        assertEquals("Martillo", resultado.get(0).getNombre());
        assertEquals("Destornillador", resultado.get(1).getNombre());
    }

    @Test
    void guardarProducto_debeGuardarYDevolverProducto() { //Prueba 4

        // Arrange
        ProductoRepository productoRepository = Mockito.mock(ProductoRepository.class);
        ProductoService productoService = new ProductoService(productoRepository);

        Producto producto = new Producto();
        producto.setNombre("Taladro");
        producto.setCategoria("Herramientas");
        producto.setMarca("Bosch");
        producto.setPrecio(150.00);
        producto.setStock(5);

        Mockito.when(productoRepository.save(producto))
                .thenReturn(producto);

        // Act
        Producto resultado = productoService.guardarProducto(producto);

        // Assert
        assertEquals("Taladro", resultado.getNombre());
        assertEquals("Bosch", resultado.getMarca());
        assertEquals(150.00, resultado.getPrecio());
        assertEquals(5, resultado.getStock());
    }

    @Test
    void eliminarProducto_debeEliminarProductoPorId() { //Prueba 5

        // Arrange
        ProductoRepository productoRepository = Mockito.mock(ProductoRepository.class);
        ProductoService productoService = new ProductoService(productoRepository);

        // Act
        productoService.eliminarProducto(1L);

        // Assert
        Mockito.verify(productoRepository).deleteById(1L);
    }
}