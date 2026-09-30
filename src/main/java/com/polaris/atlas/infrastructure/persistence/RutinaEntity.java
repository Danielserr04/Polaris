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

import java.util.ArrayList;
import java.util.List;

/**
 * Mapeo JPA. Las anotaciones de persistencia viven aqui y solo aqui.
 *
 * <p>La coleccion de lineas es EAGER, como la de ComidaEntity: con
 * open-in-view: false un LAZY leido fuera de la transaccion del adapter
 * reventaria, y una rutina nunca se lee sin sus lineas. SUBSELECT carga las
 * lineas de todas las rutinas de un listado con una sola consulta extra.
 * {@code @OrderBy("orden")} las devuelve siempre por orden.
 *
 * <p>cascade ALL + orphanRemoval: guardar la rutina guarda sus lineas, quitar
 * una linea de la coleccion la borra (asi el PUT reemplaza el conjunto) y
 * borrar la rutina borra sus lineas. El unique (usuario_id, nombre) vive en
 * V13__atlas_rutina.sql.
 */
@Entity
@Table(name = "rutina")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RutinaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private boolean activa;

    @Builder.Default
    @OneToMany(mappedBy = "rutina", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("orden ASC")
    private List<RutinaEjercicioEntity> lineas = new ArrayList<>();
}
