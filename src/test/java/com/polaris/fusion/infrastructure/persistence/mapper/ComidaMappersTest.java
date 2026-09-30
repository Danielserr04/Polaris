package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.domain.model.Comida;
import com.polaris.fusion.domain.model.ComidaFilter;
import com.polaris.fusion.domain.model.ComidaLinea;
import com.polaris.fusion.domain.model.FuenteAlimento;
import com.polaris.fusion.domain.model.MomentoComida;
import com.polaris.fusion.infrastructure.persistence.AlimentoEntity;
import com.polaris.fusion.infrastructure.persistence.ComidaEntity;
import com.polaris.fusion.infrastructure.persistence.ComidaLineaEntity;
import com.polaris.fusion.infrastructure.persistence.dto.in.ComidaFilterListDto;
import com.polaris.fusion.infrastructure.persistence.dto.in.ComidaLineaRequestDto;
import com.polaris.fusion.infrastructure.persistence.dto.in.ComidaRequestDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.ComidaFormDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.ComidaLineaFormDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.ComidaListDto;
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
 * Los mappers de Comida y ComidaLinea sobre sus implementaciones generadas por
 * MapStruct. Los de entity y los de form DTO se apoyan en otros mappers
 * (uses), asi que se prueban con un contexto de Spring minimo que solo
 * contiene esos mappers. Los macros no se guardan: salen al vuelo de la
 * cantidad y del alimento (docs/decisiones/017).
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ComidaEntityMapperImpl.class, ComidaLineaEntityMapperImpl.class,
        AlimentoEntityMapperImpl.class, ComidaFormDtoMapperImpl.class, ComidaLineaFormDtoMapperImpl.class})
class ComidaMappersTest {

    private static final LocalDate HOY = LocalDate.of(2026, 9, 30);

    @Autowired
    private ComidaEntityMapper entityMapper;

    @Autowired
    private ComidaLineaEntityMapper lineaEntityMapper;

    @Autowired
    private ComidaFormDtoMapper formMapper;

    private static Alimento arroz() {
        return Alimento.builder().id(2L).nombre("Arroz").marca("Hacendado").kcal100g(new BigDecimal("130.00"))
                .proteinas100g(new BigDecimal("2.70")).carbohidratos100g(new BigDecimal("28.00"))
                .grasas100g(new BigDecimal("0.30")).fuenteExterna(FuenteAlimento.MANUAL).build();
    }

    private static Alimento pollo() {
        return Alimento.builder().id(3L).nombre("Pollo").kcal100g(new BigDecimal("165.00"))
                .proteinas100g(new BigDecimal("31.00")).carbohidratos100g(new BigDecimal("0.00"))
                .grasas100g(new BigDecimal("3.60")).fuenteExterna(FuenteAlimento.MANUAL).build();
    }

    private static ComidaLinea linea(Long id, Alimento alimento, String cantidadG) {
        return ComidaLinea.builder().id(id).usuarioId(1L).alimentoId(alimento.getId()).alimento(alimento)
                .cantidadG(new BigDecimal(cantidadG)).build();
    }

    private static Comida comida() {
        return Comida.builder().id(8L).usuarioId(1L).fecha(HOY).momento(MomentoComida.COMIDA)
                .lineas(List.of(linea(20L, arroz(), "200.00"), linea(21L, pollo(), "100.00"))).build();
    }

    private static AlimentoEntity arrozEntity() {
        return AlimentoEntity.builder().id(2L).nombre("Arroz").marca("Hacendado").kcal100g(new BigDecimal("130.00"))
                .proteinas100g(new BigDecimal("2.70")).carbohidratos100g(new BigDecimal("28.00"))
                .grasas100g(new BigDecimal("0.30")).fuenteExterna(FuenteAlimento.MANUAL).build();
    }

    @Test
    @DisplayName("entity -> dominio de la comida trae las lineas con su alimento y alimentoId, y usuarioId por linea")
    void entityADominio() {
        ComidaLineaEntity lineaEntity = ComidaLineaEntity.builder().id(20L).usuarioId(1L).alimento(arrozEntity())
                .cantidadG(new BigDecimal("200.00")).build();
        ComidaEntity entity = ComidaEntity.builder().id(8L).usuarioId(1L).fecha(HOY).momento(MomentoComida.COMIDA)
                .lineas(List.of(lineaEntity)).build();

        Comida dominio = entityMapper.toDomain(entity);

        assertThat(dominio.getUsuarioId()).isEqualTo(1L);
        assertThat(dominio.getMomento()).isEqualTo(MomentoComida.COMIDA);
        assertThat(dominio.getLineas()).hasSize(1);
        ComidaLinea l = dominio.getLineas().get(0);
        assertThat(l.getUsuarioId()).isEqualTo(1L);
        assertThat(l.getAlimentoId()).isEqualTo(2L);
        assertThat(l.getAlimento()).usingRecursiveComparison().isEqualTo(arroz());
        assertThat(l.getCantidadG()).isEqualTo(new BigDecimal("200.00"));
        assertThat(entityMapper.toDomainList(List.of(entity))).hasSize(1);
    }

    @Test
    @DisplayName("dominio -> entity de la linea apunta al alimento solo por su id y deja la comida sin rellenar")
    void lineaDominioAEntity() {
        ComidaLineaEntity entity = lineaEntityMapper.toEntity(linea(20L, arroz(), "200.00"));

        assertThat(entity.getAlimento()).isNotNull();
        assertThat(entity.getAlimento().getId()).isEqualTo(2L);
        assertThat(entity.getComida()).isNull();
        assertThat(entity.getUsuarioId()).isEqualTo(1L);
        assertThat(entity.getCantidadG()).isEqualTo(new BigDecimal("200.00"));
        assertThat(lineaEntityMapper.toEntityList(List.of(linea(20L, arroz(), "1.00")))).hasSize(1);
    }

