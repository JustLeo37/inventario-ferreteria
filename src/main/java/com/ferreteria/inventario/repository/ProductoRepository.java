package com.ferreteria.inventario.repository;

import com.ferreteria.inventario.model.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByActivoTrue();

    Page<Producto> findByActivoTrue(Pageable pageable);
    Optional<Producto> findByIdAndActivoTrue(Long id);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.categoriaRelacionada.nombre = :nombre ORDER BY p.nombre")
List<Producto> buscarPorNombreCategoria(@Param("nombre") String nombre);

// Productos por reponer (stock menor o igual al umbral), solo activos
@Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stock <= :umbral ORDER BY p.stock ASC, p.nombre ASC")
List<Producto> buscarStockBajo(@Param("umbral") Integer umbral);

// Búsqueda por texto, sin importar mayúsculas, solo activos
@Query("SELECT p FROM Producto p WHERE p.activo = true AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))")
List<Producto> buscarPorNombre(@Param("texto") String texto);

// Valor total del inventario (suma de precio × stock), solo productos activos
@Query("SELECT COALESCE(SUM(p.precio * p.stock), 0.0) FROM Producto p WHERE p.activo = true")
Double calcularValorInventario();



    // Bloqueo pesimista: evita que dos ventas simultáneas descuenten el mismo stock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> buscarParaActualizar(@Param("id") Long id);
}