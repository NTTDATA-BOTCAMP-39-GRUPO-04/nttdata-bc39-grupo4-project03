package com.nttdata.bc39.grupo04.composite.service;

import com.nttdata.bc39.grupo04.api.account.AccountDTO;
import com.nttdata.bc39.grupo04.api.composite.*;
import com.nttdata.bc39.grupo04.api.customer.CustomerDto;
import com.nttdata.bc39.grupo04.api.exceptions.InvaliteInputException;
import com.nttdata.bc39.grupo04.api.exceptions.NotFoundException;
import com.nttdata.bc39.grupo04.api.movements.MovementsDTO;
import com.nttdata.bc39.grupo04.api.movements.MovementsReportDTO;
import com.nttdata.bc39.grupo04.api.product.ProductDTO;
import com.nttdata.bc39.grupo04.api.utils.CodesEnum;
import com.nttdata.bc39.grupo04.api.utils.Constants;
import com.nttdata.bc39.grupo04.api.utils.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

import static com.nttdata.bc39.grupo04.api.utils.Constants.*;

@Service
public class CompositeServiceImpl implements CompositeService {

    private final CompositeIntegration integration;
    private final Logger logger = Logger.getLogger(CompositeServiceImpl.class);

    @Autowired
    public CompositeServiceImpl(CompositeIntegration integration) {
        this.integration = integration;
    }

    @Override
    public Mono<TransactionAtmDTO> makeDepositATM(String destinationAccountNumber, double amount) {
        return takeTransference(ACCOUNT_NUMBER_OF_ATM,
                destinationAccountNumber, amount, CodesEnum.TYPE_DEPOSIT);
    }

    @Override
    public Mono<TransactionAtmDTO> makeWithdrawnATM(String destinationAccountNumber, double amount) {
        return takeTransference(ACCOUNT_NUMBER_OF_ATM,
                destinationAccountNumber, amount, CodesEnum.TYPE_WITHDRAWL);
    }

    @Override
    public Mono<TransactionAtmDTO> makeTransferAccount(TransactionTransferDTO body) {
        return takeTransference(body.getSourceAccount()
                , body.getDestinationAccount()
                , body.getAmount()
                , CodesEnum.TYPE_TRANSFER);
    }

    @Override
    public Flux<AvailableAmountDailyDTO> getAvailableAmountDaily(String customerId) {
        integration.getCustomerById(customerId);
        List<AccountDTO> accountAllOfCustomer = getAccountAllByCustomer(customerId).collectList().block();
        if (Objects.isNull(accountAllOfCustomer) || accountAllOfCustomer.isEmpty()) {
            return Flux.just();
        }
        List<MovementsReportDTO> movementsAllProductByCustomerList = new ArrayList<>();
        accountAllOfCustomer.forEach(account -> {
            List<MovementsReportDTO> movementByAccount = getAllMovementsByAccount(account.getAccount())
                    .collectList().block();
            if (!Objects.isNull(movementByAccount)) {
                movementByAccount.forEach(item -> item.setDate(DateUtils.getDateWithFormatddMMyyyy(item.getDate())));
                movementsAllProductByCustomerList.addAll(movementByAccount);
            }
        });
        Map<Date, Double> avgAvailableBalanceMap = movementsAllProductByCustomerList.stream()
                .collect(Collectors.groupingBy(MovementsReportDTO::getDate,
                        Collectors.averagingDouble(MovementsReportDTO::getAvailableBalance)));
        List<AvailableAmountDailyDTO> reportList = new ArrayList<>();
        avgAvailableBalanceMap.keySet().forEach(key -> reportList.add(new AvailableAmountDailyDTO(key,
                avgAvailableBalanceMap.get(key))));
        return Mono.just(reportList).flatMapMany(Flux::fromIterable);
    }

