package com.ferreteria.inventario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar un usuario nuevo (rol USER)")
public record RegisterRequest(
        @Schema(description = "Nombre de usuario (3 a 50 caracteres)", example = "cliente1")
        @NotBlank @Size(min = 3, max = 50) String username,
        @Schema(description = "Contraseña (6 a 100 caracteres)", example = "cliente123")
        @NotBlank @Size(min = 6, max = 100) String password) {
}
