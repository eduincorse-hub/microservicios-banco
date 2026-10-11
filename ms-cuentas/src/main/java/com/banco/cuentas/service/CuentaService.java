package com.banco.cuentas.service;

import com.banco.cuentas.dto.CuentaPatchRequest;
import com.banco.cuentas.dto.CuentaRequest;
import com.banco.cuentas.dto.CuentaResponse;
import com.banco.cuentas.dto.CuentaUpdateRequest;

import java.util.List;

public interface CuentaService {

    CuentaResponse crear(CuentaRequest request);

    List<CuentaResponse> listar(String clienteId);

    CuentaResponse obtener(Integer numeroCuenta);

    CuentaResponse actualizar(Integer numeroCuenta, CuentaUpdateRequest request);

    CuentaResponse actualizarParcial(Integer numeroCuenta, CuentaPatchRequest request);

    void eliminar(Integer numeroCuenta);
}