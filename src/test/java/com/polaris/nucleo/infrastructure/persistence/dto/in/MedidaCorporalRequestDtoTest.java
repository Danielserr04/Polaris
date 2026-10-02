package com.polaris.nucleo.infrastructure.persistence.dto.in;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Los limites del DTO son los de la columna: un valor que no cabe tiene que
 * ser un 400 de validacion y no un error de MySQL.
 */
class MedidaCorporalRequestDtoTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void abrirValidador() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void cerrarValidador() {
        factory.close();
    }

    private static MedidaCorporalRequestDto conNotas(String notas) {
        return new MedidaCorporalRequestDto(LocalDate.of(2026, 9, 30),
                null, null, new BigDecimal("82.5"), null, null, null, null, null, notas);
    }

    @Test
    @DisplayName("notas nulas o de hasta 65535 caracteres (TEXT) son validas")
    void notasEnElLimiteSonValidas() {
        assertThat(validator.validate(conNotas(null))).isEmpty();
        assertThat(validator.validate(conNotas("x".repeat(65535)))).isEmpty();
    }

    @Test
    @DisplayName("notas de mas de 65535 caracteres se rechazan con mensaje en espanol")
    void notasQueNoCabenSeRechazan() {
        Set<ConstraintViolation<MedidaCorporalRequestDto>> errores = validator.validate(conNotas("x".repeat(65536)));

        assertThat(errores).hasSize(1);
        ConstraintViolation<MedidaCorporalRequestDto> error = errores.iterator().next();
        assertThat(error.getPropertyPath()).hasToString("notas");
        assertThat(error.getMessage()).isEqualTo("maximo 65535 caracteres");
    }

    @Test
    @DisplayName("una medida con mas de 1 decimal, de 4 enteros o no positiva se rechaza")
    void medidaQueNoCabeSeRechaza() {
        for (String cintura : new String[] {"82.55", "1000.0", "0", "-1"}) {
            MedidaCorporalRequestDto dto = new MedidaCorporalRequestDto(LocalDate.of(2026, 9, 30),
                    null, null, new BigDecimal(cintura), null, null, null, null, null, null);

            assertThat(validator.validate(dto)).as(cintura).hasSize(1);
        }
    }
}
