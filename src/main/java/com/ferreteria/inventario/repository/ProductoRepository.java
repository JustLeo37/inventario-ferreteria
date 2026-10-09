package com.ferreteria.inventario.repository;

import com.ferreteria.inventario.model.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Productos de una categoría (navega la relación: p.categoriaRelacionada.nombre)
    @Query("SELECT p FROM Producto p WHERE p.categoriaRelacionada.nombre = :nombre ORDER BY p.nombre")
    List<Producto> buscarPorNombreCategoria(@Param("nombre") String nombre);

    // Productos por reponer (stock menor o igual al umbral)
    @Query("SELECT p FROM Producto p WHERE p.stock <= :umbral ORDER BY p.stock ASC, p.nombre ASC")
    List<Producto> buscarStockBajo(@Param("umbral") Integer umbral);

    // Búsqueda por texto, sin importar mayúsculas
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<Producto> buscarPorNombre(@Param("texto") String texto);

    // Valor total del inventario (suma de precio × stock)
    @Query("SELECT COALESCE(SUM(p.precio * p.stock), 0.0) FROM Producto p")
    Double calcularValorInventario();

    // Bloqueo pesimista: evita que dos ventas simultáneas descuenten el mismo stock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> buscarParaActualizar(@Param("id") Long id);
}