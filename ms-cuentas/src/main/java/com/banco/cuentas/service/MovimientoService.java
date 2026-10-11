package com.banco.cuentas.service;

import com.banco.cuentas.dto.MovimientoRequest;
import com.banco.cuentas.dto.MovimientoResponse;
import com.banco.cuentas.dto.MovimientoUpdateRequest;

import java.util.List;

public interface MovimientoService {

    MovimientoResponse registrar(MovimientoRequest request);

    List<MovimientoResponse> listar(Integer numeroCuenta);

    MovimientoResponse obtener(Integer id);

    MovimientoResponse actualizar(Integer id, MovimientoUpdateRequest request);

    void eliminar(Integer id);
}