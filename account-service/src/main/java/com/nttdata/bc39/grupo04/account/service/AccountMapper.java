package com.nttdata.bc39.grupo04.account.service;


import com.nttdata.bc39.grupo04.account.persistence.AccountEntity;
import com.nttdata.bc39.grupo04.api.account.AccountDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountDTO entityToDto(AccountEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true)
    })
    AccountEntity dtoToEntity(AccountDTO dto);
}
