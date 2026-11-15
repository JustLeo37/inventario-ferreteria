package com.ferreteria.inventario.model;

import jakarta.persistence.EntityListeners;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;


@Entity
@Table(name = "productos")
@EntityListeners(AuditingEntityListener.class)

public class Producto {

       @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    // Se completa automáticamente desde la categoría relacionada (ver ProductoService)
    private String categoria;

    @NotBlank(message = "La marca es obligatoria")
    private String marca;

    @NotNull(message = "El precio es obligatorio")
    @PositiveOrZero(message = "El precio no puede ser negativo")
    private Double precio;

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stock;

    @Column(nullable = false, columnDefinition = "boolean default true")
     private Boolean activo = true;
    

    public Long getId() {
        return id;
    }
     public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    
    public Boolean getActivo() {
    return activo;
    }

    public void setActivo(Boolean activo) {
    this.activo = activo;
    }

    public boolean isActivo() {
    return !Boolean.FALSE.equals(activo);
    }


@ManyToOne
@JoinColumn(name = "categoria_id")
private Categoria categoriaRelacionada;

public Categoria getCategoriaRelacionada() {
    return categoriaRelacionada;
}

public void setCategoriaRelacionada(Categoria categoriaRelacionada) {
    this.categoriaRelacionada = categoriaRelacionada;
}

// Auditoría: usuario que creó el producto
@CreatedBy
@Column(name = "created_by", updatable = false, length = 50)
private String createdBy;

// Auditoría: fecha y hora de creación
@CreatedDate
@Column(name = "created_date", updatable = false)
private LocalDateTime createdDate;

public String getCreatedBy() {
    return createdBy;
}

public LocalDateTime getCreatedDate() {
    return createdDate;
}
}