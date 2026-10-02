package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.in.CreateCuentaInterface;
import com.polaris.kuiper.application.in.DeleteCuentaInterface;
import com.polaris.kuiper.application.in.GetCuentaInterface;
import com.polaris.kuiper.application.in.GetPatrimonioInterface;
import com.polaris.kuiper.application.in.ListCuentaInterface;
import com.polaris.kuiper.application.in.UpdateCuentaInterface;
import com.polaris.kuiper.application.out.CuentaRepositoryPort;
import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.application.out.RecurrenteRepositoryPort;
import com.polaris.kuiper.application.out.TransferenciaRepositoryPort;
import com.polaris.kuiper.domain.model.Cuenta;
import com.polaris.kuiper.domain.model.CuentaFilter;
import com.polaris.kuiper.domain.model.CuentaNotFoundException;
import com.polaris.kuiper.domain.model.Patrimonio;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.shared.error.DuplicateResourceException;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Nombre unico por usuario. El saldo actual no se guarda: se calcula en cada
 * lectura como
 * {@code saldoInicial + ingresos - gastos + transferencias entrantes - salientes},
 * con cuatro SUM agrupados por cuenta (dos sobre movimiento, dos sobre
 * transferencia) que valen para todas las cuentas del usuario a la vez. Asi
 * el listado cuesta lo mismo con 2 cuentas que con 20, y el saldo nunca se
 * desincroniza al editar o borrar un movimiento. Ver
 * docs/decisiones/039-cuentas-y-transferencias.md.
 *
 * <p>Una cuenta con movimientos, recurrentes o transferencias no se borra
 * (400): se archiva.
 */
@Service
@RequiredArgsConstructor
public class CuentaService implements
        CreateCuentaInterface,
        GetCuentaInterface,
        ListCuentaInterface,
        UpdateCuentaInterface,
        DeleteCuentaInterface,
        GetPatrimonioInterface {

    /** Con escala 2 para que los saldos salgan siempre como 0.00 y no como 0. */
    private static final BigDecimal CERO = BigDecimal.ZERO.setScale(2);

    private final CuentaRepositoryPort repository;
    private final MovimientoRepositoryPort movimientoRepository;
    private final RecurrenteRepositoryPort recurrenteRepository;
    private final TransferenciaRepositoryPort transferenciaRepository;

    @Override
    public Cuenta create(Long usuarioId, Cuenta cuenta) {
        comprobarNombreLibre(usuarioId, cuenta, null);
        cuenta.setId(null);
        cuenta.setUsuarioId(usuarioId);
        Cuenta creada = repository.save(cuenta);
        creada.setSaldoActual(creada.getSaldoInicial().setScale(2));
        return creada;
    }

    @Override
    public Cuenta get(Long usuarioId, Long id) {
        Cuenta cuenta = getPropia(usuarioId, id);
        new Saldos(usuarioId).aplicar(cuenta);
        return cuenta;
    }

    @Override
    public List<Cuenta> list(Long usuarioId, CuentaFilter filter) {
        List<Cuenta> cuentas = repository.findAll(usuarioId, filter);
        if (!cuentas.isEmpty()) {
            Saldos saldos = new Saldos(usuarioId);
            cuentas.forEach(saldos::aplicar);
        }
        return cuentas;
    }

    @Override
    public Cuenta update(Long usuarioId, Long id, Cuenta cuenta) {
        Cuenta existente = getPropia(usuarioId, id);
        comprobarNombreLibre(usuarioId, cuenta, existente.getId());

        cuenta.setId(existente.getId());
        cuenta.setUsuarioId(existente.getUsuarioId());
        Cuenta actualizada = repository.save(cuenta);
        new Saldos(usuarioId).aplicar(actualizada);
        return actualizada;
    }

    @Override
    public void delete(Long usuarioId, Long id) {
        getPropia(usuarioId, id);

        if (movimientoRepository.existsByCuentaId(id)
                || recurrenteRepository.existsByCuentaId(id)
                || transferenciaRepository.existsByCuentaId(id)) {
            throw new ValidationException(
                    "No se puede borrar una cuenta que tiene movimientos, recurrentes o transferencias: archivala");
        }

        repository.deleteById(id);
    }

    @Override
    public Patrimonio get(Long usuarioId) {
        List<Cuenta> cuentas = list(usuarioId, CuentaFilter.builder().build());
        BigDecimal total = cuentas.stream().map(Cuenta::getSaldoActual).reduce(CERO, BigDecimal::add);
        return Patrimonio.builder().total(total).numeroCuentas(cuentas.size()).build();
    }

    /**
     * Las cuatro sumas por cuenta de un usuario, leidas una vez y aplicadas a
     * cuantas cuentas haga falta.
     */
    private final class Saldos {

        private final Map<Long, BigDecimal> ingresos;
        private final Map<Long, BigDecimal> gastos;
        private final Map<Long, BigDecimal> entrantes;
        private final Map<Long, BigDecimal> salientes;

        private Saldos(Long usuarioId) {
            ingresos = movimientoRepository.sumarPorCuenta(usuarioId, TipoMovimiento.INGRESO);
            gastos = movimientoRepository.sumarPorCuenta(usuarioId, TipoMovimiento.GASTO);
            entrantes = transferenciaRepository.sumarEntrantesPorCuenta(usuarioId);
            salientes = transferenciaRepository.sumarSalientesPorCuenta(usuarioId);
        }

        private void aplicar(Cuenta cuenta) {
            Long id = cuenta.getId();
            cuenta.setSaldoActual(cuenta.getSaldoInicial().setScale(2)
                    .add(ingresos.getOrDefault(id, CERO))
                    .subtract(gastos.getOrDefault(id, CERO))
                    .add(entrantes.getOrDefault(id, CERO))
                    .subtract(salientes.getOrDefault(id, CERO)));
        }
    }

    /** 409 si otra cuenta del usuario (distinta de {@code propiaId}) ya usa ese nombre. */
    private void comprobarNombreLibre(Long usuarioId, Cuenta cuenta, Long propiaId) {
        repository.findByUsuarioIdAndNombre(usuarioId, cuenta.getNombre())
                .filter(otra -> !otra.getId().equals(propiaId))
                .ifPresent(otra -> {
                    throw new DuplicateResourceException("Ya tienes una cuenta con ese nombre");
                });
    }

    /**
     * 404, no 403, si el id existe pero pertenece a otro usuario: un 403
     * confirmaria que ese id existe.
     */
    private Cuenta getPropia(Long usuarioId, Long id) {
        Cuenta cuenta = repository.findById(id)
                .orElseThrow(() -> new CuentaNotFoundException(id));

        if (!cuenta.getUsuarioId().equals(usuarioId)) {
            throw new CuentaNotFoundException(id);
        }

        return cuenta;
    }
}
