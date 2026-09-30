package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.in.CreatePresupuestoInterface;
import com.polaris.kuiper.application.in.DeletePresupuestoInterface;
import com.polaris.kuiper.application.in.GetPresupuestoInterface;
import com.polaris.kuiper.application.in.ListPresupuestoInterface;
import com.polaris.kuiper.application.in.UpdatePresupuestoInterface;
import com.polaris.kuiper.application.out.CategoriaRepositoryPort;
import com.polaris.kuiper.application.out.PresupuestoRepositoryPort;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.CategoriaNotFoundException;
import com.polaris.kuiper.domain.model.Presupuesto;
import com.polaris.kuiper.domain.model.PresupuestoFilter;
import com.polaris.kuiper.domain.model.PresupuestoNotFoundException;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.shared.error.DuplicateResourceException;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Un presupuesto por categoria y periodo, y solo sobre categorias de GASTO.
 * Ver docs/decisiones/013-presupuesto-solo-gastos-uno-por-periodo.md.
 */
@Service
@RequiredArgsConstructor
public class PresupuestoService implements
        CreatePresupuestoInterface,
        GetPresupuestoInterface,
        ListPresupuestoInterface,
        UpdatePresupuestoInterface,
        DeletePresupuestoInterface {

    private final PresupuestoRepositoryPort repository;
    private final CategoriaRepositoryPort categoriaRepository;

    @Override
    public Presupuesto create(Long usuarioId, Presupuesto presupuesto) {
        validarCategoria(usuarioId, presupuesto);
        comprobarLibre(usuarioId, presupuesto, null);
        presupuesto.setId(null);
        presupuesto.setUsuarioId(usuarioId);
        return repository.save(presupuesto);
    }

    @Override
    public Presupuesto get(Long usuarioId, Long id) {
        return getPropio(usuarioId, id);
    }

    @Override
    public List<Presupuesto> list(Long usuarioId, PresupuestoFilter filter) {
        return repository.findAll(usuarioId, filter);
    }

    @Override
    public Presupuesto update(Long usuarioId, Long id, Presupuesto presupuesto) {
        Presupuesto existente = getPropio(usuarioId, id);
        validarCategoria(usuarioId, presupuesto);
        comprobarLibre(usuarioId, presupuesto, existente.getId());
        presupuesto.setId(existente.getId());
        presupuesto.setUsuarioId(existente.getUsuarioId());
        return repository.save(presupuesto);
    }

    @Override
    public void delete(Long usuarioId, Long id) {
        getPropio(usuarioId, id);
        repository.deleteById(id);
    }

    /**
     * 404 si la categoria no existe o es de otro usuario (no se distingue, por
     * lo mismo que en getPropio). 400 si existe pero no es de GASTO: un limite
     * solo tiene sentido sobre lo que se gasta.
     */
    private void validarCategoria(Long usuarioId, Presupuesto presupuesto) {
        Long categoriaId = presupuesto.getCategoriaId();
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .filter(c -> c.getUsuarioId().equals(usuarioId))
                .orElseThrow(() -> new CategoriaNotFoundException(categoriaId));

        if (categoria.getTipo() != TipoMovimiento.GASTO) {
            throw new ValidationException("Solo se puede poner presupuesto a una categoria de gasto");
        }
    }

    /**
     * 409 si otro presupuesto del usuario (distinto de {@code propioId}) ya
     * cubre esa categoria y periodo.
     */
    private void comprobarLibre(Long usuarioId, Presupuesto presupuesto, Long propioId) {
        repository.findByUsuarioIdAndCategoriaIdAndPeriodo(usuarioId, presupuesto.getCategoriaId(),
                        presupuesto.getPeriodo())
                .filter(otro -> !otro.getId().equals(propioId))
                .ifPresent(otro -> {
                    throw new DuplicateResourceException("Ya tienes un presupuesto para esa categoria y periodo");
                });
    }

    /**
     * 404, no 403, si el id existe pero pertenece a otro usuario: un 403
     * confirmaria que ese id existe.
     */
    private Presupuesto getPropio(Long usuarioId, Long id) {
        Presupuesto presupuesto = repository.findById(id)
                .orElseThrow(() -> new PresupuestoNotFoundException(id));

        if (!presupuesto.getUsuarioId().equals(usuarioId)) {
            throw new PresupuestoNotFoundException(id);
        }

        return presupuesto;
    }
}
