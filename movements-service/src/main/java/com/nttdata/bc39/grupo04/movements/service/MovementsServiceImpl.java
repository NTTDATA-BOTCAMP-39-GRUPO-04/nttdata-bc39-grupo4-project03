package com.nttdata.bc39.grupo04.movements.service;

import com.nttdata.bc39.grupo04.api.exceptions.InvaliteInputException;
import com.nttdata.bc39.grupo04.api.movements.MovementsDTO;
import com.nttdata.bc39.grupo04.api.movements.MovementsReportDTO;
import com.nttdata.bc39.grupo04.api.movements.MovementsService;
import com.nttdata.bc39.grupo04.api.utils.CodesEnum;
import com.nttdata.bc39.grupo04.movements.persistence.MovementsEntity;
import com.nttdata.bc39.grupo04.movements.persistence.MovementsRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Calendar;
import java.util.Objects;

@Service
public class MovementsServiceImpl implements MovementsService {
    private final MovementsRepository repository;
    private final MovementMapper mapper;
    private final Logger logger = Logger.getLogger(MovementsServiceImpl.class);

    @Autowired
    public MovementsServiceImpl(MovementsRepository repository, MovementMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<MovementsDTO> saveDepositMovement(MovementsDTO dto) {
        validationMovement(dto, CodesEnum.TYPE_DEPOSIT);
        MovementsEntity entity = mapper.dtoToEntity(dto);
        entity.setTotalAmount(dto.getAmount() + dto.getComission());
        entity.setDate(Calendar.getInstance().getTime());
        return repository.save(entity)
                .map(mapper::entityToDto);
    }

    @Override
    public Mono<MovementsDTO> saveWithdrawlMovement(MovementsDTO dto) {
        validationMovement(dto, CodesEnum.TYPE_WITHDRAWL);
        dto.setAmount(dto.getAmount() * -1);
        MovementsEntity entity = mapper.dtoToEntity(dto);
        entity.setTotalAmount(dto.getAmount() + dto.getComission());
        entity.setDate(Calendar.getInstance().getTime());
        return repository.save(entity)
                .map(mapper::entityToDto);
    }

    @Override
    public Flux<MovementsReportDTO> getAllMovements() {
        return repository.findAll().map(mapper::entityToReportDto);
    }

    @Override
    public Flux<MovementsReportDTO> getAllMovementsByNumberAccount(String accountNumber) {
        return repository.findByAccount(accountNumber).map(mapper::entityToReportDto);
    }

    private void validationMovement(MovementsDTO dto, CodesEnum codesEnum) {
        if (Objects.isNull(dto)) {
            throw new InvaliteInputException("Error, el formato de los datos es invalido");
        }
        if (Objects.isNull(dto.getAccount())) {
            throw new InvaliteInputException("Error,el numero de cuenta es invalido");
        }
        if (Objects.isNull(dto.getTransferAccount())) {
            throw new InvaliteInputException("Error , cuenta transferencia es invalida.");
        }
    }

}
