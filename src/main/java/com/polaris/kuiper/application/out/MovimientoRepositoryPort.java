package com.polaris.kuiper.application.out;

import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.MovimientoFilter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de Movimiento, nunca de MovimientoEntity.
 *
 * <p>Papelera: salvo los metodos que la nombran, todo trabaja solo con los
 * movimientos fuera de ella ({@code borradoEn} nulo). Ver
 * docs/decisiones/038-movimiento-papelera-y-duplicar.md.
 */
public interface MovimientoRepositoryPort {

    /** Guarda tambien borradoEn: restaurar es guardar con borradoEn a null. */
    Movimiento save(Movimiento movimiento);

    /** Solo si no esta en la papelera. */
    Optional<Movimiento> findById(Long id);

    /** Solo si esta en la papelera. */
    Optional<Movimiento> findEnPapeleraById(Long id);

    /** Mas reciente primero. Sin los de la papelera. */
    List<Movimiento> findAll(Long usuarioId, MovimientoFilter filter);

    /** La papelera del usuario, el borrado mas reciente primero. */
    List<Movimiento> findPapelera(Long usuarioId);

    /**
     * Manda a la papelera, en una sola sentencia, los {@code ids} del usuario
     * que no esten ya en ella. Devuelve cuantos ha movido.
     */
    int moverAPapelera(Long usuarioId, List<Long> ids, LocalDateTime borradoEn);

    /** Borrado fisico, este o no en la papelera. */
    void deleteById(Long id);

    /** Borra de verdad toda la papelera del usuario. Devuelve cuantos. */
    int vaciarPapelera(Long usuarioId);

    /** Borra de verdad, de todos los usuarios, los que entraron en la papelera antes de {@code limite}. */
    int purgarPapelera(LocalDateTime limite);

    /**
     * Para que CategoriaService pueda impedir borrar o cambiar de tipo una
     * categoria en uso. Los de la papelera no cuentan.
     */
    boolean existsByCategoriaId(Long categoriaId);

    /** Borra de verdad los movimientos de la papelera de esa categoria, para poder borrarla. */
    int deleteEnPapeleraByCategoriaId(Long categoriaId);
}
