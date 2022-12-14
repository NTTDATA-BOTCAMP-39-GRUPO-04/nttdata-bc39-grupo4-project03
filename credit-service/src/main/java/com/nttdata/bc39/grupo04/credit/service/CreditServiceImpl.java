package com.nttdata.bc39.grupo04.credit.service;

import com.nttdata.bc39.grupo04.api.credit.CreditDTO;
import com.nttdata.bc39.grupo04.api.credit.CreditService;
import com.nttdata.bc39.grupo04.api.exceptions.InvaliteInputException;
import com.nttdata.bc39.grupo04.api.exceptions.NotFoundException;
import com.nttdata.bc39.grupo04.credit.persistence.CreditEntity;
import com.nttdata.bc39.grupo04.credit.persistence.CreditRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.nttdata.bc39.grupo04.api.utils.Constants.*;

@Service
public class CreditServiceImpl implements CreditService {
    private CreditRepository repository;
    private CreditMapper mapper;
    private Logger LOG = Logger.getLogger(CreditServiceImpl.class);

    @Autowired
    public CreditServiceImpl(CreditRepository repository, CreditMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<CreditDTO> createCredit(CreditDTO dto) {
        validateCreateCredit(dto);
        CreditEntity entity = mapper.dtoToEntity(dto);
        entity.setCreditNumber(generateCreditNumber());
        if (dto.getProductId().equals(CODE_PRODUCT_CREDITO_PERSONAL)
                || dto.getProductId().equals(CODE_PRODUCT_CREDITO_EMPRESARIAL)) {
        	entity.setAvailableBalance(0);
        }
        else {
        	entity.setAvailableBalance(entity.getCreditAmount());
        }        
        entity.setCreateDate(Calendar.getInstance().getTime());
        return repository.save(entity)
                .onErrorMap(DuplicateKeyException.class,
                        ex -> throwDuplicateCredit(dto.getCreditNumber()))
                .map(mapper::entityToDto);
    }

    @Override
    public Mono<CreditDTO> getByCreditNumber(String creditNumber) {
        // TODO Auto-generated method stub
        if (ObjectUtils.isEmpty(creditNumber)) {
            throw new InvaliteInputException("Error, numero de credito invalido");
        }
        Mono<CreditEntity> entityMono = repository.findByCreditNumber(creditNumber);
        if (Objects.isNull(entityMono.block())) {
            throw new NotFoundException("Error, no existe el crédito con Nro: " + creditNumber);
        }
        return entityMono.map(mapper::entityToDto);
    }

    private RuntimeException throwDuplicateCredit(String creditId) {
        LOG.debug("Error , ya existe una credito con el Nro: " + creditId);
        return new InvaliteInputException("Error , ya existe una credito con el Nro: " + creditId);
    }

    @Override
    public Flux<CreditDTO> getAllCreditByCustomer(String customerId) {
        // TODO Auto-generated method stub
        if (ObjectUtils.isEmpty(customerId)) {
            throw new InvaliteInputException("Error, codigo de cliente invalido");
        }
        return repository.findAll().filter(x -> x.getCustomerId().equals(customerId)).map(mapper::entityToDto);
    }

    @Override
    public Mono<CreditDTO> makePaymentCredit(double amount, String creditNumber) {
        // TODO Auto-generated method stub
        CreditEntity entity = repository.findByCreditNumber(creditNumber).block();
        if (ObjectUtils.isEmpty(entity)) {
            throw new NotFoundException("Error, no existe el credito con Nro: " + creditNumber);
        }
        if (entity.getAvailableBalance() == entity.getCreditAmount()) {
            throw new InvaliteInputException("No tiene pagos pendientes");
        }

        if (amount < MIN_PAYMENT_CREDIT_AMOUNT || amount > entity.getCreditAmount()) {
            throw new InvaliteInputException(
                    String.format(Locale.getDefault(), "Error, los limites de Pago son min: %d sol y max: %f sol",
                            MIN_PAYMENT_CREDIT_AMOUNT, entity.getCreditAmount()));
        }

        double newAvailableBalance = entity.getAvailableBalance() + amount;
        if (newAvailableBalance > entity.getCreditAmount()) {
            double suggestedAmount = entity.getCreditAmount() - entity.getAvailableBalance();
            throw new InvaliteInputException(
                    "El monto que intenta pagar excede el monto que debe pagar. Monto a pagar: " + suggestedAmount);
        }
        entity.setAvailableBalance(newAvailableBalance);
        entity.setModifyDate(Calendar.getInstance().getTime());
        return repository.save(entity).map(mapper::entityToDto);
    }

    @Override
    public Mono<CreditDTO> makePaymentCreditCard(double amount, String creditCardNumber) {
        // TODO Auto-generated method stub
        CreditEntity entity = repository.findByCardNumber(creditCardNumber).block();
        if (ObjectUtils.isEmpty(entity)) {
            throw new NotFoundException("Error, no existe la tarjeta de credito con Nro: " + creditCardNumber);
        }
        if (entity.getAvailableBalance() == entity.getCreditAmount()) {
            throw new InvaliteInputException("No tiene pagos pendientes");
        }

        if (amount < MIN_PAYMENT_CREDIT_CARD_AMOUNT || amount > entity.getCreditAmount()) {
            throw new InvaliteInputException(
                    String.format(Locale.getDefault(), "Error, los limites de Pago son min: %d sol y max: %f sol",
                            MIN_PAYMENT_CREDIT_CARD_AMOUNT, entity.getCreditAmount()));
        }

        double newAvailableBalance = entity.getAvailableBalance() + amount;
        if (newAvailableBalance > entity.getCreditAmount()) {
            double suggestedAmount = entity.getCreditAmount() - entity.getAvailableBalance();
            throw new InvaliteInputException(
                    "El monto que intenta pagar excede el monto que debe pagar. Monto a pagar: " + suggestedAmount);
        }
        entity.setAvailableBalance(newAvailableBalance);
        entity.setModifyDate(Calendar.getInstance().getTime());
        return repository.save(entity).map(mapper::entityToDto);
    }

    @Override
    public Mono<CreditDTO> makeChargeCredit(double amount, String creditCardNumber) {
        // TODO Auto-generated method stub
        CreditEntity entity = repository.findByCardNumber(creditCardNumber).block();
        if (Objects.isNull(entity)) {
            throw new NotFoundException("Error, no existe la tarjeta de credito con Nro: " + creditCardNumber);
        }
        if (amount < MIN_CHARGE_CREDIT_CARD_AMOUNT || amount > entity.getCreditAmount()) {
            throw new InvaliteInputException(
                    String.format(Locale.getDefault(), "Error, los limites de Cargo son min: %d sol y max: %f sol",
                            MIN_CHARGE_CREDIT_CARD_AMOUNT, entity.getCreditAmount()));
        }
        double availableBalance = entity.getAvailableBalance();
        if (amount > availableBalance) {
            throw new InvaliteInputException("Excede Limite disponible");
        }
        availableBalance -= amount;
        entity.setAvailableBalance(availableBalance);
        entity.setModifyDate(new Date());
        return repository.save(entity).map(mapper::entityToDto);
    }

    @Override
    public Mono<Void> deleteCredit(String creditNumber) {
        // TODO Auto-generated method stub
        return repository.deleteByCreditNumber(creditNumber);
    }

    private String generateCreditNumber() {
        Date todayDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return System.currentTimeMillis() + sdf.format(todayDate);
    }

    private void validateCreateCredit(CreditDTO dto) {

        if (!dto.getProductId().equals(CODE_PRODUCT_CREDITO_PERSONAL)
                && !dto.getProductId().equals(CODE_PRODUCT_CREDITO_EMPRESARIAL)
                && !dto.getProductId().equals(CODE_PRODUCT_TARJETA_CREDITO)) {
            throw new InvaliteInputException(
                    "Error, el codigo tipo de crédito debe ser Personal, Empresa o Tarjeta de Crédito");
        }

        if (dto.getProductId().equals(CODE_PRODUCT_CREDITO_PERSONAL)) {
            CreditEntity credit = repository.findAll()
                    .filter(x -> x.getProductId().equals(CODE_PRODUCT_CREDITO_PERSONAL)
                            && x.getCustomerId().equals(dto.getCustomerId()))
                    .blockFirst();
            if (credit != null) {
                throw new InvaliteInputException("Error, una persona solo puede tener un máximo de un credito personal");
            }
        }

        if (dto.getProductId().equals(CODE_PRODUCT_TARJETA_CREDITO)) {
            if (ObjectUtils.isEmpty(dto.getCardNumber())) {
                throw new InvaliteInputException("Error, el producto de tarjeta de crédito debe indicar el numero de tarjeta");
            }
            Mono<CreditEntity> entityMono = repository.findByCardNumber(dto.getCardNumber());
            if (!Objects.isNull(entityMono.block())) {
                throw new InvaliteInputException("Error, Ya existe una tarjeta asignada con Nro: " + dto.getCardNumber());
            }
        } else {
            if (!ObjectUtils.isEmpty(dto.getCardNumber())) {
                throw new InvaliteInputException(
                        "Error, el numero de tarjeta solo debe indicarse para producto de tarjeta de crédito");
            }
        }
    }

    @Override
    public Flux<CreditDTO> getAllCreditCardByCustomer(String customerId) {
        return repository.findAll().filter(
                        x -> x.getProductId().equals(CODE_PRODUCT_TARJETA_CREDITO) && x.getCustomerId().equals(customerId))
                .map(mapper::entityToDto);
    }
    
    @Override
    public Mono<CreditDTO> getCreditCardByNumber(String creditCardNumber) {
        return repository.findByCardNumber(creditCardNumber).map(mapper::entityToDto);
    }

}
