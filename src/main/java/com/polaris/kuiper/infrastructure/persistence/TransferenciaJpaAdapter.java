package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.out.TransferenciaRepositoryPort;
import com.polaris.kuiper.domain.model.CuentaNotFoundException;
import com.polaris.kuiper.domain.model.Transferencia;
import com.polaris.kuiper.domain.model.TransferenciaFilter;
import com.polaris.kuiper.infrastructure.persistence.mapper.TransferenciaEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * El unico punto del modulo donde conviven modelo y Entity.
 */
@Component
@RequiredArgsConstructor
public class TransferenciaJpaAdapter implements TransferenciaRepositoryPort {

    private final TransferenciaRepository repository;
    private final CuentaRepository cuentaRepository;
    private final TransferenciaEntityMapper mapper;

    /** findById, no getReferenceById, por lo mismo que en MovimientoJpaAdapter. */
    @Override
    public Transferencia save(Transferencia transferencia) {
        TransferenciaEntity entity = mapper.toEntity(transferencia);
        entity.setCuentaOrigen(cuenta(transferencia.getCuentaOrigenId()));
        entity.setCuentaDestino(cuenta(transferencia.getCuentaDestinoId()));
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Transferencia> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Transferencia> findAll(Long usuarioId, TransferenciaFilter filter) {
        Specification<TransferenciaEntity> spec = TransferenciaSpecifications.from(usuarioId, filter);
        Sort orden = Sort.by(Sort.Direction.DESC, "fecha").and(Sort.by(Sort.Direction.DESC, "id"));
        return mapper.toDomainList(repository.findAll(spec, orden));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Map<Long, BigDecimal> sumarEntrantesPorCuenta(Long usuarioId) {
        return aMapa(repository.sumarEntrantesPorCuenta(usuarioId));
    }

    @Override
    public Map<Long, BigDecimal> sumarSalientesPorCuenta(Long usuarioId) {
        return aMapa(repository.sumarSalientesPorCuenta(usuarioId));
    }

    @Override
    public boolean existsByCuentaId(Long cuentaId) {
        return repository.existsByCuentaOrigen_IdOrCuentaDestino_Id(cuentaId, cuentaId);
    }

    private CuentaEntity cuenta(Long id) {
        return cuentaRepository.findById(id).orElseThrow(() -> new CuentaNotFoundException(id));
    }

    private static Map<Long, BigDecimal> aMapa(List<SumaPorCuenta> sumas) {
        return sumas.stream().collect(Collectors.toMap(SumaPorCuenta::getCuentaId, SumaPorCuenta::getTotal));
    }
}
