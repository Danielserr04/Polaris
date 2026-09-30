package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.in.CreateMovimientoInterface;
import com.polaris.kuiper.application.in.DeleteMovimientoInterface;
import com.polaris.kuiper.application.in.GetMovimientoInterface;
import com.polaris.kuiper.application.in.ListMovimientoInterface;
import com.polaris.kuiper.application.in.UpdateMovimientoInterface;
import com.polaris.kuiper.application.out.CategoriaRepositoryPort;
import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.CategoriaNotFoundException;
import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.MovimientoFilter;
import com.polaris.kuiper.domain.model.MovimientoNotFoundException;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * La categoria tiene que ser del usuario y de su mismo tipo. Ver
 * docs/decisiones/012-movimiento-categoria-mismo-tipo.md.
 */
@Service
@RequiredArgsConstructor
public class MovimientoService implements
        CreateMovimientoInterface,
        GetMovimientoInterface,
        ListMovimientoInterface,
        UpdateMovimientoInterface,
        DeleteMovimientoInterface {

    private final MovimientoRepositoryPort repository;
    private final CategoriaRepositoryPort categoriaRepository;

    @Override
    public Movimiento create(Long usuarioId, Movimiento movimiento) {
        validarCategoria(usuarioId, movimiento);
        movimiento.setId(null);
        movimiento.setUsuarioId(usuarioId);
        return repository.save(movimiento);
    }

    @Override
    public Movimiento get(Long usuarioId, Long id) {
        return getPropio(usuarioId, id);
    }

    @Override
    public List<Movimiento> list(Long usuarioId, MovimientoFilter filter) {
        return repository.findAll(usuarioId, filter);
    }

    @Override
    public Movimiento update(Long usuarioId, Long id, Movimiento movimiento) {
        Movimiento existente = getPropio(usuarioId, id);
        validarCategoria(usuarioId, movimiento);
        movimiento.setId(existente.getId());
        movimiento.setUsuarioId(existente.getUsuarioId());
        return repository.save(movimiento);
    }

    @Override
    public void delete(Long usuarioId, Long id) {
        getPropio(usuarioId, id);
        repository.deleteById(id);
    }

    /**
     * 404 si la categoria no existe o es de otro usuario (no se distingue, por
     * lo mismo que en getPropio). 400 si existe pero es de otro tipo.
     */
    private void validarCategoria(Long usuarioId, Movimiento movimiento) {
        Long categoriaId = movimiento.getCategoriaId();
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .filter(c -> c.getUsuarioId().equals(usuarioId))
                .orElseThrow(() -> new CategoriaNotFoundException(categoriaId));

        if (categoria.getTipo() != movimiento.getTipo()) {
            throw new ValidationException("El tipo del movimiento no coincide con el de su categoria");
        }
    }

    /**
     * 404, no 403, si el id existe pero pertenece a otro usuario: un 403
     * confirmaria que ese id existe.
     */
    private Movimiento getPropio(Long usuarioId, Long id) {
        Movimiento movimiento = repository.findById(id)
                .orElseThrow(() -> new MovimientoNotFoundException(id));

        if (!movimiento.getUsuarioId().equals(usuarioId)) {
            throw new MovimientoNotFoundException(id);
        }

        return movimiento;
    }
}
