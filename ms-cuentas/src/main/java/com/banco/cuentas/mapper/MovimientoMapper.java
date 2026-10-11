package com.banco.cuentas.mapper;

import com.banco.cuentas.dto.MovimientoResponse;
import com.banco.cuentas.entity.Movimiento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface MovimientoMapper {

    @Mapping(target = "numeroCuenta", source = "cuenta.numeroCuenta")
    MovimientoResponse toResponse(Movimiento movimiento);
}