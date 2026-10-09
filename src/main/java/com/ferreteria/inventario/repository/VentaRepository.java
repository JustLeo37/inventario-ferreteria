package com.ferreteria.inventario.repository;

import com.ferreteria.inventario.dto.ProductoMasVendido;
import com.ferreteria.inventario.dto.VentasPorVendedor;
import com.ferreteria.inventario.model.Venta;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    // Ventas dentro de un rango de fechas
    @Query("SELECT v FROM Venta v WHERE v.fecha >= :desde AND v.fecha < :hasta ORDER BY v.fecha DESC")
    List<Venta> buscarPorRangoFechas(@Param("desde") LocalDateTime desde,
                                     @Param("hasta") LocalDateTime hasta);

    // Ranking de productos más vendidos (el límite se controla con Pageable)
    @Query("SELECT p.nombre AS producto, SUM(d.cantidad) AS cantidadVendida "
            + "FROM DetalleVenta d JOIN d.producto p "
            + "GROUP BY p.id, p.nombre ORDER BY SUM(d.cantidad) DESC")
    List<ProductoMasVendido> productosMasVendidos(Pageable pageable);

    // Total vendido por cada vendedor
    @Query("SELECT v.vendedor AS vendedor, COUNT(v) AS cantidadVentas, SUM(v.total) AS totalVendido "
            + "FROM Venta v GROUP BY v.vendedor ORDER BY SUM(v.total) DESC")
    List<VentasPorVendedor> totalPorVendedor();
}