    @Test
    @DisplayName("request -> dominio no rellena id ni usuarioId (ni en las lineas) y lleva alimentoId y cantidad")
    void requestNoTrae() {
        ComidaRequestDtoMapper mapper = Mappers.getMapper(ComidaRequestDtoMapper.class);

        Comida dominio = mapper.toDomain(new ComidaRequestDto(HOY, MomentoComida.CENA, List.of(
                new ComidaLineaRequestDto(2L, new BigDecimal("150.00")),
                new ComidaLineaRequestDto(3L, new BigDecimal("80.50")))));

        assertThat(dominio.getId()).isNull();
        assertThat(dominio.getUsuarioId()).isNull();
        assertThat(dominio.getFecha()).isEqualTo(HOY);
        assertThat(dominio.getMomento()).isEqualTo(MomentoComida.CENA);
        assertThat(dominio.getLineas()).hasSize(2);
        assertThat(dominio.getLineas()).allSatisfy(l -> {
            assertThat(l.getId()).isNull();
            assertThat(l.getUsuarioId()).isNull();
            assertThat(l.getAlimento()).isNull();
        });
        assertThat(dominio.getLineas()).extracting(ComidaLinea::getAlimentoId).containsExactly(2L, 3L);
        assertThat(dominio.getLineas()).extracting(ComidaLinea::getCantidadG)
                .containsExactly(new BigDecimal("150.00"), new BigDecimal("80.50"));
    }

    @Test
    @DisplayName("dominio -> form DTO calcula macros por linea y totales de la comida al vuelo")
    void formDtoConMacros() {
        ComidaFormDto dto = formMapper.toFormDto(comida());

        assertThat(dto.id()).isEqualTo(8L);
        assertThat(dto.fecha()).isEqualTo(HOY);
        assertThat(dto.momento()).isEqualTo(MomentoComida.COMIDA);
        assertThat(dto.lineas()).hasSize(2);
        ComidaLineaFormDto arrozLinea = dto.lineas().get(0);
        assertThat(arrozLinea.alimentoId()).isEqualTo(2L);
        assertThat(arrozLinea.alimentoNombre()).isEqualTo("Arroz");
        assertThat(arrozLinea.alimentoMarca()).isEqualTo("Hacendado");
        assertThat(arrozLinea.cantidadG()).isEqualByComparingTo("200.00");
        assertThat(arrozLinea.kcal()).isEqualByComparingTo("260.00");
        assertThat(arrozLinea.proteinas()).isEqualByComparingTo("5.40");
        assertThat(arrozLinea.carbohidratos()).isEqualByComparingTo("56.00");
        assertThat(arrozLinea.grasas()).isEqualByComparingTo("0.60");
        assertThat(dto.kcalTotal()).isEqualByComparingTo("425.00");
        assertThat(dto.proteinasTotal()).isEqualByComparingTo("36.40");
        assertThat(dto.carbohidratosTotal()).isEqualByComparingTo("56.00");
        assertThat(dto.grasasTotal()).isEqualByComparingTo("4.20");
    }

    @Test
    @DisplayName("dominio -> list DTO lleva los totales y no las lineas, y funciona sobre listas")
    void listDto() {
        ComidaListDtoMapper mapper = Mappers.getMapper(ComidaListDtoMapper.class);

        ComidaListDto dto = mapper.toListDto(comida());

        assertThat(dto.id()).isEqualTo(8L);
        assertThat(dto.momento()).isEqualTo(MomentoComida.COMIDA);
        assertThat(dto.kcalTotal()).isEqualByComparingTo("425.00");
        assertThat(dto.proteinasTotal()).isEqualByComparingTo("36.40");
        assertThat(dto.carbohidratosTotal()).isEqualByComparingTo("56.00");
        assertThat(dto.grasasTotal()).isEqualByComparingTo("4.20");
        assertThat(mapper.toListDtoList(List.of(comida(), comida()))).hasSize(2);
    }

    @Test
    @DisplayName("una comida sin lineas tiene totales a cero")
    void comidaSinLineas() {
        Comida vacia = Comida.builder().id(9L).usuarioId(1L).fecha(HOY).momento(MomentoComida.SNACK).build();

        ComidaListDto dto = Mappers.getMapper(ComidaListDtoMapper.class).toListDto(vacia);

        assertThat(dto.kcalTotal()).isEqualByComparingTo("0");
        assertThat(dto.grasasTotal()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("filtro: query params -> filtro de dominio")
    void filtro() {
        ComidaFilterMapper mapper = Mappers.getMapper(ComidaFilterMapper.class);

        ComidaFilter filtro = mapper.toFilter(new ComidaFilterListDto(HOY, HOY.minusDays(7), HOY, MomentoComida.DESAYUNO));
        ComidaFilter vacio = mapper.toFilter(new ComidaFilterListDto(null, null, null, null));

        assertThat(filtro.getFecha()).isEqualTo(HOY);
        assertThat(filtro.getDesde()).isEqualTo(HOY.minusDays(7));
        assertThat(filtro.getHasta()).isEqualTo(HOY);
        assertThat(filtro.getMomento()).isEqualTo(MomentoComida.DESAYUNO);
        assertThat(vacio).usingRecursiveComparison().isEqualTo(ComidaFilter.builder().build());
    }
}
