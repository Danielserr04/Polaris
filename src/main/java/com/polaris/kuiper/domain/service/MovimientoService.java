package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.in.ComprobarPresupuestoInterface;
import com.polaris.kuiper.application.in.CreateMovimientoInterface;
import com.polaris.kuiper.application.in.DeleteMovimientoInterface;
import com.polaris.kuiper.application.in.DuplicarMovimientoInterface;
import com.polaris.kuiper.application.in.GetMovimientoInterface;
import com.polaris.kuiper.application.in.ListMovimientoInterface;
import com.polaris.kuiper.application.in.UpdateMovimientoInterface;
import com.polaris.kuiper.application.out.CategoriaRepositoryPort;
import com.polaris.kuiper.application.out.CuentaRepositoryPort;
import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.CategoriaNotFoundException;
import com.polaris.kuiper.domain.model.CuentaNotFoundException;
import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.MovimientoFilter;
import com.polaris.kuiper.domain.model.MovimientoNotFoundException;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * La categoria tiene que ser del usuario y de su mismo tipo. Ver
 * docs/decisiones/012-movimiento-categoria-mismo-tipo.md.
 *
 * <p>Borrar manda a la papelera, y todo lo demas (get, update, duplicar) solo
 * ve los movimientos fuera de ella: uno en la papelera es un 404 aqui. La
 * papelera en si la lleva MovimientoPapeleraService. Ver
 * docs/decisiones/038-movimiento-papelera-y-duplicar.md.
 *
 * <p>Despues de guardar un gasto se comprueba su presupuesto mensual por si
 * hay que avisar (docs/decisiones/040-notificaciones-de-kuiper.md). Esa
 * comprobacion nunca lanza.
 *
 * <p>La cuenta es opcional; si viene, tiene que ser del usuario. Ver
 * docs/decisiones/039-cuentas-y-transferencias.md.
 */
@Service
@RequiredArgsConstructor
public class MovimientoService implements
        CreateMovimientoInterface,
        GetMovimientoInterface,
        ListMovimientoInterface,
        UpdateMovimientoInterface,
        DeleteMovimientoInterface,
        DuplicarMovimientoInterface {

    private final MovimientoRepositoryPort repository;
    private final CategoriaRepositoryPort categoriaRepository;
    private final ComprobarPresupuestoInterface comprobarPresupuesto;
    private final CuentaRepositoryPort cuentaRepository;

    @Override
    public Movimiento create(Long usuarioId, Movimiento movimiento) {
        validarCategoria(usuarioId, movimiento);
        validarCuenta(usuarioId, movimiento.getCuentaId());
        movimiento.setId(null);
        movimiento.setUsuarioId(usuarioId);
        return avisarSiEsGasto(repository.save(movimiento));
    }

    @Override
    public Movimiento get(Long usuarioId, Long id) {
        return getPropio(usuarioId, id);
    }

    @Override
    public List<Movimiento> list(Long usuarioId, MovimientoFilter filter) {
        return repository.findAll(usuarioId, filter);
    }

    @Override
    public Movimiento update(Long usuarioId, Long id, Movimiento movimiento) {
        Movimiento existente = getPropio(usuarioId, id);
        validarCategoria(usuarioId, movimiento);
        validarCuenta(usuarioId, movimiento.getCuentaId());
        movimiento.setId(existente.getId());
        movimiento.setUsuarioId(existente.getUsuarioId());
        return avisarSiEsGasto(repository.save(movimiento));
    }

    @Override
    public void delete(Long usuarioId, Long id) {
        getPropio(usuarioId, id);
        repository.moverAPapelera(usuarioId, List.of(id), LocalDateTime.now());
    }

    /**
     * Copia todo salvo el id y la fecha. La copia no es {@code recurrente}:
     * esa marca dice que lo genero un Recurrente, y esta la crea el usuario.
     */
    @Override
    public Movimiento duplicar(Long usuarioId, Long id, LocalDate fecha) {
        Movimiento original = getPropio(usuarioId, id);
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        if (dia.isAfter(LocalDate.now())) {
            throw new ValidationException("La fecha no puede ser futura");
        }

        Movimiento copia = Movimiento.builder()
                .usuarioId(usuarioId)
                .fecha(dia)
                .importe(original.getImporte())
                .tipo(original.getTipo())
                .categoriaId(original.getCategoriaId())
                .concepto(original.getConcepto())
                .metodoPago(original.getMetodoPago())
                .cuentaId(original.getCuentaId())
                .recurrente(false)
                .build();
        validarCategoria(usuarioId, copia);
        return avisarSiEsGasto(repository.save(copia));
    }

    private Movimiento avisarSiEsGasto(Movimiento guardado) {
        if (guardado.getTipo() == TipoMovimiento.GASTO) {
            comprobarPresupuesto.comprobar(guardado.getUsuarioId(), guardado.getCategoriaId(), guardado.getFecha());
        }
        return guardado;
    }

    /**
     * 404 si la categoria no existe o es de otro usuario (no se distingue, por
     * lo mismo que en getPropio). 400 si existe pero es de otro tipo.
     */
    private void validarCategoria(Long usuarioId, Movimiento movimiento) {
        Long categoriaId = movimiento.getCategoriaId();
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .filter(c -> c.getUsuarioId().equals(usuarioId))
                .orElseThrow(() -> new CategoriaNotFoundException(categoriaId));

        if (categoria.getTipo() != movimiento.getTipo()) {
            throw new ValidationException("El tipo del movimiento no coincide con el de su categoria");
        }
    }

    /** 404 si la cuenta no existe o es de otro usuario. Sin cuenta no hay nada que comprobar. */
    private void validarCuenta(Long usuarioId, Long cuentaId) {
        if (cuentaId == null) {
            return;
        }
        cuentaRepository.findById(cuentaId)
                .filter(c -> c.getUsuarioId().equals(usuarioId))
                .orElseThrow(() -> new CuentaNotFoundException(cuentaId));
    }

    /**
     * 404, no 403, si el id existe pero pertenece a otro usuario: un 403
     * confirmaria que ese id existe.
     */
    private Movimiento getPropio(Long usuarioId, Long id) {
        Movimiento movimiento = repository.findById(id)
                .orElseThrow(() -> new MovimientoNotFoundException(id));

        if (!movimiento.getUsuarioId().equals(usuarioId)) {
            throw new MovimientoNotFoundException(id);
        }

        return movimiento;
    }
}
