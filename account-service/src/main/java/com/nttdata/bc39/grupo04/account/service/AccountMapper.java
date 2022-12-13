package com.nttdata.bc39.grupo04.account.service;


import com.nttdata.bc39.grupo04.account.persistence.AccountEntity;
import com.nttdata.bc39.grupo04.api.account.AccountDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mappings({
            @Mapping(target = "maxMovements", ignore = true),
            @Mapping(target = "maintenanceFee", ignore = true),
            @Mapping(target = "hasMaintenanceFee", ignore = true),
            @Mapping(target = "minAmountDailyAverage", ignore = true),
            @Mapping(target = "hasMinAmountDailyAverage", ignore = true)
    })
    AccountDTO entityToDto(AccountEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true)
    })
    AccountEntity dtoToEntity(AccountDTO dto);
}
