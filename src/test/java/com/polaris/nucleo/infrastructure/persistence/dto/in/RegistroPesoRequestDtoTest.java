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
class RegistroPesoRequestDtoTest {

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

    private static RegistroPesoRequestDto conNotas(String notas) {
        return new RegistroPesoRequestDto(LocalDate.of(2026, 9, 30), new BigDecimal("78.50"), null, notas);
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
        Set<ConstraintViolation<RegistroPesoRequestDto>> errores = validator.validate(conNotas("x".repeat(65536)));

        assertThat(errores).hasSize(1);
        ConstraintViolation<RegistroPesoRequestDto> error = errores.iterator().next();
        assertThat(error.getPropertyPath()).hasToString("notas");
        assertThat(error.getMessage()).isEqualTo("maximo 65535 caracteres");
    }
}
