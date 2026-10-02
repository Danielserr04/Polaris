package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.application.out.MetaAhorroRepositoryPort;
import com.polaris.kuiper.domain.model.AportacionMeta;
import com.polaris.kuiper.domain.model.MetaAhorro;
import com.polaris.kuiper.domain.model.MetaAhorroFilter;
import com.polaris.shared.testing.IntegracionBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Las consultas de suma de MetaAhorro contra MySQL: la suma de una meta (con
 * retiradas y sin aportaciones), la agrupada del listado, el filtro
 * completada por subconsulta, el orden, el aislamiento entre usuarios, el
 * ON DELETE CASCADE y el unique del nombre.
 */
class MetaAhorroIntegracionTest extends IntegracionBase {

    private static final Long A = 9601L;
    private static final Long B = 9602L;

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private MetaAhorroRepositoryPort metas;

    private MetaAhorro viaje;
    private MetaAhorro coche;
    private MetaAhorro colchon;

    @BeforeEach
    void datos() {
        jdbc.update("delete from aportacion_meta where usuario_id in (?, ?)", A, B);
        jdbc.update("delete from meta_ahorro where usuario_id in (?, ?)", A, B);

        viaje = meta(A, "Viaje", "1000.00", LocalDate.of(2027, 6, 30));
        coche = meta(A, "Coche", "500.00", LocalDate.of(2027, 1, 31));
        colchon = meta(A, "Colchon", "2000.00", null);
        MetaAhorro deB = meta(B, "Viaje", "10.00", null);

        aportar(viaje, "2026-09-01", "300.00");
        aportar(viaje, "2026-09-15", "200.50");
        aportar(viaje, "2026-09-20", "-100.25");
        aportar(coche, "2026-08-01", "500.00");
        aportar(deB, "2026-09-01", "99.00");
    }

    @Test
    @DisplayName("Suma de una meta: aportaciones menos retiradas, con 2 decimales; 0.00 si no tiene ninguna")
    void sumaDeUnaMeta() {
        assertThat(metas.sumaAportaciones(viaje.getId())).isEqualTo(new BigDecimal("400.25"));
        assertThat(metas.sumaAportaciones(colchon.getId())).isEqualTo(new BigDecimal("0.00"));
        assertThat(metas.findById(viaje.getId()).orElseThrow().getImporteActual()).isEqualTo(new BigDecimal("400.25"));
    }

    @Test
    @DisplayName("Listado: solo las del usuario, por fecha limite (sin fecha al final) y cada una con su suma")
    void listadoConSumas() {
        List<MetaAhorro> deA = metas.findAll(A, MetaAhorroFilter.builder().build());

        assertThat(deA).extracting(m -> m.getNombre() + " " + m.getImporteActual().toPlainString())
                .containsExactly("Coche 500.00", "Viaje 400.25", "Colchon 0.00");
        assertThat(deA.get(0).isCompletada()).isTrue();
        assertThat(metas.findAll(B, null)).extracting(m -> m.getImporteActual().toPlainString())
                .containsExactly("99.00");
        assertThat(metas.findAll(9999L, null)).isEmpty();
    }

    @Test
    @DisplayName("Filtro completada: la subconsulta de suma frente al objetivo, contando las que no tienen aportaciones")
    void filtroCompletada() {
        assertThat(metas.findAll(A, MetaAhorroFilter.builder().completada(true).build()))
                .extracting(MetaAhorro::getNombre).containsExactly("Coche");
        assertThat(metas.findAll(A, MetaAhorroFilter.builder().completada(false).build()))
                .extracting(MetaAhorro::getNombre).containsExactly("Viaje", "Colchon");
        // La de B tiene 99 sobre 10: completada, pero no sale para A.
        assertThat(metas.findAll(B, MetaAhorroFilter.builder().completada(true).build())).hasSize(1);
    }

    @Test
    @DisplayName("Historial de la mas reciente a la mas antigua, y borrar una aportacion cambia la suma")
    void historialYBorrado() {
        List<AportacionMeta> historial = metas.findAportaciones(viaje.getId());

        assertThat(historial).extracting(a -> a.getFecha() + " " + a.getImporte().toPlainString())
                .containsExactly("2026-09-20 -100.25", "2026-09-15 200.50", "2026-09-01 300.00");

        metas.deleteAportacionById(historial.get(0).getId());
        assertThat(metas.sumaAportaciones(viaje.getId())).isEqualTo(new BigDecimal("500.50"));
    }

    @Test
    @DisplayName("Borrar la meta borra sus aportaciones (ON DELETE CASCADE)")
    void borradoEnCascada() {
        metas.deleteById(viaje.getId());

        Integer quedan = jdbc.queryForObject("select count(*) from aportacion_meta where meta_id = ?",
                Integer.class, viaje.getId());
        assertThat(quedan).isZero();
        assertThat(metas.findById(viaje.getId())).isEmpty();
    }

    @Test
    @DisplayName("Nombre unico por usuario, sin distinguir mayusculas ni tildes; otro usuario puede repetirlo")
    void nombreUnico() {
        assertThat(metas.findByUsuarioIdAndNombre(A, "VIAJE")).isPresent();
        assertThatThrownBy(() -> meta(A, "viaje", "1.00", null)).isInstanceOf(DataIntegrityViolationException.class);
        assertThat(metas.findByUsuarioIdAndNombre(B, "Viaje")).isPresent();
    }

    private MetaAhorro meta(Long usuarioId, String nombre, String objetivo, LocalDate fechaLimite) {
        return metas.save(MetaAhorro.builder().usuarioId(usuarioId).nombre(nombre)
                .importeObjetivo(new BigDecimal(objetivo)).fechaLimite(fechaLimite).creadaEn(Instant.now()).build());
    }

    private void aportar(MetaAhorro meta, String fecha, String importe) {
        metas.saveAportacion(AportacionMeta.builder().usuarioId(meta.getUsuarioId()).metaId(meta.getId())
                .fecha(LocalDate.parse(fecha)).importe(new BigDecimal(importe)).build());
    }
}
