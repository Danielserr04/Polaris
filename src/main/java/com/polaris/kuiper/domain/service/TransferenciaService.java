package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.in.CreateTransferenciaInterface;
import com.polaris.kuiper.application.in.DeleteTransferenciaInterface;
import com.polaris.kuiper.application.in.GetTransferenciaInterface;
import com.polaris.kuiper.application.in.ListTransferenciaInterface;
import com.polaris.kuiper.application.in.UpdateTransferenciaInterface;
import com.polaris.kuiper.application.out.CuentaRepositoryPort;
import com.polaris.kuiper.application.out.TransferenciaRepositoryPort;
import com.polaris.kuiper.domain.model.CuentaNotFoundException;
import com.polaris.kuiper.domain.model.Transferencia;
import com.polaris.kuiper.domain.model.TransferenciaFilter;
import com.polaris.kuiper.domain.model.TransferenciaNotFoundException;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Las dos cuentas tienen que ser del usuario (404 si no) y distintas (400).
 * Una transferencia no es ni ingreso ni gasto: solo mueve saldo, y el resumen
 * mensual no la ve. Ver docs/decisiones/039-cuentas-y-transferencias.md.
 */
@Service
@RequiredArgsConstructor
public class TransferenciaService implements
        CreateTransferenciaInterface,
        GetTransferenciaInterface,
        ListTransferenciaInterface,
        UpdateTransferenciaInterface,
        DeleteTransferenciaInterface {

    private final TransferenciaRepositoryPort repository;
    private final CuentaRepositoryPort cuentaRepository;

    @Override
    public Transferencia create(Long usuarioId, Transferencia transferencia) {
        validarCuentas(usuarioId, transferencia);
        transferencia.setId(null);
        transferencia.setUsuarioId(usuarioId);
        return repository.save(transferencia);
    }

    @Override
    public Transferencia get(Long usuarioId, Long id) {
        return getPropia(usuarioId, id);
    }

    @Override
    public List<Transferencia> list(Long usuarioId, TransferenciaFilter filter) {
        return repository.findAll(usuarioId, filter);
    }

    @Override
    public Transferencia update(Long usuarioId, Long id, Transferencia transferencia) {
        Transferencia existente = getPropia(usuarioId, id);
        validarCuentas(usuarioId, transferencia);
        transferencia.setId(existente.getId());
        transferencia.setUsuarioId(existente.getUsuarioId());
        return repository.save(transferencia);
    }

    @Override
    public void delete(Long usuarioId, Long id) {
        getPropia(usuarioId, id);
        repository.deleteById(id);
    }

    /**
     * 400 si origen y destino son la misma cuenta; 404 si alguna no existe o
     * es de otro usuario (no se distingue, por lo mismo que en getPropia).
     */
    private void validarCuentas(Long usuarioId, Transferencia transferencia) {
        if (transferencia.getCuentaOrigenId().equals(transferencia.getCuentaDestinoId())) {
            throw new ValidationException("La cuenta de origen y la de destino tienen que ser distintas");
        }
        validarCuenta(usuarioId, transferencia.getCuentaOrigenId());
        validarCuenta(usuarioId, transferencia.getCuentaDestinoId());
    }

    private void validarCuenta(Long usuarioId, Long cuentaId) {
        cuentaRepository.findById(cuentaId)
                .filter(c -> c.getUsuarioId().equals(usuarioId))
                .orElseThrow(() -> new CuentaNotFoundException(cuentaId));
    }

    /**
     * 404, no 403, si el id existe pero pertenece a otro usuario: un 403
     * confirmaria que ese id existe.
     */
    private Transferencia getPropia(Long usuarioId, Long id) {
        Transferencia transferencia = repository.findById(id)
                .orElseThrow(() -> new TransferenciaNotFoundException(id));

        if (!transferencia.getUsuarioId().equals(usuarioId)) {
            throw new TransferenciaNotFoundException(id);
        }

        return transferencia;
    }
}
