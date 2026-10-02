package com.polaris.fusion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Un objetivo propuesto y de donde sale. No se guarda: el usuario lo revisa y,
 * si le vale, crea un ObjetivoNutricional con esos numeros. Ver
 * docs/decisiones/046-calculo-del-objetivo-desde-el-perfil.md.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculoObjetivo {

    private TipoObjetivo tipo;
    private NivelActividad nivelActividad;
    private Sexo sexo;
    private Integer edad;
    private Integer alturaCm;
    private BigDecimal pesoKg;
    /** Dia del peso usado; null si el peso llego en la peticion. */
    private LocalDate pesoFecha;
    /** Gasto basal (Mifflin-St Jeor), en kcal. */
    private Integer tmb;
    /** Gasto total diario: tmb por el factor de actividad. */
    private Integer gastoTotal;
    private Integer kcalDiarias;
    private Integer proteinasObj;
    private Integer carbosObj;
    private Integer grasasObj;
}
