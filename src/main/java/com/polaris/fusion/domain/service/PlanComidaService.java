package com.polaris.fusion.domain.service;

import com.polaris.fusion.application.in.ActivarPlanComidaInterface;
import com.polaris.fusion.application.in.CreatePlanComidaInterface;
import com.polaris.fusion.application.in.DeletePlanComidaInterface;
import com.polaris.fusion.application.in.GetListaCompraInterface;
import com.polaris.fusion.application.in.GetPlanComidaInterface;
import com.polaris.fusion.application.in.ListPlanComidaInterface;
import com.polaris.fusion.application.in.UpdatePlanComidaInterface;
import com.polaris.fusion.application.out.AlimentoRepositoryPort;
import com.polaris.fusion.application.out.PlanComidaRepositoryPort;
import com.polaris.fusion.application.out.RecetaRepositoryPort;
import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.domain.model.AlimentoNotFoundException;
import com.polaris.fusion.domain.model.ArticuloCompra;
import com.polaris.fusion.domain.model.PlanComida;
import com.polaris.fusion.domain.model.PlanComidaFilter;
import com.polaris.fusion.domain.model.PlanComidaLinea;
import com.polaris.fusion.domain.model.PlanComidaNotFoundException;
import com.polaris.fusion.domain.model.Receta;
import com.polaris.fusion.domain.model.RecetaIngrediente;
import com.polaris.fusion.domain.model.RecetaNotFoundException;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Plan semanal con sus lineas: un solo agregado. Ver
 * docs/decisiones/042-plan-de-comidas-y-lista-de-la-compra.md.
 *
 * <p>Cada linea es un alimento con gramos o una receta propia con raciones.
 * Como mucho un plan activo por usuario. La lista de la compra se calcula al
 * pedirla: suma los gramos de cada alimento, desplegando las recetas en sus
 * ingredientes en proporcion a las raciones.
 */
