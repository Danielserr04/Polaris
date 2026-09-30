package com.polaris.atlas.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data. Solo la usa RutinaEjercicioJpaAdapter, para la comprobacion de
 * uso de un ejercicio; las lineas se escriben siempre a traves de RutinaEntity.
 */
public interface RutinaEjercicioRepository extends JpaRepository<RutinaEjercicioEntity, Long> {

    boolean existsByEjercicio_Id(Long ejercicioId);
}
