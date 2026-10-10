package com.banco.clientes.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ClientePatchRequest(

        @Size(min = 1, max = 100, message = "El nombre debe tener entre 1 y 100 caracteres")
        String nombre,

        @Pattern(regexp = "(?i)^(masculino|femenino|otro)$",
                 message = "El género debe ser Masculino, Femenino u Otro")
        String genero,

        @PositiveOrZero(message = "La edad no puede ser negativa")
        @Max(value = 120, message = "La edad no puede superar 120")
        Integer edad,

        @Size(min = 1, max = 20, message = "La identificación debe tener entre 1 y 20 caracteres")
        String identificacion,

        @Size(max = 255, message = "La dirección no puede superar 255 caracteres")
        String direccion,

        @Pattern(regexp = "^\\d{7,15}$", message = "El teléfono debe tener entre 7 y 15 dígitos")
        String telefono,

        @Size(min = 4, max = 50, message = "La contraseña debe tener entre 4 y 50 caracteres")
        String contrasena,

        Boolean estado
) {
}