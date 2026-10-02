package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.in.BorrarMovimientosInterface;
import com.polaris.kuiper.application.in.DeleteDefinitivoMovimientoInterface;
import com.polaris.kuiper.application.in.ListPapeleraMovimientoInterface;
import com.polaris.kuiper.application.in.PurgarPapeleraMovimientoInterface;
import com.polaris.kuiper.application.in.RestaurarMovimientoInterface;
import com.polaris.kuiper.application.in.VaciarPapeleraMovimientoInterface;
import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.MovimientoNotFoundException;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * La papelera de movimientos: borrar varios a la vez, verla, restaurar,
 * borrar de verdad y la purga de los que llevan mas de 30 dias. Ver
 * docs/decisiones/038-movimiento-papelera-y-duplicar.md.
 *
 * <p>Como en MovimientoService, un id de otro usuario es un 404, no un 403.
 */
@Service
@RequiredArgsConstructor
public class MovimientoPapeleraService implements
        BorrarMovimientosInterface,
        ListPapeleraMovimientoInterface,
        RestaurarMovimientoInterface,
        DeleteDefinitivoMovimientoInterface,
        VaciarPapeleraMovimientoInterface,
        PurgarPapeleraMovimientoInterface {

    /** Lo que aguanta un movimiento en la papelera antes de que el job lo borre. */
    public static final int DIAS_EN_PAPELERA = 30;

    private final MovimientoRepositoryPort repository;

    /**
     * Todos o ninguno: se comprueba cada id antes de tocar nada (404 con el
     * primero que no exista, sea de otro usuario o ya este en la papelera) y
     * luego se mueven en una sola sentencia.
     */
    @Override
    public void borrar(Long usuarioId, List<Long> ids) {
        List<Long> distintos = ids.stream().distinct().toList();
        for (Long id : distintos) {
            repository.findById(id)
                    .filter(m -> m.getUsuarioId().equals(usuarioId))
                    .orElseThrow(() -> new MovimientoNotFoundException(id));
        }
        repository.moverAPapelera(usuarioId, distintos, LocalDateTime.now());
    }

    @Override
    public List<Movimiento> listPapelera(Long usuarioId) {
        return repository.findPapelera(usuarioId);
    }

    /**
     * Mientras estuvo en la papelera la categoria pudo cambiar de tipo (los
     * movimientos de la papelera no la bloquean): entonces no se restaura, con
     * un 400, para no dejar un movimiento de tipo distinto al de su categoria.
     */
    @Override
    public Movimiento restaurar(Long usuarioId, Long id) {
        Movimiento movimiento = getPropioEnPapelera(usuarioId, id);

        if (movimiento.getCategoria() != null && movimiento.getCategoria().getTipo() != movimiento.getTipo()) {
            throw new ValidationException(
                    "No se puede restaurar: su categoria ha cambiado de tipo mientras estaba en la papelera");
        }

        movimiento.setBorradoEn(null);
        return repository.save(movimiento);
    }

    /** Solo desde la papelera: un movimiento normal hay que mandarlo antes a ella. */
    @Override
    public void deleteDefinitivo(Long usuarioId, Long id) {
        getPropioEnPapelera(usuarioId, id);
        repository.deleteById(id);
    }

    @Override
    public int vaciar(Long usuarioId) {
        return repository.vaciarPapelera(usuarioId);
    }

    @Override
    public int purgar(LocalDateTime ahora) {
        return repository.purgarPapelera(ahora.minusDays(DIAS_EN_PAPELERA));
    }

    private Movimiento getPropioEnPapelera(Long usuarioId, Long id) {
        return repository.findEnPapeleraById(id)
                .filter(m -> m.getUsuarioId().equals(usuarioId))
                .orElseThrow(() -> new MovimientoNotFoundException(id));
    }
}
