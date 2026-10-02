package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.PlanComida;
import com.polaris.fusion.infrastructure.persistence.PlanComidaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = PlanComidaLineaEntityMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PlanComidaEntityMapper {

    PlanComida toDomain(PlanComidaEntity entity);

    PlanComidaEntity toEntity(PlanComida domain);

    List<PlanComida> toDomainList(List<PlanComidaEntity> entities);
}
