package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.out.MetaAhorroRepositoryPort;
import com.polaris.kuiper.domain.model.AportacionMeta;
import com.polaris.kuiper.domain.model.MetaAhorro;
import com.polaris.kuiper.domain.model.MetaAhorroFilter;
import com.polaris.kuiper.infrastructure.persistence.mapper.MetaAhorroEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * El unico punto del modulo donde conviven modelo y Entity. Cada MetaAhorro
 * que devuelve lleva importeActual: una consulta de suma para una, una
 * agrupada para el listado (nunca una por meta).
 */
@Component
@RequiredArgsConstructor
public class MetaAhorroJpaAdapter implements MetaAhorroRepositoryPort {

    /** Primero las que vencen antes; las que no tienen fecha, al final. */
    private static final Comparator<MetaAhorro> POR_FECHA_LIMITE =
            Comparator.comparing(MetaAhorro::getFechaLimite, Comparator.nullsLast(Comparator.naturalOrder()));

    private final MetaAhorroRepository repository;
    private final AportacionMetaRepository aportacionRepository;
    private final MetaAhorroEntityMapper mapper;

    @Override
    public MetaAhorro save(MetaAhorro meta) {
        return conSuma(mapper.toDomain(repository.save(mapper.toEntity(meta))));
    }

    @Override
    public Optional<MetaAhorro> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain).map(this::conSuma);
    }

    /** Por nombre en la base y despues, estable, por fecha limite: asi las de igual fecha quedan por nombre. */
    @Override
    public List<MetaAhorro> findAll(Long usuarioId, MetaAhorroFilter filter) {
        Specification<MetaAhorroEntity> spec = MetaAhorroSpecifications.from(usuarioId, filter);
        List<MetaAhorro> metas = repository.findAll(spec, Sort.by("nombre").and(Sort.by("id"))).stream()
                .map(mapper::toDomain)
                .sorted(POR_FECHA_LIMITE)
                .toList();
        if (metas.isEmpty()) {
            return metas;
        }

        Map<Long, BigDecimal> sumas = new HashMap<>();
        for (Object[] fila : aportacionRepository.sumasPorMeta(metas.stream().map(MetaAhorro::getId).toList())) {
            sumas.put((Long) fila[0], (BigDecimal) fila[1]);
        }
        metas.forEach(m -> m.setImporteActual(sumas.getOrDefault(m.getId(), BigDecimal.ZERO.setScale(2))));
        return metas;
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<MetaAhorro> findByUsuarioIdAndNombre(Long usuarioId, String nombre) {
        return repository.findByUsuarioIdAndNombre(usuarioId, nombre).map(mapper::toDomain);
    }

    @Override
    public AportacionMeta saveAportacion(AportacionMeta aportacion) {
        return mapper.toDomain(aportacionRepository.save(mapper.toEntity(aportacion)));
    }

    @Override
    public Optional<AportacionMeta> findAportacionById(Long id) {
        return aportacionRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<AportacionMeta> findAportaciones(Long metaId) {
        return mapper.toAportacionList(aportacionRepository.findByMetaIdOrderByFechaDescIdDesc(metaId));
    }

    @Override
    public void deleteAportacionById(Long id) {
        aportacionRepository.deleteById(id);
    }

    @Override
    public BigDecimal sumaAportaciones(Long metaId) {
        return aportacionRepository.sumaPorMeta(metaId).setScale(2);
    }

    private MetaAhorro conSuma(MetaAhorro meta) {
        meta.setImporteActual(sumaAportaciones(meta.getId()));
        return meta;
    }
}
