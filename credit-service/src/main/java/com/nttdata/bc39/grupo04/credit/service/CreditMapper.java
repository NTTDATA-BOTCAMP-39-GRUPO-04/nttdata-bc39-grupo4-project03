package com.nttdata.bc39.grupo04.credit.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.nttdata.bc39.grupo04.credit.dto.CreditDTO;
import com.nttdata.bc39.grupo04.credit.persistence.CreditEntity;

@Mapper(componentModel = "spring")
public interface CreditMapper {

    CreditDTO entityToDto(CreditEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true)
    })
    CreditEntity dtoToEntity(CreditDTO dto);
}
