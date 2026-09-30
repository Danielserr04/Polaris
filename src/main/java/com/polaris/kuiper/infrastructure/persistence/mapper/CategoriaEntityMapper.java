package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.infrastructure.persistence.CategoriaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CategoriaEntityMapper {

    Categoria toDomain(CategoriaEntity entity);

    CategoriaEntity toEntity(Categoria domain);

    List<Categoria> toDomainList(List<CategoriaEntity> entities);
}
