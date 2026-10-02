package com.polaris.fusion.infrastructure.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Mapeo JPA. Las anotaciones de persistencia viven aqui y solo aqui.
 *
 * <p>Mismo planteamiento que ComidaEntity: ingredientes EAGER con SUBSELECT
 * (una receta nunca se lee sin ellos, los macros los necesitan) y cascade ALL
 * + orphanRemoval para que el PUT reemplace el conjunto y el borrado se los
 * lleve.
 */
@Entity
@Table(name = "receta")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecetaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Integer raciones;

    @Column(columnDefinition = "TEXT")
    private String instrucciones;

    @Builder.Default
    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    private List<RecetaIngredienteEntity> ingredientes = new ArrayList<>();
}
