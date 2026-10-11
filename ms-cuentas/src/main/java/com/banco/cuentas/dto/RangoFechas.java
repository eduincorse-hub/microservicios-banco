package com.banco.cuentas.dto;

import com.banco.cuentas.exception.ParametroInvalidoException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public record RangoFechas(LocalDate inicio, LocalDate fin) {

    private static final String FORMATO = "fechaInicio,fechaFin (yyyy-MM-dd,yyyy-MM-dd)";

    /** Convierte "2026-10-01,2026-10-11" en un rango validado. */
    public static RangoFechas parse(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ParametroInvalidoException("El parámetro 'fecha' es obligatorio con el formato " + FORMATO);
        }

        String[] partes = valor.split(",");
        if (partes.length != 2) {
            throw new ParametroInvalidoException("El parámetro 'fecha' debe tener el formato " + FORMATO);
        }

        LocalDate inicio;
        LocalDate fin;
        try {
            inicio = LocalDate.parse(partes[0].trim());
            fin = LocalDate.parse(partes[1].trim());
        } catch (DateTimeParseException ex) {
            throw new ParametroInvalidoException("Fecha inválida en '" + valor + "'. Formato esperado: " + FORMATO);
        }

        if (inicio.isAfter(fin)) {
            throw new ParametroInvalidoException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        return new RangoFechas(inicio, fin);
    }

    /** Inicio del primer día (00:00:00). */
    public LocalDateTime desde() {
        return inicio.atStartOfDay();
    }

    /** Inicio del día SIGUIENTE al fin: así el último día se incluye completo. */
    public LocalDateTime hastaExclusivo() {
        return fin.plusDays(1).atStartOfDay();
    }
}