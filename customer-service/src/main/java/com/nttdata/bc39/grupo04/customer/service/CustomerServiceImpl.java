package com.nttdata.bc39.grupo04.customer.service;

import com.nttdata.bc39.grupo04.api.customer.CustomerService;
import com.nttdata.bc39.grupo04.api.exceptions.InvaliteInputException;
import com.nttdata.bc39.grupo04.api.exceptions.NotFoundException;
import com.nttdata.bc39.grupo04.api.customer.CustomerDto;
import com.nttdata.bc39.grupo04.customer.persistence.CustomerEntity;
import com.nttdata.bc39.grupo04.customer.persistence.CustomerRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Calendar;
import java.util.Objects;

import static com.nttdata.bc39.grupo04.api.utils.Constants.*;

@Service
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository repository;
    private final CustomerMapper mapper;
    private final Logger logger = Logger.getLogger(CustomerServiceImpl.class);

    @Autowired
    public CustomerServiceImpl(CustomerRepository repository, CustomerMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Flux<CustomerDto> getAllCustomers() {
        return repository.findAll().map(mapper::entityToDto);
    }

    @Override
    public Mono<CustomerDto> getCustomerById(String customerId) {
        if (Objects.isNull(customerId)) {
            throw new InvaliteInputException("Invalid customer with code: " + customerId);
        }
        Mono<CustomerEntity> entityMono = repository.findByCode(customerId);
        if (Objects.isNull(entityMono.block())) {
            logger.debug("El cliente nro: " + customerId + ",no existe");
            throw new NotFoundException("Error, el cliente nro: " + customerId + ",no existe");
        }
        return entityMono.map(mapper::entityToDto);
    }

    @Override
    public Mono<Void> deleteCustomerById(String customerId) {
        Mono<CustomerEntity> entityMono = repository.findByCode(customerId);
        if (Objects.isNull(entityMono.block())) {
            logger.debug("Error, el cliente nro: " + customerId + ",no existe");
            throw new NotFoundException("Error, el cliente nro: " + customerId + ",no existe");
        }
        logger.debug("Cliente nro:" + customerId + " ,eliminado");
        return repository.deleteByCode(customerId);
    }

    @Override
    public Mono<CustomerDto> createCustomer(CustomerDto customerDto) {
        validationCreateCustomer(customerDto);
        CustomerEntity entity = mapper.dtoToEntity(customerDto);
        entity.setDate(Calendar.getInstance().getTime());
        return repository.save(entity)
                .onErrorMap(DuplicateKeyException.class,
                        ex -> throwDuplicateCustomer(customerDto.getCode()))
                .map(mapper::entityToDto)
                .log("Cliente  nro: " + customerDto.getCode() + ", creado correctamente, data= " + customerDto);
    }

    private void validationCreateCustomer(CustomerDto dto) {
        if (Objects.isNull(dto)) {
            throw new InvaliteInputException("Error, el formato de la peticion es invalido");
        }
        if (Objects.isNull(dto.getCode())) {
            throw new InvaliteInputException("Error, el codigo de cliente (code) es invalido");
        }
        if (Objects.isNull(dto.getType())) {
            throw new InvaliteInputException("Error, el tipo de cliente (type) es invalido");
        }
        if (!dto.getType().equals(CODE_ACCOUNT_EMPRESARIAL) &&
                !dto.getType().equals(CODE_ACCOUNT_PERSONAL)) {
            throw new InvaliteInputException("Error, el tipo de cuenta invalido (accountType), verifique los datos admitidos: " + CODE_ACCOUNT_PERSONAL + " o " + CODE_ACCOUNT_EMPRESARIAL);
        }
        if (dto.getType().equals(CODE_ACCOUNT_PERSONAL) && dto.getCode().length() != LENGHT_CODE_PERSONAL_CUSTOMER) {
            throw new InvaliteInputException("Error, la longitud del codigo del cliente (code) es invalido, debe de ingresar el DNI de la persona.");
        }
        if (dto.getType().equals(CODE_ACCOUNT_EMPRESARIAL) && dto.getCode().length() != LENGHT_CODE_EMPRESARIAL_CUSTOMER) {
            throw new InvaliteInputException("Error, la longitud del codigo del cliente (code) es invalido, debe de ingresar el RUC de la empresa.");
        }
    }

    private RuntimeException throwDuplicateCustomer(String customerId) {
        logger.debug("Error, el cliente nro: " + customerId + ", ya esta registrado");
        return new InvaliteInputException("Error,ya existe un cliente con codigo: " + customerId);
    }

    @Override
    public Mono<CustomerDto> updateCustomerById(String customerId, CustomerDto customerDto) {
        CustomerEntity entity = repository.findByCode(customerId).block();
        if (Objects.isNull(entity)) {
            logger.debug("El cliente nro: " + customerId + ",no existe");
            throw new NotFoundException("Error, el cliente con codigo : " + customerId + " no existe");
        }
        entity.setName(customerDto.getName());
        logger.debug("cliente nro: " + customerId + ", actualizado correctamente, data=" + customerDto);
        return repository.save(entity).map(mapper::entityToDto);
    }
}
