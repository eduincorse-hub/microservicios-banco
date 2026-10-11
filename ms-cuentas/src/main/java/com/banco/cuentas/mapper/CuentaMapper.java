package com.banco.cuentas.mapper;

import com.banco.cuentas.dto.CuentaPatchRequest;
import com.banco.cuentas.dto.CuentaResponse;
import com.banco.cuentas.dto.CuentaUpdateRequest;
import com.banco.cuentas.entity.Cuenta;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CuentaMapper {

    @Mapping(target = "clienteId", source = "cliente.clienteId")
    @Mapping(target = "cliente", source = "cliente.nombre")
    CuentaResponse toResponse(Cuenta cuenta);

    /** PUT: solo tipo y estado. */
    void updateEntity(CuentaUpdateRequest request, @MappingTarget Cuenta cuenta);

    /** PATCH: solo los campos no nulos. */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patchEntity(CuentaPatchRequest request, @MappingTarget Cuenta cuenta);
}