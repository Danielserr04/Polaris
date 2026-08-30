package com.polaris.odisea.infrastructure.persistence;

import com.polaris.odisea.application.out.EntradaRepositoryPort;
import com.polaris.odisea.domain.model.Entrada;
import com.polaris.odisea.domain.model.EntradaFilter;
import com.polaris.odisea.domain.model.TituloNotFoundException;
import com.polaris.odisea.infrastructure.persistence.mapper.EntradaEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * El unico punto del modulo donde conviven modelo y Entity.
 */
@Component
@RequiredArgsConstructor
public class EntradaJpaAdapter implements EntradaRepositoryPort {

    private final EntradaRepository repository;
    private final TituloRepository tituloRepository;
    private final EntradaEntityMapper mapper;

    /**
     * findById, no getReferenceById: un proxy lazy sin inicializar reventaria
     * al mapear a dominio si repository.save() ya cerro su transaccion (aqui
     * open-in-view: false). findById trae el TituloEntity completo, asi que
     * toDomain() lo lee sin tocar la base de datos otra vez.
     */
    @Override
    public Entrada save(Entrada entrada) {
        TituloEntity titulo = tituloRepository.findById(entrada.getTituloId())
                .orElseThrow(() -> new TituloNotFoundException(entrada.getTituloId()));

        EntradaEntity entity = mapper.toEntity(entrada);
        entity.setTitulo(titulo);

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Entrada> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Entrada> findAll(Long usuarioId, EntradaFilter filter) {
        Specification<EntradaEntity> spec = EntradaSpecifications.from(usuarioId, filter);
        return mapper.toDomainList(repository.findAll(spec));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByTituloId(Long tituloId) {
        return repository.existsByTitulo_Id(tituloId);
    }

    @Override
    public boolean existsByUsuarioIdAndTituloId(Long usuarioId, Long tituloId) {
        return repository.existsByUsuarioIdAndTitulo_Id(usuarioId, tituloId);
    }
}
