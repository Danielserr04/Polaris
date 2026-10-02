package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.MedidaCorporal;
import com.polaris.nucleo.infrastructure.persistence.dto.out.MedidaCorporalListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MedidaCorporalListDtoMapper {

    MedidaCorporalListDto toListDto(MedidaCorporal registro);

    List<MedidaCorporalListDto> toListDtoList(List<MedidaCorporal> registros);
}
