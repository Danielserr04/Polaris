package com.polaris.odisea.infrastructure.persistence;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Spring Data. Solo la usa EntradaJpaAdapter.
 */
public interface EntradaRepository extends JpaRepository<EntradaEntity, Long>,
        JpaSpecificationExecutor<EntradaEntity> {

    /**
     * El listado con el titulo en la misma sentencia (JOIN). Sin esto, el titulo EAGER de cada
     * entrada se pedia con una sentencia aparte: N+1 (14.001 sentencias con 14.000 entradas).
     */
    @Override
    @EntityGraph(attributePaths = "titulo")
    List<EntradaEntity> findAll(Specification<EntradaEntity> spec);

    boolean existsByTitulo_Id(Long tituloId);

    boolean existsByUsuarioIdAndTitulo_Id(Long usuarioId, Long tituloId);
}
