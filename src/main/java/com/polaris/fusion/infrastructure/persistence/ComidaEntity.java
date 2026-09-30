package com.polaris.fusion.infrastructure.persistence;

import com.polaris.fusion.domain.model.MomentoComida;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapeo JPA. Las anotaciones de persistencia viven aqui y solo aqui.
 *
 * <p>La coleccion de lineas es EAGER, igual que los *-a-uno de Movimiento y
 * Entrada: con open-in-view: false, un LAZY leido fuera de la transaccion del
 * adapter reventaria con LazyInitializationException, y una comida nunca se
 * lee sin sus lineas (los totales las necesitan). SUBSELECT carga las lineas
 * de todas las comidas de un listado con una sola consulta extra, en vez de
 * una por comida.
 *
 * <p>cascade ALL + orphanRemoval: guardar la comida guarda sus lineas, quitar
 * una linea de la coleccion la borra (asi el PUT reemplaza el conjunto) y
 * borrar la comida borra sus lineas.
 */
@Entity
@Table(name = "comida")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComidaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MomentoComida momento;

    @Builder.Default
    @OneToMany(mappedBy = "comida", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    private List<ComidaLineaEntity> lineas = new ArrayList<>();
}
