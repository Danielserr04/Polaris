package com.polaris.fusion.domain.service;

import com.polaris.fusion.application.in.CalcularObjetivoNutricionalInterface;
import com.polaris.fusion.application.out.DatosCorporalesPort;
import com.polaris.fusion.application.out.PesoCorporalPort;
import com.polaris.fusion.domain.model.CalculoObjetivo;
import com.polaris.fusion.domain.model.DatosCorporales;
import com.polaris.fusion.domain.model.NivelActividad;
import com.polaris.fusion.domain.model.PesoCorporal;
import com.polaris.fusion.domain.model.PesoCorporalFilter;
import com.polaris.fusion.domain.model.Sexo;
import com.polaris.fusion.domain.model.TipoObjetivo;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Propone kcal y macros a partir del perfil y el ultimo peso, sin guardar
 * nada. Ver docs/decisiones/043-calculo-del-objetivo-desde-el-perfil.md.
 *
 * <ul>
 *   <li>Gasto basal, Mifflin-St Jeor: 10 * peso + 6.25 * altura - 5 * edad, +5 hombre / -161 mujer.</li>
 *   <li>Gasto total = basal * factor del nivel de actividad.</li>
 *   <li>Kcal = gasto total + ajuste del tipo (-500, 0, +300), entre 500 y 10000.</li>
 *   <li>Proteinas 2 g por kg; grasas el 25 % de las kcal; carbohidratos, el resto.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class CalculoObjetivoService implements CalcularObjetivoNutricionalInterface {

    static final int KCAL_MIN = 500;
    static final int KCAL_MAX = 10000;
    static final int MACRO_MAX = 1000;

    private final DatosCorporalesPort datosCorporalesPort;
    private final PesoCorporalPort pesoCorporalPort;

    @Override
    public CalculoObjetivo calcular(Long usuarioId, LocalDate hoy, TipoObjetivo tipo,
                                    NivelActividad nivelActividad, BigDecimal pesoKg) {
        DatosCorporales datos = datosCorporalesPort.find(usuarioId).orElseGet(DatosCorporales::new);
        NivelActividad nivel = nivelActividad != null ? nivelActividad : datos.getNivelActividad();

        PesoCorporal ultimoPeso = pesoKg != null ? null : ultimoPeso(usuarioId, hoy);
        BigDecimal peso = pesoKg != null ? pesoKg : ultimoPeso != null ? ultimoPeso.getPesoKg() : null;

        List<String> faltan = new ArrayList<>();
        if (datos.getAlturaCm() == null) faltan.add("la altura");
        if (datos.getFechaNacimiento() == null) faltan.add("la fecha de nacimiento");
        if (datos.getSexo() == null) faltan.add("el sexo");
        if (nivel == null) faltan.add("el nivel de actividad");
        if (peso == null) faltan.add("un peso registrado");
        if (!faltan.isEmpty()) {
            throw new ValidationException("Para calcular el objetivo falta " + enumerar(faltan)
                    + ". Completalo en tu perfil.");
        }

        int edad = Period.between(datos.getFechaNacimiento(), hoy).getYears();
        double w = peso.doubleValue();
        double tmb = 10 * w + 6.25 * datos.getAlturaCm() - 5 * edad + (datos.getSexo() == Sexo.MUJER ? -161 : 5);
        double gastoTotal = tmb * nivel.getFactor().doubleValue();

        int kcal = limitar((int) Math.round(gastoTotal + tipo.getAjusteKcal()), KCAL_MIN, KCAL_MAX);
        int proteinas = limitar((int) Math.round(w * 2.0), 0, MACRO_MAX);
        int grasas = limitar((int) Math.round(kcal * 0.25 / 9.0), 0, MACRO_MAX);
        int carbos = limitar((int) Math.round((kcal - proteinas * 4 - grasas * 9) / 4.0), 0, MACRO_MAX);

        return CalculoObjetivo.builder()
                .tipo(tipo)
                .nivelActividad(nivel)
                .sexo(datos.getSexo())
                .edad(edad)
                .alturaCm(datos.getAlturaCm())
                .pesoKg(peso)
                .pesoFecha(ultimoPeso != null ? ultimoPeso.getFecha() : null)
                .tmb((int) Math.round(tmb))
                .gastoTotal((int) Math.round(gastoTotal))
                .kcalDiarias(kcal)
                .proteinasObj(proteinas)
                .carbosObj(carbos)
                .grasasObj(grasas)
                .build();
    }

    /** El mas reciente hasta hoy (el puerto devuelve el mas reciente primero). */
    private PesoCorporal ultimoPeso(Long usuarioId, LocalDate hoy) {
        List<PesoCorporal> pesos = pesoCorporalPort.findAll(usuarioId, PesoCorporalFilter.builder().hasta(hoy).build());
        return pesos.isEmpty() ? null : pesos.get(0);
    }

    /** "a", "a y b", "a, b y c". */
    private static String enumerar(List<String> partes) {
        if (partes.size() == 1) {
            return partes.get(0);
        }
        return String.join(", ", partes.subList(0, partes.size() - 1)) + " y " + partes.get(partes.size() - 1);
    }

    private static int limitar(int valor, int min, int max) {
        return Math.max(min, Math.min(max, valor));
    }
}
