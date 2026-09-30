package com.polaris.atlas.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data. Solo la usa SerieRegistroJpaAdapter, para la comprobacion de
 * uso de un ejercicio; las series se escriben siempre a traves de SesionEntity.
 */
public interface SerieRegistroRepository extends JpaRepository<SerieRegistroEntity, Long> {

    boolean existsByEjercicio_Id(Long ejercicioId);
}