    @Override
    public Flux<ComissionReportDTO> getAllComissionByProduct(String fechStart, String fechEnd) {
        if (Objects.isNull(fechStart) || Objects.isNull(fechEnd)) {
            throw new InvaliteInputException("Error, las fechas ingresadas son invalidad , formato requerido : dd/mm/yyyy");
        }
        Date startDate = DateUtils.convertStringToDate(fechStart);
        Date endDate = DateUtils.convertStringToDate(fechEnd);
        if (Objects.isNull(startDate) || Objects.isNull(endDate)) {
            throw new InvaliteInputException("Error, las fechas ingresadas son invalidad , formato requerido : dd/mm/yyyy");
        }
        Flux<MovementsReportDTO> movementsAll = integration.getAllMovements()
                .map(movement -> {
                    movement.setDate(DateUtils.getDateWithFormatddMMyyyy(movement.getDate()));
                    return movement;
                }).filter(x -> x.getDate().compareTo(startDate) >= 0 && x.getDate().compareTo(endDate) <= 0);

        Map<String, Double> comissionMap = movementsAll.toStream()
                .peek(item -> item.setComission(Math.abs(item.getComission())))
                .collect(Collectors.groupingBy(MovementsReportDTO::getProductId,
                        Collectors.summingDouble(MovementsReportDTO::getComission)));

        List<ComissionReportDTO> comissionList = new ArrayList<>(List.of());
        comissionMap.keySet().forEach(key -> {
            comissionList.add(new ComissionReportDTO(key
                    , Constants.getNameProduct(key)
                    , comissionMap.get(key)));
        });
        return Mono.just(comissionList).flatMapMany(Flux::fromIterable);
    }

    private Mono<TransactionAtmDTO> takeTransference(
            String sourceAccountNumber, String destinationAccountNumber, double amount, CodesEnum codesEnum) {
        Mono<AccountDTO> sourceMono = integration.getByAccountNumber(sourceAccountNumber);
        Mono<AccountDTO> destinationMono = integration.getByAccountNumber(destinationAccountNumber);
        String productIdSource = Objects.requireNonNull(sourceMono.block()).getProductId();
        String productIdDestination = Objects.requireNonNull(destinationMono.block()).getProductId();
        logger.debug("productIdSource => " + productIdSource);
        logger.debug("productIdDestination => " + productIdDestination);
        validationLimitAmount(sourceAccountNumber, destinationAccountNumber, codesEnum, amount);
        Flux<MovementsReportDTO> movements = integration.getAllMovementsByNumberAccount(codesEnum ==
                CodesEnum.TYPE_TRANSFER ? sourceAccountNumber : destinationAccountNumber);
        double newAmount = amount;
        double newComission = 0;
        if (Objects.requireNonNull(movements.collectList().block()).size() >= MAX_TRANSACCION_FREE) {
            newAmount = amount - COMISSION_AMOUNT_PER_TRANSACTION;
            newComission = COMISSION_AMOUNT_PER_TRANSACTION * -1;
        }

        MovementsDTO sourceMovement = new MovementsDTO();
        sourceMovement.setAccount(sourceAccountNumber);
        sourceMovement.setComission(codesEnum == CodesEnum.TYPE_TRANSFER ? newComission : Math.abs(newComission));
        sourceMovement.setTransferAccount(destinationAccountNumber);
        sourceMovement.setProductId(productIdSource);
        sourceMovement.setAmount(newAmount);

        MovementsDTO destinationMovement = new MovementsDTO();
        destinationMovement.setAccount(destinationAccountNumber);
        destinationMovement.setTransferAccount(sourceAccountNumber);
        destinationMovement.setProductId(productIdDestination);
        destinationMovement.setAmount(newAmount);
        destinationMovement.setComission(codesEnum == CodesEnum.TYPE_WITHDRAWL ? newComission : 0);

        Mono<AccountDTO> sourceAccountMono = Mono.just(new AccountDTO());
        Mono<AccountDTO> destinationAccountMono = Mono.just(new AccountDTO());
        switch (codesEnum) {
            case TYPE_DEPOSIT:
                sourceAccountMono = integration.makeDepositAccount(amount, sourceAccountNumber);
                destinationAccountMono = integration.makeDepositAccount(newAmount, destinationAccountNumber);
                sourceMovement.setAvailableBalance(Objects.requireNonNull(sourceAccountMono.block()).getAvailableBalance());
                integration.saveDepositMovement(sourceMovement);
                break;
            case TYPE_WITHDRAWL:
                sourceAccountMono = integration.makeWithdrawalAccount(newAmount, sourceAccountNumber);
                destinationAccountMono = integration.makeWithdrawalAccount(amount, destinationAccountNumber);
                sourceMovement.setAvailableBalance(Objects.requireNonNull(sourceAccountMono.block()).getAvailableBalance());
                integration.saveWithdrawlMovement(sourceMovement);
                break;
            case TYPE_TRANSFER:
                sourceAccountMono = integration.makeWithdrawalAccount(amount, sourceAccountNumber);
                destinationAccountMono = integration.makeDepositAccount(newAmount, destinationAccountNumber);
                sourceMovement.setAvailableBalance(Objects.requireNonNull(sourceAccountMono.block()).getAvailableBalance());
                integration.saveWithdrawlMovement(sourceMovement);
                break;
        }

        destinationMovement.setAvailableBalance(Objects.requireNonNull(destinationAccountMono.block()).getAvailableBalance());
        if (codesEnum == CodesEnum.TYPE_WITHDRAWL) {
            integration.saveWithdrawlMovement(destinationMovement);
        } else {
            integration.saveDepositMovement(destinationMovement);
        }
        TransactionAtmDTO transactionAtmDTO = new TransactionAtmDTO();
        transactionAtmDTO.setSourceAccount(sourceAccountNumber);
        transactionAtmDTO.setDestinationAccount(destinationAccountNumber);
        transactionAtmDTO.setAmount(newAmount);
        transactionAtmDTO.setComission(Math.abs(newComission));
        transactionAtmDTO.setTotalAmount(amount);
        transactionAtmDTO.setDate(Calendar.getInstance().getTime());
        return Mono.just(transactionAtmDTO);
    }

