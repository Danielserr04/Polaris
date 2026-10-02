package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.in.CreateRecurrenteInterface;
import com.polaris.kuiper.application.in.DeleteRecurrenteInterface;
import com.polaris.kuiper.application.in.GenerarCargosRecurrentesInterface;
import com.polaris.kuiper.application.in.GetRecurrenteInterface;
import com.polaris.kuiper.application.in.ListRecurrenteInterface;
import com.polaris.kuiper.application.in.UpdateRecurrenteInterface;
import com.polaris.kuiper.application.out.CategoriaRepositoryPort;
import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.application.out.RecurrenteRepositoryPort;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.CategoriaNotFoundException;
import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.Recurrente;
import com.polaris.kuiper.domain.model.RecurrenteFilter;
import com.polaris.kuiper.domain.model.RecurrenteNotFoundException;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Cargos que se repiten y generan sus movimientos solos. Ver
 * docs/decisiones/034-recurrente-genera-movimientos.md.
 */
@Service
@RequiredArgsConstructor
public class RecurrenteService implements
        CreateRecurrenteInterface,
        GetRecurrenteInterface,
        ListRecurrenteInterface,
        UpdateRecurrenteInterface,
        DeleteRecurrenteInterface,
        GenerarCargosRecurrentesInterface {

    private final RecurrenteRepositoryPort repository;
    private final CategoriaRepositoryPort categoriaRepository;
    private final MovimientoRepositoryPort movimientoRepository;

    /**
     * El primer cargo es fechaInicio, aunque sea pasada: el job genera los
     * atrasados la proxima vez que pase. Asi se puede dar de alta una
     * suscripcion que empezo hace meses y queda el historico completo.
     */
    @Override
    public Recurrente create(Long usuarioId, Recurrente recurrente) {
        validarCategoria(usuarioId, recurrente);
        recurrente.setId(null);
        recurrente.setUsuarioId(usuarioId);
        recurrente.setCuotasPagadas(0);
        recurrente.setProximaFecha(recurrente.getFechaInicio());
        return repository.save(recurrente);
    }

    @Override
    public Recurrente get(Long usuarioId, Long id) {
        return getPropio(usuarioId, id);
    }

    @Override
    public List<Recurrente> list(Long usuarioId, RecurrenteFilter filter) {
        return repository.findAll(usuarioId, filter);
    }

    /**
     * Las cuotas pagadas no se tocan. El proximo cargo se recalcula si cambia
     * fechaInicio o si se reactiva uno pausado, y en los dos casos desde hoy:
     * reactivar no cobra el tiempo que estuvo en pausa.
     */
    @Override
    public Recurrente update(Long usuarioId, Long id, Recurrente recurrente) {
        Recurrente existente = getPropio(usuarioId, id);
        validarCategoria(usuarioId, recurrente);

        recurrente.setId(existente.getId());
        recurrente.setUsuarioId(existente.getUsuarioId());
        recurrente.setCuotasPagadas(existente.getCuotasPagadas());

        boolean cambiaInicio = !recurrente.getFechaInicio().equals(existente.getFechaInicio());
        boolean reactiva = recurrente.isActivo() && !existente.isActivo();
        if (cambiaInicio || reactiva) {
            recurrente.setProximaFecha(recurrente.primeroDesde(LocalDate.now()));
        } else {
            recurrente.setProximaFecha(existente.getProximaFecha());
        }

        if (recurrente.plazosTerminados()) {
            recurrente.setActivo(false);
        }
        return repository.save(recurrente);
    }

    /** Los movimientos ya generados se quedan: son gastos que ocurrieron. */
    @Override
    public void delete(Long usuarioId, Long id) {
        getPropio(usuarioId, id);
        repository.deleteById(id);
    }

    @Override
    public int generar(LocalDate hoy) {
        int creados = 0;
        for (Recurrente recurrente : repository.findPendientes(hoy)) {
            creados += generarPendientes(recurrente, hoy);
            repository.save(recurrente);
        }
        return creados;
    }

    /** Uno por cada cargo atrasado, no solo el ultimo: si el job no corrio en dias, no se pierde ninguno. */
    private int generarPendientes(Recurrente recurrente, LocalDate hoy) {
        int creados = 0;
        while (recurrente.isActivo() && !recurrente.getProximaFecha().isAfter(hoy)) {
            movimientoRepository.save(movimientoDe(recurrente));
            creados++;
            recurrente.setCuotasPagadas(recurrente.getCuotasPagadas() + 1);
            recurrente.setProximaFecha(recurrente.siguienteDespuesDe(recurrente.getProximaFecha()));
            if (recurrente.plazosTerminados()) {
                recurrente.setActivo(false);
            }
        }
        return creados;
    }

    private Movimiento movimientoDe(Recurrente recurrente) {
        String concepto = recurrente.getCuotasTotal() == null
                ? recurrente.getConcepto()
                : recurrente.getConcepto() + " (" + (recurrente.getCuotasPagadas() + 1) + "/"
                        + recurrente.getCuotasTotal() + ")";
        return Movimiento.builder()
                .usuarioId(recurrente.getUsuarioId())
                .fecha(recurrente.getProximaFecha())
                .importe(recurrente.getImporte())
                .tipo(recurrente.getTipo())
                .categoriaId(recurrente.getCategoriaId())
                .concepto(concepto.length() > 255 ? concepto.substring(0, 255) : concepto)
                .metodoPago(recurrente.getMetodoPago())
                .recurrente(true)
                .build();
    }

    /**
     * 404 si la categoria no existe o es de otro usuario; 400 si es de otro
     * tipo. Lo mismo que MovimientoService, porque cada cargo sera un movimiento.
     */
    private void validarCategoria(Long usuarioId, Recurrente recurrente) {
        Long categoriaId = recurrente.getCategoriaId();
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .filter(c -> c.getUsuarioId().equals(usuarioId))
                .orElseThrow(() -> new CategoriaNotFoundException(categoriaId));

        if (categoria.getTipo() != recurrente.getTipo()) {
            throw new ValidationException("El tipo del recurrente no coincide con el de su categoria");
        }
    }

    /**
     * 404, no 403, si el id existe pero pertenece a otro usuario: un 403
     * confirmaria que ese id existe.
     */
    private Recurrente getPropio(Long usuarioId, Long id) {
        Recurrente recurrente = repository.findById(id)
                .orElseThrow(() -> new RecurrenteNotFoundException(id));

        if (!recurrente.getUsuarioId().equals(usuarioId)) {
            throw new RecurrenteNotFoundException(id);
        }

        return recurrente;
    }
}
