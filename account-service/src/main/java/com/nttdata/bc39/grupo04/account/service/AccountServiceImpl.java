package com.nttdata.bc39.grupo04.account.service;

import com.nttdata.bc39.grupo04.account.persistence.AccountEntity;
import com.nttdata.bc39.grupo04.account.persistence.AccountRepository;
import com.nttdata.bc39.grupo04.api.account.*;
import com.nttdata.bc39.grupo04.api.credit.CreditCustomerDTO;
import com.nttdata.bc39.grupo04.api.exceptions.BadRequestException;
import com.nttdata.bc39.grupo04.api.exceptions.InvaliteInputException;
import com.nttdata.bc39.grupo04.api.exceptions.NotFoundException;
import com.nttdata.bc39.grupo04.api.feign.CreditRestCustomer;
import com.nttdata.bc39.grupo04.api.utils.Constants;
import io.netty.util.internal.StringUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;

import static com.nttdata.bc39.grupo04.api.utils.Constants.*;

@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository repository;
    private final AccountMapper mapper;
    private final Logger logger = Logger.getLogger(AccountServiceImpl.class);

    @Autowired
    public AccountServiceImpl(AccountRepository repository, AccountMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Autowired
    private CreditRestCustomer creditRestCustomer;

    @Override
    @SuppressWarnings("all")
    public Mono<DebitCardDTO> createDebitCard(DebitCardDTO debitCardDTO) {
        validateCreateDebitCard(debitCardDTO);
        debitCardDTO.getAssociedAccounts().forEach(numberAccount -> {
            AccountEntity entity = repository.findByAccount(numberAccount).block();
            if (Objects.isNull(entity.getDebitCardNumber())) {
                entity.setDebitCardNumber(debitCardDTO.getDebitCardNumber());
                entity.setDebitCardCreationDate(Calendar.getInstance().getTime());
                repository.save(entity).block();
            }
        });
        debitCardDTO.setDebitCardCreationDate(Calendar.getInstance().getTime());
        return Mono.just(debitCardDTO);
    }

    @Override
    public Flux<AccountDTO> getAllAccountByDebitCardNumber(String debitCardNumber) {
        return repository.findByDebitCardNumber(debitCardNumber).map(mapper::entityToDto);
    }

    @Override
    public Mono<DebitCardNumberDTO> generateNumberDebitCard() {
        Supplier<String> generateNumberDebitCard = () -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyddMM");
            return sdf.format(new Date()) + System.currentTimeMillis();
        };
        DebitCardNumberDTO numberDebitCardDTO = new DebitCardNumberDTO(generateNumberDebitCard.get(),
                Calendar.getInstance().getTime());
        return Mono.just(numberDebitCardDTO);
    }

    private void validateCreateDebitCard(DebitCardDTO debitCardDTO) {
        if (Objects.isNull(debitCardDTO)) {
            throw new InvaliteInputException("Error, el cuerpo de la solicitud es invalido");
        }
        if (Objects.isNull(debitCardDTO.getCustomerId())) {
            throw new InvaliteInputException("Error, codigo de cliente invalido");
        }
        if (Objects.isNull(debitCardDTO.getAssociedAccounts())) {
            throw new InvaliteInputException("Error, la lista de cuentas asociadas es invalido");
        }
        if (debitCardDTO.getAssociedAccounts().isEmpty()) {
            throw new InvaliteInputException("Error, deje de asociar por lo menos 1 cuenta bancaria hacia la tarjeta de debito.");
        }
        debitCardDTO.getAssociedAccounts().forEach(numberAccount -> {
            AccountEntity entity = repository.findByAccount(numberAccount).block();
            if (Objects.isNull(entity)) {
                throw new NotFoundException("Error, el nro de cuenta :" + numberAccount + " no existe.");
            }
            if (!entity.getCustomerId().equals(debitCardDTO.getCustomerId())) {
                throw new InvaliteInputException("Error, el nro de cuenta :" + numberAccount
                        + " no corresponde al cliente nro:" + debitCardDTO.getCustomerId() + " enviado");
            }
        });
    }

    @Override
    public Mono<AccountDTO> getByAccountNumber(String accountNumber) {
        if (Objects.isNull(accountNumber)) {
            throw new InvaliteInputException("Error, numero de cuenta invalido");
        }
        Mono<AccountEntity> entityMono = repository.findByAccount(accountNumber);
        if (Objects.isNull(entityMono.block())) {
            logger.debug("Error, no existe la cuenta bancaria con Nro:" + accountNumber);
            throw new NotFoundException("Error, no existe la cuenta bancaria con Nro: " + accountNumber);
        }
        logger.debug("Retornando detalle de la cuenta bancaria con Nro:" + accountNumber);
        return entityMono.map(mapper::entityToDto);
    }

    @Override
    public Flux<AccountDTO> getAllAccountByCustomer(String customerId) {
        if (Objects.isNull(customerId)) {
            throw new InvaliteInputException("Error, codigo de cliente invalido");
        }
        logger.debug("Retornando las cuentas bancarias del cliente con Nro:" + customerId);
        return repository.findAll().filter(x -> x.getCustomerId().equals(customerId)).map(mapper::entityToDto);
    }

    @Override
    public Mono<AccountDTO> createAccount(AccountDTO dto) {
        validateCreateAccount(dto);
        AccountEntity entity = mapper.dtoToEntity(dto);
        if (!Objects.isNull(dto.getAccount()) && dto.getAccount().equals(ACCOUNT_NUMBER_OF_ATM)) {
            entity.setAccount(ACCOUNT_NUMBER_OF_ATM);
        } else {
            Supplier<String> generateAccountNumberSupplier = () -> {
                Date todayDate = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                return "1"+sdf.format(todayDate) + System.currentTimeMillis();
            };
            entity.setAccount(generateAccountNumberSupplier.get());
        }
        entity.setCreateDate(Calendar.getInstance().getTime());
        return repository.save(entity).onErrorMap(DuplicateKeyException.class, ex -> throwDuplicateAccount(dto.getAccount())).map(mapper::entityToDto);
    }

    RuntimeException throwDuplicateAccount(String accountNumber) {
        logger.debug("Error , ya existe una cuenta con el Nro: " + accountNumber);
        return new InvaliteInputException("Error , ya existe una cuenta con el Nro: " + accountNumber);
    }

    @Override
    public Mono<AccountDTO> makeDepositAccount(double amount, String accountNumber) {
        AccountEntity entity = repository.findByAccount(accountNumber).block();
        if (Objects.isNull(entity)) {
            logger.debug("Error, no existe la cuenta con Nro: " + accountNumber);
            throw new NotFoundException("Error, no existe la cuenta bancaria con Nro: " + accountNumber);
        }
        if (amount < MIN_DEPOSIT_AMOUNT || amount > MAX_DEPOSIT_AMOUNT) {
            logger.debug("Error limites de deposito , Nro cuenta: " + accountNumber + " con monto: " + amount);
            throw new NotFoundException(String.format(Locale.getDefault(), "Error, los limites de DEPOSITO son min: %d sol y max: %d sol", MIN_DEPOSIT_AMOUNT, MAX_DEPOSIT_AMOUNT));
        }
        double newAvailableBalance = entity.getAvailableBalance() + amount;
        entity.setAvailableBalance(newAvailableBalance);
        entity.setModifyDate(Calendar.getInstance().getTime());
        return repository.save(entity).map(mapper::entityToDto);
    }

    @Override
    public Mono<AccountDTO> makeWithdrawalAccount(double amount, String accountNumber) {
        AccountEntity entity = repository.findByAccount(accountNumber).block();
        if (Objects.isNull(entity)) {
            logger.debug("Error, no existe la cuenta con Nro: " + accountNumber);
            throw new NotFoundException("Error, no existe la cuenta con Nro: " + accountNumber);
        }
        if (amount < MIN_WITHDRAWAL_AMOUNT || amount > MAX_WITHDRAWAL_AMOUNT) {
            logger.debug("El retirno, no cumple con los limites establecidos , Nro cuenta: " + accountNumber);
            throw new NotFoundException(String.format(Locale.getDefault(), "Error, los limites de RETIRO son min: %d sol y max: %d sol", MIN_WITHDRAWAL_AMOUNT, MAX_WITHDRAWAL_AMOUNT));
        }
        double availableBalance = entity.getAvailableBalance();
        if (amount > availableBalance) {
            logger.debug("Saldo insuficiente, cuenta con Nro:" + accountNumber);
            throw new BadRequestException("Error,saldo insuficiente en cuenta Nro: " + accountNumber);
        }
        availableBalance -= amount;
        entity.setAvailableBalance(availableBalance);
        entity.setModifyDate(new Date());
        return repository.save(entity).map(mapper::entityToDto);
    }

    @Override
    public Mono<Void> deleteAccount(String accountNumber) {
        return repository.deleteByAccount(accountNumber);
    }

    private void validateCreateAccount(AccountDTO dto) {
        if (Objects.isNull(dto)) {
            logger.debug("Error , objecto enviado invalido para la creacion de cuenta");
            throw new InvaliteInputException("Error , objecto enviado invalido para la creacion de cuenta");
        }
        if (Objects.isNull(dto.getProductId())) {
            throw new InvaliteInputException("Error, codigo de producto invalido");
        }
        if (Objects.isNull(dto.getCustomerId())) {
            throw new InvaliteInputException("Error, codigo de cliente invalido");
        }
        if (dto.getCustomerId().length() != LENGHT_CODE_PERSONAL_CUSTOMER && dto.getCustomerId().length() != LENGHT_CODE_EMPRESARIAL_CUSTOMER) {
            throw new InvaliteInputException("Error,codigo de cliente invalido, considerar el DNI(" + LENGHT_CODE_PERSONAL_CUSTOMER + ") para cuentas personales" + " y el ruc(" + LENGHT_CODE_EMPRESARIAL_CUSTOMER + ") para cuentas empresariales");
        }

        if (!dto.getProductId().equals(CODE_PRODUCT_CUENTA_AHORRO) && !dto.getProductId().equals(CODE_PRODUCT_CUENTA_CORRIENTE) && !dto.getProductId().equals(CODE_PRODUCT_PLAZO_FIJO) && !dto.getProductId().equals(CODE_PRODUCT_EMPRESA_PYME_CORRIENTE) && !dto.getProductId().equals(CODE_PRODUCT_PERSONAL_VIP_AHORRO)) {
            throw new InvaliteInputException("Error, el tipo de cuenta invalida (productId), verifique los datos admitidos: " + CODE_PRODUCT_CUENTA_AHORRO + " => CUENTA DE AHORRO , " + CODE_PRODUCT_CUENTA_CORRIENTE + " => CUENTA CORRIENTE, " + CODE_PRODUCT_PLAZO_FIJO + " => PLAZO FIJO, " + CODE_PRODUCT_PERSONAL_VIP_AHORRO + " => CUENTA AHORRO VIP, " + CODE_PRODUCT_EMPRESA_PYME_CORRIENTE + " => CUENTA CORRIENTE PYME");
        }

        logger.debug("Data enviada para la creacion de cuenta, object= " + dto);

        if (dto.getCustomerId().length() == LENGHT_CODE_EMPRESARIAL_CUSTOMER) {
            if (Objects.isNull(dto.getHolders())) {
                throw new InvaliteInputException("Error, titular o titulares de la cuenta empresarial, invalido");
            }
            if (Objects.isNull(dto.getHolders().get(0))) {
                throw new InvaliteInputException("Error, es necesario enviar el titular o titulares de la cuenta empresarial");
            }

            for (HolderDTO holder : dto.getHolders()) {
                if (StringUtil.isNullOrEmpty(holder.getCode()) || StringUtil.isNullOrEmpty(holder.getName())) {
                    throw new InvaliteInputException("Error, algunos de los tituales tienen datos invalidos o en blanco");
                }
            }
            if (dto.getProductId().equals(Constants.CODE_PRODUCT_CUENTA_AHORRO)) {
                logger.debug("Error, un cliente empresarial no puede tener cuentas de ahorro, cliente: " + dto.getCustomerId());
                throw new InvaliteInputException("Error, un cliente empresarial no puede tener cuentas de ahorro");
            }
            if (dto.getProductId().equals(CODE_PRODUCT_PLAZO_FIJO)) {
                logger.debug("Error, un cliente empresarial no puede tener cuentas de plazo fijo" + dto.getCustomerId());
                throw new InvaliteInputException("Error, un cliente empresarial no puede tener cuentas de plazo fijo");
            }

            if (dto.getProductId().equals(CODE_PRODUCT_EMPRESA_PYME_CORRIENTE)) {
                if (dto.isHasMaintenanceFee() || dto.getMaintenanceFee() != MIN_AMOUNT_MANTENANCE_FEET) {
                    throw new InvaliteInputException("Error, una cuenta PYME no debe tener comisión de mantenimiento");
                }
            }
        }
        if (dto.getCustomerId().length() == LENGHT_CODE_PERSONAL_CUSTOMER) {
            if (!Objects.isNull(dto.getHolders())) {
                throw new InvaliteInputException("Error, las cuentas personales tiene como titular al cliente, " + "no hay necesitad de enviarlo.");
            }
            if (!Objects.isNull(dto.getSignatories())) {
                throw new InvaliteInputException("Error, las cuentas personales no requieren firmante autorizados");
            }
            if (dto.getProductId().equals(CODE_PRODUCT_CUENTA_AHORRO)) {
                AccountEntity account = repository.findAll().filter(x -> x.getProductId().equals(CODE_PRODUCT_CUENTA_AHORRO) && x.getCustomerId().equals(dto.getCustomerId())).blockFirst();
                if (account != null) {
                    logger.debug("Error, un cliente personal solo puede tener un máximo de una cuenta de ahorro, cliente:" + dto.getCustomerId());
                    throw new InvaliteInputException("Error, un cliente personal solo puede tener un máximo de una cuenta de ahorro");
                }
            }
            if (dto.getProductId().equals(CODE_PRODUCT_CUENTA_CORRIENTE)) {
                AccountEntity account = repository.findAll().filter(x -> x.getProductId().equals(CODE_PRODUCT_CUENTA_CORRIENTE) && x.getCustomerId().equals(dto.getCustomerId())).blockFirst();
                if (account != null) {
                    logger.debug("Error, un cliente personal solo puede tener un máximo de una cuenta corriente, cliente:" + dto.getCustomerId());
                    throw new InvaliteInputException("Error, un cliente personal solo puede tener un máximo de una cuenta corriente");
                }
            }

            if (dto.getProductId().equals(CODE_PRODUCT_PERSONAL_VIP_AHORRO)) {
                if (!dto.isHasMinAmountDailyAverage() || ObjectUtils.isEmpty(dto.getMinAmountDailyAverage())) {
                    throw new InvaliteInputException("Error, una cuenta VIP requiere un monto mínimo de promedio diario");
                }
            }
        }
    }
}
