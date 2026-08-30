package com.polaris.odisea.infrastructure.persistence;

import com.polaris.odisea.domain.model.FuenteExterna;
import com.polaris.odisea.domain.model.TipoContenido;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Mapeo JPA. Las anotaciones de persistencia viven aqui y solo aqui.
 *
 * <p>Indice unico en (fuente_externa, id_externo) para no duplicar una ficha
 * al importar del catalogo externo (B3). Los MANUAL dejan id_externo a NULL,
 * y en MySQL los NULL no colisionan en un indice unico.
 */
@Entity
@Table(name = "titulo", uniqueConstraints = @UniqueConstraint(columnNames = {"fuente_externa", "id_externo"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TituloEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoContenido tipo;

    @Column(nullable = false)
    private String titulo;

    @Column(name = "titulo_original")
    private String tituloOriginal;

    private Integer anio;

    @Column(columnDefinition = "TEXT")
    private String sinopsis;

    @Column(name = "imagen_url")
    private String imagenUrl;

    /** Separados por coma, como dice docs/modelo-datos.md. */
    private String generos;

    @Column(name = "duracion_min")
    private Integer duracionMin;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuente_externa", nullable = false)
    private FuenteExterna fuenteExterna;

    @Column(name = "id_externo")
    private String idExterno;
}
