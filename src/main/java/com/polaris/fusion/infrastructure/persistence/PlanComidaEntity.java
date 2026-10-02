package com.polaris.fusion.infrastructure.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapeo JPA. Mismo planteamiento que ComidaEntity: lineas EAGER con SUBSELECT
 * y cascade ALL + orphanRemoval. Las lineas salen ordenadas por dia de la
 * semana y momento: los dos son ENUM de MySQL, que ordena por posicion (V31).
 */
@Entity
@Table(name = "plan_comida")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanComidaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false)
    private boolean activo;

    @Builder.Default
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("diaSemana ASC, momento ASC, id ASC")
    private List<PlanComidaLineaEntity> lineas = new ArrayList<>();
}
