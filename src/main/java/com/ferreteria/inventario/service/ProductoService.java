package com.ferreteria.inventario.service;

import com.ferreteria.inventario.model.Producto;
import com.ferreteria.inventario.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import com.ferreteria.inventario.model.Categoria;
import com.ferreteria.inventario.repository.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    // Inyección de dependencias por constructor
    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository) {

        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // Listar todos los productos
    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    // Buscar un producto por su ID
    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    // Guardar un producto y validar su categoría
    public Producto guardarProducto(Producto producto) {

        if (producto.getCategoriaRelacionada() != null) {

            Long categoriaId = producto.getCategoriaRelacionada().getId();

            if (categoriaId == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Debe proporcionar el ID de la categoria"
                );
            }

            Categoria categoria = categoriaRepository.findById(categoriaId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "La categoria indicada no existe"
                    ));

            producto.setCategoriaRelacionada(categoria);
            producto.setCategoria(categoria.getNombre());
        }

        return productoRepository.save(producto);
    }

    // Eliminar un producto por su ID
    public void eliminarProducto(Long id) {
        productoRepository.deleteById(id);
    }
}
