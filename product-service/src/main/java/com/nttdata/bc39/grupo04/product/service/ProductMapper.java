package com.nttdata.bc39.grupo04.product.service;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.nttdata.bc39.grupo04.api.product.ProductDTO;
import com.nttdata.bc39.grupo04.product.persistence.ProductEntity;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductDTO entityToDto(ProductEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true)
    })
    ProductEntity dtoToEntity(ProductDTO dto);
}
