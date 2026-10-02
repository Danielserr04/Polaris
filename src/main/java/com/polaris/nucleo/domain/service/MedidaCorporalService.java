package com.polaris.nucleo.domain.service;

import com.polaris.nucleo.application.in.CreateMedidaCorporalInterface;
import com.polaris.nucleo.application.in.DeleteMedidaCorporalInterface;
import com.polaris.nucleo.application.in.GetMedidaCorporalInterface;
import com.polaris.nucleo.application.in.ListMedidaCorporalInterface;
import com.polaris.nucleo.application.in.UpdateMedidaCorporalInterface;
import com.polaris.nucleo.application.out.MedidaCorporalRepositoryPort;
import com.polaris.nucleo.domain.model.MedidaCorporal;
import com.polaris.nucleo.domain.model.MedidaCorporalFilter;
import com.polaris.nucleo.domain.model.MedidaCorporalNotFoundException;
import com.polaris.shared.error.DuplicateResourceException;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Una medicion por dia (unique usuario_id + fecha), con al menos un perimetro.
 * Ver docs/decisiones/041-medida-corporal-una-por-dia.md.
 */
@Service
@RequiredArgsConstructor
public class MedidaCorporalService implements
        CreateMedidaCorporalInterface,
        GetMedidaCorporalInterface,
        ListMedidaCorporalInterface,
        UpdateMedidaCorporalInterface,
        DeleteMedidaCorporalInterface {

    private final MedidaCorporalRepositoryPort repository;

    /**
     * Si ya hay registro ese dia, conserva su id para que el save actualice y
     * no inserte una segunda fila (que el unique de la tabla rechazaria).
     */
    @Override
    public MedidaCorporal create(Long usuarioId, MedidaCorporal registro) {
        exigirAlgunaMedida(registro);
        registro.setId(repository.findByUsuarioIdAndFecha(usuarioId, registro.getFecha())
                .map(MedidaCorporal::getId)
                .orElse(null));
        registro.setUsuarioId(usuarioId);
        return repository.save(registro);
    }

    @Override
    public MedidaCorporal get(Long usuarioId, Long id) {
        return getPropio(usuarioId, id);
    }

    @Override
    public List<MedidaCorporal> list(Long usuarioId, MedidaCorporalFilter filter) {
        return repository.findAll(usuarioId, filter);
    }

    /**
     * Mover un registro a una fecha que ya tiene otro es un 409: aqui no hay
     * "actualizar el otro", el cliente pidio editar este.
     */
    @Override
    public MedidaCorporal update(Long usuarioId, Long id, MedidaCorporal registro) {
        exigirAlgunaMedida(registro);
        MedidaCorporal existente = getPropio(usuarioId, id);

        repository.findByUsuarioIdAndFecha(usuarioId, registro.getFecha())
                .filter(otro -> !otro.getId().equals(existente.getId()))
                .ifPresent(otro -> {
                    throw new DuplicateResourceException("Ya hay medidas registradas en esa fecha");
                });

        registro.setId(existente.getId());
        registro.setUsuarioId(existente.getUsuarioId());
        return repository.save(registro);
    }

    @Override
    public void delete(Long usuarioId, Long id) {
        getPropio(usuarioId, id);
        repository.deleteById(id);
    }

    private static void exigirAlgunaMedida(MedidaCorporal registro) {
        if (registro.sinMedidas()) {
            throw new ValidationException("Indica al menos una medida");
        }
    }

    /**
     * 404, no 403, si el id existe pero pertenece a otro usuario: un 403
     * confirmaria que ese id existe.
     */
    private MedidaCorporal getPropio(Long usuarioId, Long id) {
        MedidaCorporal registro = repository.findById(id)
                .orElseThrow(() -> new MedidaCorporalNotFoundException(id));

        if (!registro.getUsuarioId().equals(usuarioId)) {
            throw new MedidaCorporalNotFoundException(id);
        }

        return registro;
    }
}
