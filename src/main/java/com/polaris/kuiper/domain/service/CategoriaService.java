package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.in.CreateCategoriaInterface;
import com.polaris.kuiper.application.in.DeleteCategoriaInterface;
import com.polaris.kuiper.application.in.GetCategoriaInterface;
import com.polaris.kuiper.application.in.ListCategoriaInterface;
import com.polaris.kuiper.application.in.UpdateCategoriaInterface;
import com.polaris.kuiper.application.out.CategoriaRepositoryPort;
import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.application.out.PresupuestoRepositoryPort;
import com.polaris.kuiper.application.out.RecurrenteRepositoryPort;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.CategoriaFilter;
import com.polaris.kuiper.domain.model.CategoriaNotFoundException;
import com.polaris.shared.error.DuplicateResourceException;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Nombre unico por usuario y tipo. Ver docs/decisiones/011-categoria-nombre-unico-por-tipo.md.
 *
 * <p>Una categoria con movimientos, presupuestos o recurrentes no se borra ni cambia de
 * tipo (400, como TituloService con sus entradas): dejaria filas huerfanas o
 * de tipo distinto al de su categoria. Ver
 * docs/decisiones/012-movimiento-categoria-mismo-tipo.md y
 * docs/decisiones/013-presupuesto-solo-gastos-uno-por-periodo.md.
 *
 * <p>Los movimientos de la papelera no cuentan como uso: al borrar la
 * categoria se borran de verdad antes que ella. Ver
 * docs/decisiones/038-movimiento-papelera-y-duplicar.md.
 */
@Service
@RequiredArgsConstructor
public class CategoriaService implements
        CreateCategoriaInterface,
        GetCategoriaInterface,
        ListCategoriaInterface,
        UpdateCategoriaInterface,
        DeleteCategoriaInterface {

    private final CategoriaRepositoryPort repository;
    private final MovimientoRepositoryPort movimientoRepository;
    private final PresupuestoRepositoryPort presupuestoRepository;
    private final RecurrenteRepositoryPort recurrenteRepository;

    @Override
    public Categoria create(Long usuarioId, Categoria categoria) {
        comprobarNombreLibre(usuarioId, categoria, null);
        categoria.setId(null);
        categoria.setUsuarioId(usuarioId);
        return repository.save(categoria);
    }

    @Override
    public Categoria get(Long usuarioId, Long id) {
        return getPropia(usuarioId, id);
    }

    @Override
    public List<Categoria> list(Long usuarioId, CategoriaFilter filter) {
        return repository.findAll(usuarioId, filter);
    }

    @Override
    public Categoria update(Long usuarioId, Long id, Categoria categoria) {
        Categoria existente = getPropia(usuarioId, id);
        comprobarNombreLibre(usuarioId, categoria, existente.getId());

        if (existente.getTipo() != categoria.getTipo() && estaEnUso(id)) {
            throw new ValidationException(
                    "No se puede cambiar el tipo de una categoria que tiene movimientos, presupuestos o recurrentes");
        }

        categoria.setId(existente.getId());
        categoria.setUsuarioId(existente.getUsuarioId());
        return repository.save(categoria);
    }

    @Override
    public void delete(Long usuarioId, Long id) {
        getPropia(usuarioId, id);

        if (estaEnUso(id)) {
            throw new ValidationException("No se puede borrar una categoria que tiene movimientos, presupuestos o recurrentes");
        }

        movimientoRepository.deleteEnPapeleraByCategoriaId(id);
        repository.deleteById(id);
    }

    private boolean estaEnUso(Long categoriaId) {
        return movimientoRepository.existsByCategoriaId(categoriaId)
                || presupuestoRepository.existsByCategoriaId(categoriaId)
                || recurrenteRepository.existsByCategoriaId(categoriaId);
    }

    /**
     * 409 si otra categoria del usuario (distinta de {@code propiaId}) ya usa
     * ese nombre con ese tipo.
     */
    private void comprobarNombreLibre(Long usuarioId, Categoria categoria, Long propiaId) {
        repository.findByUsuarioIdAndNombreAndTipo(usuarioId, categoria.getNombre(), categoria.getTipo())
                .filter(otra -> !otra.getId().equals(propiaId))
                .ifPresent(otra -> {
                    throw new DuplicateResourceException("Ya tienes una categoria con ese nombre y tipo");
                });
    }

    /**
     * 404, no 403, si el id existe pero pertenece a otro usuario: un 403
     * confirmaria que ese id existe.
     */
    private Categoria getPropia(Long usuarioId, Long id) {
        Categoria categoria = repository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));

        if (!categoria.getUsuarioId().equals(usuarioId)) {
            throw new CategoriaNotFoundException(id);
        }

        return categoria;
    }
}
