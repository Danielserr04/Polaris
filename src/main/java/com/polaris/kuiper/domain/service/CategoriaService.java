package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.in.CreateCategoriaInterface;
import com.polaris.kuiper.application.in.DeleteCategoriaInterface;
import com.polaris.kuiper.application.in.GetCategoriaInterface;
import com.polaris.kuiper.application.in.ListCategoriaInterface;
import com.polaris.kuiper.application.in.UpdateCategoriaInterface;
import com.polaris.kuiper.application.out.CategoriaRepositoryPort;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.CategoriaFilter;
import com.polaris.kuiper.domain.model.CategoriaNotFoundException;
import com.polaris.shared.error.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Nombre unico por usuario y tipo. Ver docs/decisiones/011-categoria-nombre-unico-por-tipo.md.
 *
 * <p>Todavia no impide borrar una categoria con movimientos o presupuestos:
 * esas tablas no existen. Esa comprobacion entra con Movimiento.
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
        categoria.setId(existente.getId());
        categoria.setUsuarioId(existente.getUsuarioId());
        return repository.save(categoria);
    }

    @Override
    public void delete(Long usuarioId, Long id) {
        getPropia(usuarioId, id);
        repository.deleteById(id);
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
