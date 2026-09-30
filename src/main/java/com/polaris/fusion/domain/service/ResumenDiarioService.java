package com.polaris.fusion.domain.service;

import com.polaris.fusion.application.in.GetResumenDiarioInterface;
import com.polaris.fusion.application.out.ComidaRepositoryPort;
import com.polaris.fusion.application.out.ObjetivoNutricionalRepositoryPort;
import com.polaris.fusion.domain.model.Comida;
import com.polaris.fusion.domain.model.ComidaFilter;
import com.polaris.fusion.domain.model.MacroResumen;
import com.polaris.fusion.domain.model.Macros;
import com.polaris.fusion.domain.model.ObjetivoNutricional;
import com.polaris.fusion.domain.model.ResumenDiario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Suma en Java los totales de las comidas del dia (que a su vez suman lineas
 * calculadas al vuelo, con el redondeo de Macros) y los compara con el objetivo
 * vigente ese dia. Ver docs/decisiones/021-resumen-diario-fusion.md.
 */
@Service
@RequiredArgsConstructor
public class ResumenDiarioService implements GetResumenDiarioInterface {

    private static final int ESCALA = 2;
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final ComidaRepositoryPort comidaRepository;
    private final ObjetivoNutricionalRepositoryPort objetivoRepository;

    @Override
    public ResumenDiario get(Long usuarioId, LocalDate fecha) {
        List<Comida> comidas = comidaRepository.findAll(usuarioId, ComidaFilter.builder().fecha(fecha).build());
        Macros consumido = comidas.stream()
                .map(Comida::getTotales)
                .reduce(Macros.CERO, Macros::plus);

        // Sin objetivo vigente no es un error: se devuelve lo consumido sin comparar.
        ObjetivoNutricional objetivo = objetivoRepository.findVigente(usuarioId, fecha).orElse(null);

        return ResumenDiario.builder()
                .fecha(fecha)
                .objetivoVigenteDesde(objetivo == null ? null : objetivo.getVigenteDesde())
                .kcal(macro(consumido.kcal(), objetivo == null ? null : objetivo.getKcalDiarias()))
                .proteinas(macro(consumido.proteinas(), objetivo == null ? null : objetivo.getProteinasObj()))
                .carbohidratos(macro(consumido.carbohidratos(), objetivo == null ? null : objetivo.getCarbosObj()))
                .grasas(macro(consumido.grasas(), objetivo == null ? null : objetivo.getGrasasObj()))
                .build();
    }

    private MacroResumen macro(BigDecimal consumido, Integer objetivoInt) {
        if (objetivoInt == null) {
            return MacroResumen.builder().consumido(consumido).build();
        }
        BigDecimal objetivo = BigDecimal.valueOf(objetivoInt).setScale(ESCALA);
        // Objetivo 0 es valido (macro sin meta): no hay porcentaje que calcular.
        BigDecimal porcentaje = objetivo.signum() == 0
                ? null
                : consumido.multiply(CIEN).divide(objetivo, ESCALA, RoundingMode.HALF_UP);
        return MacroResumen.builder()
                .consumido(consumido)
                .objetivo(objetivo)
                .restante(objetivo.subtract(consumido))
                .porcentaje(porcentaje)
                .build();
    }
}
