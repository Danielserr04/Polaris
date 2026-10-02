package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.out.NotificacionRepositoryPort;
import com.polaris.kuiper.domain.model.Notificacion;
import com.polaris.kuiper.domain.model.NotificacionFilter;
import com.polaris.kuiper.infrastructure.persistence.mapper.NotificacionEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * El unico punto del modulo donde conviven modelo y Entity.
 */
@Component
@RequiredArgsConstructor
public class NotificacionJpaAdapter implements NotificacionRepositoryPort {

    private final NotificacionRepository repository;
    private final NotificacionEntityMapper mapper;

    /**
     * REQUIRES_NEW: el aviso se guarda en su propia transaccion. Si falla, se
     * deshace solo el aviso y la transaccion de quien llama (por ejemplo la
     * pasada de RecurrenteJob) no queda marcada para rollback. NotificacionService
     * captura la excepcion.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notificacion save(Notificacion notificacion) {
        return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(notificacion)));
    }

    @Override
    public Optional<Notificacion> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Notificacion> findAll(Long usuarioId, NotificacionFilter filter) {
        Specification<NotificacionEntity> spec = NotificacionSpecifications.from(usuarioId, filter);
        Sort orden = Sort.by(Sort.Direction.DESC, "creadaEn").and(Sort.by(Sort.Direction.DESC, "id"));
        return mapper.toDomainList(repository.findAll(spec, orden));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByUsuarioIdAndClave(Long usuarioId, String clave) {
        return repository.existsByUsuarioIdAndClave(usuarioId, clave);
    }

    @Override
    public long countNoLeidas(Long usuarioId) {
        return repository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    @Override
    public int marcarTodasLeidas(Long usuarioId) {
        return repository.marcarTodasLeidas(usuarioId);
    }

    @Override
    public int deleteLeidas(Long usuarioId) {
        return repository.deleteLeidas(usuarioId);
    }
}
