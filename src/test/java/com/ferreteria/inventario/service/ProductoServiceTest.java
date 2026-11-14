package com.ferreteria.inventario.service;

import com.ferreteria.inventario.dto.VentaItemRequest;
import com.ferreteria.inventario.model.Categoria;
import com.ferreteria.inventario.model.Producto;
import com.ferreteria.inventario.repository.CategoriaRepository;
import com.ferreteria.inventario.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductoServiceTest {

    @Test
    void buscarPorId_debeDevolverProductoExistente() { //Prueba 1

        // Arrange
        ProductoRepository productoRepository = Mockito.mock(ProductoRepository.class);
        CategoriaRepository categoriaRepository = Mockito.mock(CategoriaRepository.class);
        ProductoService productoService = new ProductoService(productoRepository, categoriaRepository);

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Martillo");
        producto.setCategoria("Herramientas");
        producto.setMarca("Stanley");
        producto.setPrecio(25.50);
        producto.setStock(10);

        Mockito.when(productoRepository.findByIdAndActivoTrue(1L))
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
        CategoriaRepository categoriaRepository = Mockito.mock(CategoriaRepository.class);
        ProductoService productoService = new ProductoService(productoRepository, categoriaRepository);

        Mockito.when(productoRepository.findByIdAndActivoTrue(999L))
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
        CategoriaRepository categoriaRepository = Mockito.mock(CategoriaRepository.class);
        ProductoService productoService = new ProductoService(productoRepository, categoriaRepository);

        Producto producto1 = new Producto();
        producto1.setId(1L);
        producto1.setNombre("Martillo");

        Producto producto2 = new Producto();
        producto2.setId(2L);
        producto2.setNombre("Destornillador");

        Mockito.when(productoRepository.findByActivoTrue())
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
        CategoriaRepository categoriaRepository = Mockito.mock(CategoriaRepository.class);
        ProductoService productoService = new ProductoService(productoRepository, categoriaRepository);

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
void eliminarProducto_debeDesactivarSinBorrarFisicamente() { // Prueba 5

    // Arrange
    ProductoRepository productoRepository = Mockito.mock(ProductoRepository.class);
    CategoriaRepository categoriaRepository = Mockito.mock(CategoriaRepository.class);
    ProductoService productoService = new ProductoService(productoRepository, categoriaRepository);

    Producto producto = new Producto();
    producto.setActivo(true);
    Mockito.when(productoRepository.findById(3L)).thenReturn(Optional.of(producto));

    // Act
    productoService.eliminarProducto(3L);

    // Assert
    assertEquals(false, producto.getActivo());
    Mockito.verify(productoRepository).save(producto);
    Mockito.verify(productoRepository, Mockito.never()).deleteById(Mockito.anyLong());
}


    //Sexta prueba unitaria
    @Test
    void guardarProducto_debeRechazarCategoriaInexistente() {
        //Arrange
        ProductoRepository productoRepository = Mockito.mock(ProductoRepository.class);
        CategoriaRepository categoriaRepository = Mockito.mock(CategoriaRepository.class);
        ProductoService productoService = new ProductoService(productoRepository, categoriaRepository);

        Categoria categoria = new Categoria();
        categoria.setId(999L); // ID de categoría que no existe

        Producto producto = new Producto();
        producto.setNombre("Sierra");
        producto.setCategoriaRelacionada(categoria);

        Mockito.when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ResponseStatusException excepcion = assertThrows(
            ResponseStatusException.class,() -> productoService.guardarProducto(producto)
        );

        assertEquals(HttpStatus.BAD_REQUEST, excepcion.getStatusCode());
        // Assert
        Mockito.verify(productoRepository, Mockito.never()).save(Mockito.any(Producto.class));
    }

    @Test
void registrarVenta_debeRechazarProductoDesactivado() { // Prueba 7

    // Arrange
    ProductoRepository productoRepository = Mockito.mock(ProductoRepository.class);
    CategoriaRepository categoriaRepository = Mockito.mock(CategoriaRepository.class);
    ProductoService productoService = new ProductoService(productoRepository, categoriaRepository);

    Producto producto = new Producto();
    producto.setNombre("Martillo");
    producto.setStock(10);
    producto.setActivo(false);
    Mockito.when(productoRepository.findById(5L)).thenReturn(Optional.of(producto));

    // Act + Assert
    org.springframework.web.server.ResponseStatusException error =
            assertThrows(org.springframework.web.server.ResponseStatusException.class,
                    () -> productoService.registrarVenta(
                            List.of(new VentaItemRequest(5L, 1))));

    assertEquals(409, error.getStatusCode().value());
    assertEquals(10, producto.getStock());
}

}