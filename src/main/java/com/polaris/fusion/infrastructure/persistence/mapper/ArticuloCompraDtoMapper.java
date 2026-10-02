package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.ArticuloCompra;
import com.polaris.fusion.infrastructure.persistence.dto.out.ArticuloCompraDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ArticuloCompraDtoMapper {

    ArticuloCompraDto toDto(ArticuloCompra articulo);

    List<ArticuloCompraDto> toDtoList(List<ArticuloCompra> articulos);
}