@Service
@RequiredArgsConstructor
public class PlanComidaService implements
        CreatePlanComidaInterface,
        GetPlanComidaInterface,
        ListPlanComidaInterface,
        UpdatePlanComidaInterface,
        DeletePlanComidaInterface,
        ActivarPlanComidaInterface,
        GetListaCompraInterface {

    static final int MAX_LINEAS = 200;

    private final PlanComidaRepositoryPort repository;
    private final AlimentoRepositoryPort alimentoRepository;
    private final RecetaRepositoryPort recetaRepository;

    /** Un plan nuevo nunca nace activo: se activa aparte. */
    @Override
    public PlanComida create(Long usuarioId, PlanComida plan) {
        prepararLineas(usuarioId, plan);
        plan.setId(null);
        plan.setUsuarioId(usuarioId);
        plan.setActivo(false);
        return repository.save(plan);
    }

    @Override
    public PlanComida get(Long usuarioId, Long id) {
        return getPropio(usuarioId, id);
    }

    @Override
    public List<PlanComida> list(Long usuarioId, PlanComidaFilter filter) {
        return repository.findAll(usuarioId, filter);
    }

    /** Reemplazo completo de nombre y lineas; conserva si estaba activo. */
    @Override
    public PlanComida update(Long usuarioId, Long id, PlanComida plan) {
        PlanComida existente = getPropio(usuarioId, id);
        prepararLineas(usuarioId, plan);
        plan.setId(existente.getId());
        plan.setUsuarioId(existente.getUsuarioId());
        plan.setActivo(existente.isActivo());
        return repository.save(plan);
    }

    @Override
    public void delete(Long usuarioId, Long id) {
        getPropio(usuarioId, id);
        repository.deleteById(id);
    }

    /** Desactiva el resto de planes del usuario y activa este. Idempotente. */
    @Override
    public PlanComida activar(Long usuarioId, Long id) {
        getPropio(usuarioId, id);
        repository.activarUnico(usuarioId, id);
        return getPropio(usuarioId, id);
    }

    /**
     * Gramos por alimento para toda la semana, por nombre. Un alimento suelto
     * suma sus gramos; una receta suma cada ingrediente * raciones / raciones
     * de la receta, con escala 2 y HALF_UP por linea.
     */
    @Override
    public List<ArticuloCompra> getListaCompra(Long usuarioId, Long planId) {
        PlanComida plan = getPropio(usuarioId, planId);
        Map<Long, Alimento> alimentos = new LinkedHashMap<>();
        Map<Long, BigDecimal> gramos = new LinkedHashMap<>();

        for (PlanComidaLinea linea : plan.getLineas()) {
            if (linea.esReceta()) {
                Receta receta = linea.getReceta();
                BigDecimal racionesReceta = BigDecimal.valueOf(receta.getRaciones());
                for (RecetaIngrediente ingrediente : receta.getIngredientes()) {
                    BigDecimal cantidad = ingrediente.getCantidadG().multiply(linea.getRaciones())
                            .divide(racionesReceta, 2, RoundingMode.HALF_UP);
                    sumar(alimentos, gramos, ingrediente.getAlimento(), cantidad);
                }
            } else {
                sumar(alimentos, gramos, linea.getAlimento(), linea.getCantidadG());
            }
        }

        Collator orden = Collator.getInstance(Locale.forLanguageTag("es"));
        return gramos.entrySet().stream()
                .map(e -> {
                    Alimento alimento = alimentos.get(e.getKey());
                    return ArticuloCompra.builder()
                            .alimentoId(alimento.getId())
                            .nombre(alimento.getNombre())
                            .marca(alimento.getMarca())
                            .cantidadG(e.getValue())
                            .build();
                })
                .sorted(Comparator.comparing(ArticuloCompra::getNombre, orden))
                .toList();
    }

    private static void sumar(Map<Long, Alimento> alimentos, Map<Long, BigDecimal> gramos,
                              Alimento alimento, BigDecimal cantidad) {
        alimentos.putIfAbsent(alimento.getId(), alimento);
        gramos.merge(alimento.getId(), cantidad, BigDecimal::add);
    }

    /**
     * Valida cada linea (alimento con gramos o receta con raciones, nunca las
     * dos), comprueba que el alimento existe y que la receta es del usuario
     * (404 si no), anula los ids y fija el usuario.
     */
    private void prepararLineas(Long usuarioId, PlanComida plan) {
        List<PlanComidaLinea> lineas = plan.getLineas();
        if (lineas.size() > MAX_LINEAS) {
            throw new ValidationException("Un plan admite como mucho " + MAX_LINEAS + " lineas");
        }

        for (PlanComidaLinea linea : lineas) {
            boolean conAlimento = linea.getAlimentoId() != null;
            boolean conReceta = linea.getRecetaId() != null;
            if (conAlimento == conReceta) {
                throw new ValidationException("Cada linea del plan lleva un alimento o una receta, no las dos cosas");
            }
            if (conAlimento) {
                if (linea.getCantidadG() == null) {
                    throw new ValidationException("Una linea con alimento necesita los gramos");
                }
                Long alimentoId = linea.getAlimentoId();
                alimentoRepository.findById(alimentoId)
                        .orElseThrow(() -> new AlimentoNotFoundException(alimentoId));
                linea.setRaciones(null);
            } else {
                if (linea.getRaciones() == null) {
                    throw new ValidationException("Una linea con receta necesita las raciones");
                }
                Long recetaId = linea.getRecetaId();
                Receta receta = recetaRepository.findById(recetaId)
                        .orElseThrow(() -> new RecetaNotFoundException(recetaId));
                if (!receta.getUsuarioId().equals(usuarioId)) {
                    throw new RecetaNotFoundException(recetaId);
                }
                linea.setCantidadG(null);
            }
            linea.setId(null);
            linea.setUsuarioId(usuarioId);
        }
    }

    /**
     * 404, no 403, si el id existe pero pertenece a otro usuario: un 403
     * confirmaria que ese id existe.
     */
    private PlanComida getPropio(Long usuarioId, Long id) {
        PlanComida plan = repository.findById(id)
                .orElseThrow(() -> new PlanComidaNotFoundException(id));

        if (!plan.getUsuarioId().equals(usuarioId)) {
            throw new PlanComidaNotFoundException(id);
        }

        return plan;
    }
}
