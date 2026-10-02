package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.MedidaCorporal;
import com.polaris.nucleo.infrastructure.persistence.MedidaCorporalEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MedidaCorporalEntityMapper {

    MedidaCorporal toDomain(MedidaCorporalEntity entity);

    MedidaCorporalEntity toEntity(MedidaCorporal domain);

    List<MedidaCorporal> toDomainList(List<MedidaCorporalEntity> entities);
}
