package com.banco.clientes.mapper;

import com.banco.clientes.dto.ClientePatchRequest;
import com.banco.clientes.dto.ClienteRequest;
import com.banco.clientes.dto.ClienteResponse;
import com.banco.clientes.entity.Cliente;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper
public interface ClienteMapper {

    /** POST: crea una entidad nueva a partir del request. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clienteId", ignore = true)    // lo genera la entidad (@PrePersist)
    @Mapping(target = "contrasena", ignore = true)   // la cifra el servicio
    @Mapping(target = "estado", defaultValue = "true")
    Cliente toEntity(ClienteRequest request);

    /** Entidad → respuesta (sin contraseña). */
    ClienteResponse toResponse(Cliente cliente);

    /** PUT: reemplaza todos los datos editables de un cliente existente. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clienteId", ignore = true)
    @Mapping(target = "contrasena", ignore = true)
    @Mapping(target = "estado", defaultValue = "true")
    void updateEntity(ClienteRequest request, @MappingTarget Cliente cliente);

    /** PATCH: solo copia los campos que no son null. */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clienteId", ignore = true)
    @Mapping(target = "contrasena", ignore = true)
    void patchEntity(ClientePatchRequest request, @MappingTarget Cliente cliente);
}