package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.domain.model.CuentaFilter;
import com.polaris.kuiper.domain.model.TipoCuenta;
import com.polaris.kuiper.domain.model.TransferenciaFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.polaris.shared.persistence.CriteriaFalsa.describir;
import static org.assertj.core.api.Assertions.assertThat;

/** Cuenta y Transferencia: el filtro por usuario va siempre y la cuenta casa como origen o destino. */
class CuentaTransferenciaSpecificationsTest {

    @Test
    @DisplayName("cuenta: sin filtro solo usuario; con filtro, archivada y tipo")
    void cuenta() {
        assertThat(describir(CuentaSpecifications.from(1L, null))).isEqualTo("equal(usuarioId, 1)");
        assertThat(describir(CuentaSpecifications.from(1L,
                CuentaFilter.builder().archivada(false).tipo(TipoCuenta.AHORRO).build()))).isEqualTo(
                "and(and(equal(usuarioId, 1), equal(archivada, false)), equal(tipo, AHORRO))");
    }

    @Test
    @DisplayName("transferencia: sin filtro solo usuario")
    void transferenciaSinFiltro() {
        assertThat(describir(TransferenciaSpecifications.from(1L, null))).isEqualTo("equal(usuarioId, 1)");
        assertThat(describir(TransferenciaSpecifications.from(1L, TransferenciaFilter.builder().build())))
                .isEqualTo("equal(usuarioId, 1)");
    }

    @Test
    @DisplayName("transferencia: rango inclusivo y la cuenta como origen o como destino")
    void transferenciaFiltros() {
        TransferenciaFilter filtro = TransferenciaFilter.builder().desde(LocalDate.of(2026, 9, 1))
                .hasta(LocalDate.of(2026, 9, 30)).cuentaId(3L).build();

        assertThat(describir(TransferenciaSpecifications.from(1L, filtro))).isEqualTo(
                "and(and(and(equal(usuarioId, 1), greaterThanOrEqualTo(fecha, 2026-09-01)), "
                        + "lessThanOrEqualTo(fecha, 2026-09-30)), "
                        + "or(equal(cuentaOrigen.id, 3), equal(cuentaDestino.id, 3)))");
    }
}
