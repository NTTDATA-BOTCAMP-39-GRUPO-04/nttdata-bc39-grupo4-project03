package com.nttdata.bc39.grupo04.customer.service;

import com.nttdata.bc39.grupo04.api.customer.CustomerDto;
import com.nttdata.bc39.grupo04.customer.persistence.CustomerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerDto entityToDto(CustomerEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
    })
    CustomerEntity dtoToEntity(CustomerDto dto);
}
