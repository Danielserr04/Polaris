package com.polaris.atlas.domain.service;

import com.polaris.atlas.application.in.CreateRutinaInterface;
import com.polaris.atlas.application.in.DeleteRutinaInterface;
import com.polaris.atlas.application.in.GetRutinaInterface;
import com.polaris.atlas.application.in.ListRutinaInterface;
import com.polaris.atlas.application.in.UpdateRutinaInterface;
import com.polaris.atlas.application.out.EjercicioRepositoryPort;
import com.polaris.atlas.application.out.RutinaRepositoryPort;
import com.polaris.atlas.domain.model.Rutina;
import com.polaris.atlas.domain.model.RutinaEjercicio;
import com.polaris.atlas.domain.model.RutinaFilter;
import com.polaris.atlas.domain.model.RutinaNotFoundException;
import com.polaris.shared.error.DuplicateResourceException;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Rutina y sus lineas son un solo agregado: se crean y editan juntas. Ver
 * docs/decisiones/024-rutina-agregado-con-lineas.md.
 *
 * <p>El usuario de la rutina y de cada linea sale del JWT, nunca del cliente.
 * Cada ejercicio de una linea tiene que ser visible para el usuario (catalogo o
 * suyo): si no, 400 sin distinguir "no existe" de "es de otro".
 *
 * <p>Pendiente con Sesion: el borrado de una rutina con sesiones asociadas se
 * decidira al crear Sesion. Hoy borra sin mas porque esa tabla no existe.
 */
@Service
@RequiredArgsConstructor
public class RutinaService implements
        CreateRutinaInterface,
        GetRutinaInterface,
        ListRutinaInterface,
        UpdateRutinaInterface,
        DeleteRutinaInterface {

    static final int MAX_LINEAS = 50;

    private final RutinaRepositoryPort repository;
    private final EjercicioRepositoryPort ejercicioRepository;

    @Override
    public Rutina create(Long usuarioId, Rutina rutina) {
        validarYPreparar(usuarioId, rutina, null);
        rutina.setId(null);
        rutina.setUsuarioId(usuarioId);
        return repository.save(rutina);
    }

    @Override
    public Rutina get(Long usuarioId, Long id) {
        return getPropia(usuarioId, id);
    }

    @Override
    public List<Rutina> list(Long usuarioId, RutinaFilter filter) {
        return repository.findAll(usuarioId, filter);
    }

    /** Reemplazo completo: el conjunto de lineas que llega sustituye al anterior. */
    @Override
    public Rutina update(Long usuarioId, Long id, Rutina rutina) {
        Rutina existente = getPropia(usuarioId, id);
        validarYPreparar(usuarioId, rutina, existente.getId());
        rutina.setId(existente.getId());
        rutina.setUsuarioId(existente.getUsuarioId());
        return repository.save(rutina);
    }

    @Override
    public void delete(Long usuarioId, Long id) {
        getPropia(usuarioId, id);
        repository.deleteById(id);
    }

    /**
     * Validaciones, en este orden: estructura de las lineas (400), ejercicios
     * visibles (400), nombre libre (409). Deja las lineas ordenadas por
     * {@code orden}, sin id (nunca vienen del cliente) y con el usuario fijado.
     */
    private void validarYPreparar(Long usuarioId, Rutina rutina, Long propiaId) {
        validarEstructura(rutina.getLineas());

        List<RutinaEjercicio> lineas = new ArrayList<>(rutina.getLineas());
        for (RutinaEjercicio linea : lineas) {
            comprobarEjercicioVisible(usuarioId, linea.getEjercicioId());
            linea.setId(null);
            linea.setUsuarioId(usuarioId);
        }
        lineas.sort(Comparator.comparing(RutinaEjercicio::getOrden));
        rutina.setLineas(lineas);

        rutina.setNombre(rutina.getNombre().trim());
        comprobarNombreLibre(usuarioId, rutina.getNombre(), propiaId);
    }

    private void validarEstructura(List<RutinaEjercicio> lineas) {
        if (lineas == null || lineas.isEmpty()) {
            throw new ValidationException("Una rutina necesita al menos un ejercicio");
        }
        if (lineas.size() > MAX_LINEAS) {
            throw new ValidationException("Una rutina admite como mucho " + MAX_LINEAS + " ejercicios");
        }
        Set<Integer> ordenes = new HashSet<>();
        for (RutinaEjercicio linea : lineas) {
            if (!ordenes.add(linea.getOrden())) {
                throw new ValidationException("El orden " + linea.getOrden() + " esta repetido en la rutina");
            }
        }
    }

    /**
     * El ejercicio tiene que ser del catalogo o del propio usuario. Un ejercicio
     * que no existe y uno propio de otro usuario dan el mismo mensaje: no se
     * confirma que ese id exista.
     */
    private void comprobarEjercicioVisible(Long usuarioId, Long ejercicioId) {
        boolean visible = ejercicioRepository.findById(ejercicioId)
                .filter(e -> !e.isPropio() || e.perteneceA(usuarioId))
                .isPresent();
        if (!visible) {
            throw new ValidationException("El ejercicio " + ejercicioId + " no existe o no esta disponible para ti");
        }
    }

    /** 409 si otra rutina del mismo usuario (distinta de {@code propiaId}) ya se llama asi. */
    private void comprobarNombreLibre(Long usuarioId, String nombre, Long propiaId) {
        boolean ocupado = repository.findByUsuarioIdAndNombre(usuarioId, nombre)
                .filter(otra -> !otra.getId().equals(propiaId))
                .isPresent();
        if (ocupado) {
            throw new DuplicateResourceException("Ya tienes una rutina con ese nombre");
        }
    }

    /**
     * 404, no 403, si el id existe pero pertenece a otro usuario: un 403
     * confirmaria que ese id existe.
     */
    private Rutina getPropia(Long usuarioId, Long id) {
        Rutina rutina = repository.findById(id)
                .orElseThrow(() -> new RutinaNotFoundException(id));

        if (!rutina.getUsuarioId().equals(usuarioId)) {
            throw new RutinaNotFoundException(id);
        }

        return rutina;
    }
}
