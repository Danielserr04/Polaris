package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.ObjetivoNutricional;
import com.polaris.fusion.infrastructure.persistence.ObjetivoNutricionalEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ObjetivoNutricionalEntityMapper {

    ObjetivoNutricional toDomain(ObjetivoNutricionalEntity entity);

    ObjetivoNutricionalEntity toEntity(ObjetivoNutricional domain);

    List<ObjetivoNutricional> toDomainList(List<ObjetivoNutricionalEntity> entities);
}
