package com.polaris.auth.infrastructure.persistence.mapper;

import com.polaris.auth.domain.model.Usuario;
import com.polaris.auth.infrastructure.persistence.UsuarioEntity;
import com.polaris.auth.infrastructure.persistence.dto.out.UsuarioFormDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/** Los mappers de Usuario sobre sus implementaciones generadas por MapStruct. */
class UsuarioMappersTest {

    private static final Instant CREADO = Instant.parse("2026-09-01T10:00:00Z");

    private static Usuario usuario(String passwordHash, String googleId) {
        return Usuario.builder().id(1L).username("daniel").email("daniel@example.com").nombre("Daniel")
                .passwordHash(passwordHash).emailVerificado(true).googleId(googleId)
                .avatarUrl("http://img/a.png").creadoEn(CREADO).build();
    }

    @Test
    @DisplayName("entity <-> dominio conserva todos los campos, incluidos hash y googleId")
    void entityIdaYVuelta() {
        UsuarioEntityMapper mapper = Mappers.getMapper(UsuarioEntityMapper.class);

        UsuarioEntity entity = mapper.toEntity(usuario("hash", "google-sub"));
        Usuario vuelta = mapper.toDomain(entity);

        assertThat(entity.getPasswordHash()).isEqualTo("hash");
        assertThat(entity.getGoogleId()).isEqualTo("google-sub");
        assertThat(vuelta).usingRecursiveComparison().isEqualTo(usuario("hash", "google-sub"));
    }

    @Test
    @DisplayName("form DTO deriva tieneGoogle y tienePassword como booleanos y no expone hash ni googleId")
    void formDtoConAmbosLogins() {
        UsuarioFormDto dto = Mappers.getMapper(UsuarioFormDtoMapper.class).toFormDto(usuario("hash", "google-sub"));

        assertThat(dto).isEqualTo(new UsuarioFormDto(1L, "daniel", "daniel@example.com", "Daniel",
                "http://img/a.png", CREADO, true, true, true));
    }

    @Test
    @DisplayName("form DTO marca a false lo que el usuario no tiene")
    void formDtoSinGoogleNiPassword() {
        UsuarioFormDtoMapper mapper = Mappers.getMapper(UsuarioFormDtoMapper.class);

        UsuarioFormDto soloNativo = mapper.toFormDto(usuario("hash", null));
        UsuarioFormDto soloGoogle = mapper.toFormDto(usuario(null, "google-sub"));

        assertThat(soloNativo.tienePassword()).isTrue();
        assertThat(soloNativo.tieneGoogle()).isFalse();
        assertThat(soloGoogle.tienePassword()).isFalse();
        assertThat(soloGoogle.tieneGoogle()).isTrue();
    }
}
