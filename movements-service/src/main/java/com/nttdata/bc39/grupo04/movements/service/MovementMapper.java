package com.nttdata.bc39.grupo04.movements.service;

import com.nttdata.bc39.grupo04.api.movements.MovementsDTO;
import com.nttdata.bc39.grupo04.api.movements.MovementsReportDTO;
import com.nttdata.bc39.grupo04.movements.persistence.MovementsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface MovementMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "totalAmount", ignore = true)
    })
    MovementsEntity dtoToEntity(MovementsDTO dto);

    MovementsDTO entityToDto(MovementsEntity entity);

    //  @Mappings({
    //        @Mapping(source = "entity.totalAmount", target = "amount"),
    //})
    MovementsReportDTO entityToReportDto(MovementsEntity entity);

}
