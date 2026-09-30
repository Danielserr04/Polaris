package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.domain.model.SerieRegistro;
import com.polaris.atlas.domain.model.Sesion;
import com.polaris.atlas.domain.model.SesionFilter;
import com.polaris.atlas.infrastructure.persistence.EjercicioEntity;
import com.polaris.atlas.infrastructure.persistence.SerieRegistroEntity;
import com.polaris.atlas.infrastructure.persistence.SesionEntity;
import com.polaris.atlas.infrastructure.persistence.dto.in.SerieRegistroRequestDto;
import com.polaris.atlas.infrastructure.persistence.dto.in.SesionFilterListDto;
import com.polaris.atlas.infrastructure.persistence.dto.in.SesionRequestDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.SerieRegistroFormDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.SesionFormDto;
import com.polaris.atlas.infrastructure.persistence.dto.out.SesionListDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Los mappers de Sesion y SerieRegistro sobre sus implementaciones generadas
 * por MapStruct. Los de entity y form DTO se apoyan en otros mappers (uses), asi
 * que se prueban con un contexto de Spring minimo que solo contiene esos mappers.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SesionEntityMapperImpl.class, SerieRegistroEntityMapperImpl.class,
        EjercicioEntityMapperImpl.class, SesionFormDtoMapperImpl.class, SerieRegistroFormDtoMapperImpl.class})
class SesionMappersTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 9, 30);

    @Autowired
    private SesionEntityMapper entityMapper;

    @Autowired
    private SerieRegistroEntityMapper serieEntityMapper;

    @Autowired
    private SesionFormDtoMapper formMapper;

    private static Ejercicio pressBanca() {
        return Ejercicio.builder().id(2L).nombre("Press banca").grupoMuscular("Pecho").equipamiento("Barra").build();
    }

    private static Ejercicio dominadas() {
        return Ejercicio.builder().id(3L).usuarioId(1L).nombre("Dominadas").grupoMuscular("Espalda").build();
    }

    private static SerieRegistro serie(Long id, Ejercicio ejercicio, int numero, String peso, String rpe) {
        return SerieRegistro.builder().id(id).usuarioId(1L).ejercicioId(ejercicio.getId()).ejercicio(ejercicio)
                .numeroSerie(numero).reps(8).pesoKg(new BigDecimal(peso))
                .rpe(rpe == null ? null : new BigDecimal(rpe)).build();
    }

    private static Sesion sesion() {
        return Sesion.builder().id(8L).usuarioId(1L).rutinaId(4L).rutinaNombre("Push").fecha(FECHA).duracionMin(65)
                .notas("Buen dia").series(List.of(serie(20L, pressBanca(), 1, "82.5", "8.5"),
                        serie(21L, dominadas(), 1, "0", null))).build();
    }

    @Test
    @DisplayName("entity -> dominio de la sesion trae las series con su ejercicio, ejercicioId, peso y rpe exactos, y usuarioId por serie")
    void entityADominio() {
        EjercicioEntity ejercicio = EjercicioEntity.builder().id(2L).nombre("Press banca").grupoMuscular("Pecho")
                .equipamiento("Barra").build();
        SerieRegistroEntity serieEntity = SerieRegistroEntity.builder().id(20L).usuarioId(1L).ejercicio(ejercicio)
                .numeroSerie(1).reps(8).pesoKg(new BigDecimal("82.50")).rpe(new BigDecimal("8.5")).build();
        SesionEntity entity = SesionEntity.builder().id(8L).usuarioId(1L).rutinaId(4L).fecha(FECHA).duracionMin(65)
                .notas("Buen dia").series(List.of(serieEntity)).build();

        Sesion dominio = entityMapper.toDomain(entity);

        assertThat(dominio.getUsuarioId()).isEqualTo(1L);
        assertThat(dominio.getRutinaId()).isEqualTo(4L);
        assertThat(dominio.getRutinaNombre()).as("el nombre lo rellena el adaptador").isNull();
        assertThat(dominio.getFecha()).isEqualTo(FECHA);
        assertThat(dominio.getDuracionMin()).isEqualTo(65);
        assertThat(dominio.getNotas()).isEqualTo("Buen dia");
        assertThat(dominio.getNumeroSeries()).isEqualTo(1);
        SerieRegistro s = dominio.getSeries().get(0);
        assertThat(s.getUsuarioId()).isEqualTo(1L);
        assertThat(s.getEjercicioId()).isEqualTo(2L);
        assertThat(s.getEjercicio()).usingRecursiveComparison().isEqualTo(pressBanca());
        assertThat(s.getNumeroSerie()).isEqualTo(1);
        assertThat(s.getReps()).isEqualTo(8);
        assertThat(s.getPesoKg()).isEqualTo(new BigDecimal("82.50"));
        assertThat(s.getRpe()).isEqualTo(new BigDecimal("8.5"));
        assertThat(entityMapper.toDomainList(List.of(entity))).hasSize(1);
    }

    @Test
    @DisplayName("entity -> dominio de un entreno libre deja rutinaId y rpe en null")
    void entityLibre() {
        SesionEntity entity = SesionEntity.builder().id(8L).usuarioId(1L).fecha(FECHA).series(List.of()).build();

        Sesion dominio = entityMapper.toDomain(entity);

        assertThat(dominio.getRutinaId()).isNull();
        assertThat(dominio.getDuracionMin()).isNull();
        assertThat(dominio.getNotas()).isNull();
    }

    @Test
    @DisplayName("dominio -> entity de la serie apunta al ejercicio solo por su id y deja la sesion sin rellenar")
    void serieDominioAEntity() {
        SerieRegistroEntity entity = serieEntityMapper.toEntity(serie(20L, pressBanca(), 3, "100", "9"));

        assertThat(entity.getEjercicio()).isNotNull();
        assertThat(entity.getEjercicio().getId()).isEqualTo(2L);
        assertThat(entity.getSesion()).isNull();
        assertThat(entity.getUsuarioId()).isEqualTo(1L);
        assertThat(entity.getNumeroSerie()).isEqualTo(3);
        assertThat(entity.getReps()).isEqualTo(8);
        assertThat(entity.getPesoKg()).isEqualTo(new BigDecimal("100"));
        assertThat(entity.getRpe()).isEqualTo(new BigDecimal("9"));
        assertThat(serieEntityMapper.toEntityList(List.of(serie(20L, pressBanca(), 1, "5", null)))).hasSize(1);
    }

    @Test
    @DisplayName("dominio -> entity de la sesion lleva sus series y su rutinaId")
    void sesionDominioAEntity() {
        SesionEntity entity = entityMapper.toEntity(sesion());

        assertThat(entity.getId()).isEqualTo(8L);
        assertThat(entity.getRutinaId()).isEqualTo(4L);
        assertThat(entity.getFecha()).isEqualTo(FECHA);
        assertThat(entity.getSeries()).hasSize(2);
        assertThat(entity.getSeries()).extracting(s -> s.getEjercicio().getId()).containsExactly(2L, 3L);
    }

    @Test
    @DisplayName("request -> dominio no rellena id, usuarioId ni rutinaNombre (ni en las series) y conserva el peso y el rpe exactos")
    void requestNoTrae() {
        SesionRequestDtoMapper mapper = Mappers.getMapper(SesionRequestDtoMapper.class);

        Sesion dominio = mapper.toDomain(new SesionRequestDto(4L, FECHA, 65, "Buen dia", List.of(
                new SerieRegistroRequestDto(2L, 1, 8, new BigDecimal("82.5"), new BigDecimal("8.5")),
                new SerieRegistroRequestDto(3L, 1, 10, new BigDecimal("0"), null))));

        assertThat(dominio.getId()).isNull();
        assertThat(dominio.getUsuarioId()).isNull();
        assertThat(dominio.getRutinaNombre()).isNull();
        assertThat(dominio.getRutinaId()).isEqualTo(4L);
        assertThat(dominio.getFecha()).isEqualTo(FECHA);
        assertThat(dominio.getDuracionMin()).isEqualTo(65);
        assertThat(dominio.getNotas()).isEqualTo("Buen dia");
        assertThat(dominio.getSeries()).hasSize(2);
        assertThat(dominio.getSeries()).allSatisfy(s -> {
            assertThat(s.getId()).isNull();
            assertThat(s.getUsuarioId()).isNull();
            assertThat(s.getEjercicio()).isNull();
        });
        assertThat(dominio.getSeries()).extracting(SerieRegistro::getEjercicioId).containsExactly(2L, 3L);
        assertThat(dominio.getSeries()).extracting(SerieRegistro::getNumeroSerie).containsExactly(1, 1);
        assertThat(dominio.getSeries()).extracting(SerieRegistro::getReps).containsExactly(8, 10);
        assertThat(dominio.getSeries()).extracting(SerieRegistro::getPesoKg)
                .containsExactly(new BigDecimal("82.5"), new BigDecimal("0"));
        assertThat(dominio.getSeries()).extracting(SerieRegistro::getRpe)
                .containsExactly(new BigDecimal("8.5"), null);
    }

    @Test
    @DisplayName("request -> dominio de un entreno libre (sin rutina, duracion ni notas)")
    void requestLibre() {
        SesionRequestDtoMapper mapper = Mappers.getMapper(SesionRequestDtoMapper.class);

        Sesion dominio = mapper.toDomain(new SesionRequestDto(null, FECHA, null, null, List.of(
                new SerieRegistroRequestDto(2L, 1, 8, new BigDecimal("80"), null))));

        assertThat(dominio.getRutinaId()).isNull();
        assertThat(dominio.getDuracionMin()).isNull();
        assertThat(dominio.getNotas()).isNull();
    }

    @Test
    @DisplayName("dominio -> form DTO lleva las series con nombre y grupo del ejercicio, peso y rpe, y nunca el usuarioId")
    void formDto() {
        SesionFormDto dto = formMapper.toFormDto(sesion());

        assertThat(dto.id()).isEqualTo(8L);
        assertThat(dto.rutinaId()).isEqualTo(4L);
        assertThat(dto.rutinaNombre()).isEqualTo("Push");
        assertThat(dto.fecha()).isEqualTo(FECHA);
        assertThat(dto.duracionMin()).isEqualTo(65);
        assertThat(dto.notas()).isEqualTo("Buen dia");
        assertThat(dto.series()).hasSize(2);
        SerieRegistroFormDto primera = dto.series().get(0);
        assertThat(primera.id()).isEqualTo(20L);
        assertThat(primera.ejercicioId()).isEqualTo(2L);
        assertThat(primera.ejercicioNombre()).isEqualTo("Press banca");
        assertThat(primera.ejercicioGrupoMuscular()).isEqualTo("Pecho");
        assertThat(primera.numeroSerie()).isEqualTo(1);
        assertThat(primera.reps()).isEqualTo(8);
        assertThat(primera.pesoKg()).isEqualTo(new BigDecimal("82.5"));
        assertThat(primera.rpe()).isEqualTo(new BigDecimal("8.5"));
        assertThat(dto.series().get(1).rpe()).isNull();
    }

    @Test
    @DisplayName("dominio -> list DTO lleva rutina, duracion y numero de series, no las series, y funciona sobre listas")
    void listDto() {
        SesionListDtoMapper mapper = Mappers.getMapper(SesionListDtoMapper.class);

        SesionListDto dto = mapper.toListDto(sesion());

        assertThat(dto.id()).isEqualTo(8L);
        assertThat(dto.fecha()).isEqualTo(FECHA);
        assertThat(dto.rutinaId()).isEqualTo(4L);
        assertThat(dto.rutinaNombre()).isEqualTo("Push");
        assertThat(dto.duracionMin()).isEqualTo(65);
        assertThat(dto.numeroSeries()).isEqualTo(2);
        assertThat(mapper.toListDtoList(List.of(sesion(), sesion()))).hasSize(2);
    }

    @Test
    @DisplayName("filtro: query params -> filtro de dominio")
    void filtro() {
        SesionFilterMapper mapper = Mappers.getMapper(SesionFilterMapper.class);

        SesionFilter filtro = mapper.toFilter(new SesionFilterListDto(FECHA.minusDays(7), FECHA, 4L));
        SesionFilter vacio = mapper.toFilter(new SesionFilterListDto(null, null, null));

        assertThat(filtro.getDesde()).isEqualTo(FECHA.minusDays(7));
        assertThat(filtro.getHasta()).isEqualTo(FECHA);
        assertThat(filtro.getRutinaId()).isEqualTo(4L);
        assertThat(vacio.getDesde()).isNull();
        assertThat(vacio.getHasta()).isNull();
        assertThat(vacio.getRutinaId()).isNull();
    }
}
