package com.polaris.odisea.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data. Solo la usa EntradaJpaAdapter.
 */
public interface EntradaRepository extends JpaRepository<EntradaEntity, Long>,
        JpaSpecificationExecutor<EntradaEntity> {

    boolean existsByTitulo_Id(Long tituloId);

    boolean existsByUsuarioIdAndTitulo_Id(Long usuarioId, Long tituloId);
}
