package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.domain.model.CategoriaNotFoundException;
import com.polaris.kuiper.domain.model.CuentaNotFoundException;
import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.MovimientoFilter;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.kuiper.infrastructure.persistence.mapper.MovimientoEntityMapper;
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
public class MovimientoJpaAdapter implements MovimientoRepositoryPort {

    private final MovimientoRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final CuentaRepository cuentaRepository;
    private final MovimientoEntityMapper mapper;

    /**
     * findById, no getReferenceById: un proxy lazy sin inicializar reventaria
     * al mapear a dominio si repository.save() ya cerro su transaccion (aqui
     * open-in-view: false). Es la misma razon que en EntradaJpaAdapter.
     */
    @Override
    public Movimiento save(Movimiento movimiento) {
        CategoriaEntity categoria = categoriaRepository.findById(movimiento.getCategoriaId())
                .orElseThrow(() -> new CategoriaNotFoundException(movimiento.getCategoriaId()));

        MovimientoEntity entity = mapper.toEntity(movimiento);
        entity.setCategoria(categoria);
        if (movimiento.getCuentaId() != null) {
            entity.setCuenta(cuentaRepository.findById(movimiento.getCuentaId())
                    .orElseThrow(() -> new CuentaNotFoundException(movimiento.getCuentaId())));
        }

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Movimiento> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Movimiento> findAll(Long usuarioId, MovimientoFilter filter) {
        Specification<MovimientoEntity> spec = MovimientoSpecifications.from(usuarioId, filter);
        Sort orden = Sort.by(Sort.Direction.DESC, "fecha").and(Sort.by(Sort.Direction.DESC, "id"));
        return mapper.toDomainList(repository.findAll(spec, orden));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByCategoriaId(Long categoriaId) {
        return repository.existsByCategoria_Id(categoriaId);
    }

    @Override
    public boolean existsByCuentaId(Long cuentaId) {
        return repository.existsByCuenta_Id(cuentaId);
    }

    @Override
    public Map<Long, BigDecimal> sumarPorCuenta(Long usuarioId, TipoMovimiento tipo) {
        return repository.sumarPorCuenta(usuarioId, tipo).stream()
                .collect(Collectors.toMap(SumaPorCuenta::getCuentaId, SumaPorCuenta::getTotal));
    }
}
