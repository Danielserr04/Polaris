package com.polaris.fusion.application.out;

/**
 * Lo que necesitan AlimentoService y RecetaService para no borrar algo que un
 * plan esta usando. Puerto propio: las lineas solo se escriben dentro de su plan.
 */
public interface PlanComidaLineaRepositoryPort {

    /** De CUALQUIER usuario: el catalogo de alimentos es compartido. */
    boolean existsByAlimentoId(Long alimentoId);

    boolean existsByRecetaId(Long recetaId);
}
