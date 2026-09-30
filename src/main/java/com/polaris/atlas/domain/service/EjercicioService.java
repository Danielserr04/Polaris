package com.polaris.atlas.domain.service;

import com.polaris.atlas.application.in.CreateEjercicioInterface;
import com.polaris.atlas.application.in.DeleteEjercicioInterface;
import com.polaris.atlas.application.in.GetEjercicioInterface;
import com.polaris.atlas.application.in.ListEjercicioInterface;
import com.polaris.atlas.application.in.UpdateEjercicioInterface;
import com.polaris.atlas.application.out.EjercicioRepositoryPort;
import com.polaris.atlas.application.out.RutinaEjercicioRepositoryPort;
import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.domain.model.EjercicioCatalogoNoModificableException;
import com.polaris.atlas.domain.model.EjercicioFilter;
import com.polaris.atlas.domain.model.EjercicioNotFoundException;
import com.polaris.shared.error.DuplicateResourceException;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Catalogo compartido (usuarioId nulo, solo lectura) mas ejercicios propios
 * (editables solo por su dueno). Ver docs/decisiones/023-ejercicio-catalogo-y-propios.md.
 *
 * <p>Un ejercicio propio usado en alguna rutina no se borra: 400, como
 * AlimentoService con sus comidas. Ver docs/decisiones/024-rutina-agregado-con-lineas.md.
 * Pendiente con SerieRegistro: uno con series registradas tampoco se podra
 * borrar. Hoy no se comprueba porque esa tabla no existe.
 */
@Service
@RequiredArgsConstructor
public class EjercicioService implements
        CreateEjercicioInterface,
        GetEjercicioInterface,
        ListEjercicioInterface,
        UpdateEjercicioInterface,
        DeleteEjercicioInterface {

    private final EjercicioRepositoryPort repository;
    private final RutinaEjercicioRepositoryPort rutinaEjercicioRepository;

    /** Siempre propio: el catalogo no se crea por la API. El usuarioId sale del JWT, nunca del body. */
    @Override
    public Ejercicio create(Long usuarioId, Ejercicio ejercicio) {
        comprobarNombreLibre(usuarioId, ejercicio.getNombre(), null);
        ejercicio.setId(null);
        ejercicio.setUsuarioId(usuarioId);
        return repository.save(ejercicio);
    }

    @Override
    public Ejercicio get(Long usuarioId, Long id) {
        return getVisible(usuarioId, id);
    }

    @Override
    public List<Ejercicio> list(Long usuarioId, EjercicioFilter filter) {
        return repository.findAll(usuarioId, filter);
    }

    @Override
    public Ejercicio update(Long usuarioId, Long id, Ejercicio ejercicio) {
        Ejercicio existente = getPropio(usuarioId, id);
        comprobarNombreLibre(usuarioId, ejercicio.getNombre(), existente.getId());

        ejercicio.setId(existente.getId());
        ejercicio.setUsuarioId(existente.getUsuarioId());
        return repository.save(ejercicio);
    }

    @Override
    public void delete(Long usuarioId, Long id) {
        getPropio(usuarioId, id);

        if (rutinaEjercicioRepository.existsByEjercicioId(id)) {
            throw new ValidationException("No se puede borrar un ejercicio que esta en alguna rutina");
        }

        repository.deleteById(id);
    }

    /**
     * 409 si otro ejercicio visible para el usuario (catalogo o suyo, distinto
     * de {@code propioId}) ya se llama asi: en su lista saldrian dos iguales.
     */
    private void comprobarNombreLibre(Long usuarioId, String nombre, Long propioId) {
        boolean ocupado = repository.findVisiblesByNombre(usuarioId, nombre).stream()
                .anyMatch(otro -> !otro.getId().equals(propioId));
        if (ocupado) {
            throw new DuplicateResourceException("Ya existe un ejercicio con ese nombre");
        }
    }

    /**
     * 404, no 403, si el id existe pero es propio de otro usuario: un 403
     * confirmaria que ese id existe.
     */
    private Ejercicio getVisible(Long usuarioId, Long id) {
        Ejercicio ejercicio = repository.findById(id)
                .orElseThrow(() -> new EjercicioNotFoundException(id));

        if (ejercicio.isPropio() && !ejercicio.perteneceA(usuarioId)) {
            throw new EjercicioNotFoundException(id);
        }

        return ejercicio;
    }

    /** Visible y ademas del usuario: el catalogo se ve, pero no se toca (403). */
    private Ejercicio getPropio(Long usuarioId, Long id) {
        Ejercicio ejercicio = getVisible(usuarioId, id);

        if (!ejercicio.isPropio()) {
            throw new EjercicioCatalogoNoModificableException(id);
        }

        return ejercicio;
    }
}
