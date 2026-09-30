package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.infrastructure.persistence.AlimentoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AlimentoEntityMapper {

    Alimento toDomain(AlimentoEntity entity);

    AlimentoEntity toEntity(Alimento domain);

    List<Alimento> toDomainList(List<AlimentoEntity> entities);
}
