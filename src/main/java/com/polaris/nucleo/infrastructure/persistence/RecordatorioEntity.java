package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.domain.model.TipoRecordatorio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Mapeo JPA. Las anotaciones de persistencia viven aqui y solo aqui.
 *
 * <p>dias son los dias ISO de la semana separados por comas ("1,3,5" es lunes,
 * miercoles y viernes); RecordatorioEntityMapper lo traduce a DayOfWeek. Ver
 * V35__nucleo_recordatorio.sql.
 */
@Entity
@Table(name = "recordatorio")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordatorioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRecordatorio tipo;

    @Column(nullable = false)
    private boolean activo;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false, length = 13)
    private String dias;

    @Column(name = "avisado_en")
    private LocalDate avisadoEn;

    @Column(name = "descartado_en")
    private LocalDate descartadoEn;
}
