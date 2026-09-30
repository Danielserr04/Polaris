package com.polaris.atlas.infrastructure.persistence;

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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapeo JPA. Las anotaciones de persistencia viven aqui y solo aqui.
 *
 * <p>{@code rutinaId} es una columna simple y no una relacion: solo hace falta
 * el id (y el nombre, que SesionJpaAdapter consulta aparte), y una relacion
 * arrastraria la rutina entera con sus lineas.
 *
 * <p>La coleccion de series es EAGER, como la de RutinaEntity: con
 * open-in-view: false un LAZY leido fuera de la transaccion del adapter
 * reventaria, y una sesion nunca se lee sin sus series. SUBSELECT carga las
 * series de todas las sesiones de un listado con una sola consulta extra.
 * {@code @OrderBy("id ASC")} las devuelve en el orden en que se guardaron (el
 * servicio las inserta agrupadas por ejercicio y por numero de serie).
 *
 * <p>cascade ALL + orphanRemoval: guardar la sesion guarda sus series, quitar
 * una de la coleccion la borra (asi el PUT reemplaza el conjunto) y borrar la
 * sesion borra sus series. Las tablas viven en V14__atlas_sesion.sql.
 */
@Entity
@Table(name = "sesion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SesionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "rutina_id")
    private Long rutinaId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "duracion_min")
    private Integer duracionMin;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @Builder.Default
    @OneToMany(mappedBy = "sesion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("id ASC")
    private List<SerieRegistroEntity> series = new ArrayList<>();
}