    @Override
    public Flux<MovementsReportDTO> getAllMovementsByAccount(String account) {
        integration.getByAccountNumber(account);
        return integration.getAllMovementsByNumberAccount(account);
    }

    @Override
    public Flux<AccountDTO> getAccountAllByCustomer(String customerId) {
        integration.getCustomerById(customerId);
        return integration.getAllAccountByCustomer(customerId);
    }

    @Override
    public Mono<AccountDTO> getAccountByNumber(String accountNumber) {
        integration.getByAccountNumber(accountNumber);
        return integration.getByAccountNumber(accountNumber);
    }

    @Override
    public Mono<AccountDTO> createAccount(AccountDTO dto) {
        integration.getCustomerById(dto.getCustomerId());
        return integration.createAccount(dto);
    }

    @Override
    public Flux<CustomerDto> getAllCustomers() {
        return integration.getAllCustomers();
    }

    @Override
    public Mono<CustomerDto> createCustomer(CustomerDto customerDto) {
        return integration.createCustomer(customerDto);
    }

    private void validationLimitAmount(String sourceAccount, String destinationAccount,
                                       CodesEnum codesEnum, double amount) {

        switch (codesEnum) {
            case TYPE_DEPOSIT:
                if (amount < MIN_DEPOSIT_AMOUNT || amount > MAX_DEPOSIT_AMOUNT) {
                    logger.debug("Error, limites de deposita hacia la cuenta nro:" + destinationAccount + " ,amount: " + amount);
                    throw new NotFoundException(String.format(Locale.getDefault(),
                            "Error, limites de monto de operacion , cuenta nro: " + destinationAccount + " (min: %d PEN y max: %d PEN) ",
                            MIN_DEPOSIT_AMOUNT, MAX_DEPOSIT_AMOUNT));
                }
                break;
            case TYPE_WITHDRAWL:
                if (amount < MIN_WITHDRAWAL_AMOUNT || amount > MAX_WITHDRAWAL_AMOUNT) {
                    logger.debug("Error,  limites de retiro  en cuenta nro: " + destinationAccount + " ,monto: " + amount);
                    throw new NotFoundException(String.format(Locale.getDefault(),
                            "Error, limites de monto de operacion , cuenta nro:" + destinationAccount + " (min: %d PEN y max: %d PEN)",
                            MIN_WITHDRAWAL_AMOUNT, MAX_WITHDRAWAL_AMOUNT));
                }
                break;
            case TYPE_TRANSFER:
                if (amount < MIN_TRANSFERENCE_AMOUNT || amount > MAX_TRANSFERENCE_AMOUNT) {
                    logger.debug("Error,  limites de transferencia ,  amount: " + amount);
                    throw new NotFoundException(String.format(Locale.getDefault(),
                            "Error, limites de monto de operacion , cuenta nro: " + sourceAccount + " (min: %d PEN y max: %d PEN)",
                            MIN_TRANSFERENCE_AMOUNT, MAX_TRANSFERENCE_AMOUNT));
                }
                break;
        }
    }
}
