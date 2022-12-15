package com.nttdata.bc39.grupo04.composite.service;

import com.nttdata.bc39.grupo04.api.account.AccountDTO;
import com.nttdata.bc39.grupo04.api.account.DebitCardDTO;
import com.nttdata.bc39.grupo04.api.account.DebitCardNumberDTO;
import com.nttdata.bc39.grupo04.api.account.DebitCardPaymentDTO;
import com.nttdata.bc39.grupo04.api.account.DebitCardReportDTO;
import com.nttdata.bc39.grupo04.api.composite.*;
import com.nttdata.bc39.grupo04.api.credit.CreditCardReportDTO;
import com.nttdata.bc39.grupo04.api.credit.CreditDTO;
import com.nttdata.bc39.grupo04.api.customer.ConsolidatedSummaryDTO;
import com.nttdata.bc39.grupo04.api.customer.CustomerDto;
import com.nttdata.bc39.grupo04.api.exceptions.BadRequestException;
import com.nttdata.bc39.grupo04.api.exceptions.InvaliteInputException;
import com.nttdata.bc39.grupo04.api.exceptions.NotFoundException;
import com.nttdata.bc39.grupo04.api.movements.MovementsDTO;
import com.nttdata.bc39.grupo04.api.movements.MovementsReportDTO;
import com.nttdata.bc39.grupo04.api.product.GeneralReportDTO;
import com.nttdata.bc39.grupo04.api.product.ProductDTO;
import com.nttdata.bc39.grupo04.api.utils.CodesEnum;
import com.nttdata.bc39.grupo04.api.utils.Constants;
import com.nttdata.bc39.grupo04.api.utils.DateUtils;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Supplier;
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
        return takeTransference(ACCOUNT_NUMBER_OF_ATM, destinationAccountNumber, amount, CodesEnum.TYPE_DEPOSIT);
    }

    @Override
    public Mono<TransactionAtmDTO> makeWithdrawnATM(String destinationAccountNumber, double amount) {
        return takeTransference(ACCOUNT_NUMBER_OF_ATM, destinationAccountNumber, amount, CodesEnum.TYPE_WITHDRAWL);
    }

    @Override
    public Mono<TransactionAtmDTO> makeTransferAccount(TransactionTransferDTO body) {
        return takeTransference(body.getSourceAccount(), body.getDestinationAccount(), body.getAmount(), CodesEnum.TYPE_TRANSFER);
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
            List<MovementsReportDTO> movementByAccount = getAllMovementsByAccount(account.getAccount()).collectList().block();
            if (!Objects.isNull(movementByAccount)) {
                movementByAccount.forEach(item -> item.setDate(DateUtils.getDateWithFormatddMMyyyy(item.getDate())));
                movementsAllProductByCustomerList.addAll(movementByAccount);
            }
        });
        Map<Date, Double> avgAvailableBalanceMap = movementsAllProductByCustomerList.stream().collect(Collectors.groupingBy(MovementsReportDTO::getDate, Collectors.averagingDouble(MovementsReportDTO::getAvailableBalance)));
        List<AvailableAmountDailyDTO> reportList = new ArrayList<>();
        avgAvailableBalanceMap.keySet().forEach(key -> reportList.add(new AvailableAmountDailyDTO(key, avgAvailableBalanceMap.get(key))));
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
        Flux<MovementsReportDTO> movementsAll = integration.getAllMovements().map(movement -> {
            movement.setDate(DateUtils.getDateWithFormatddMMyyyy(movement.getDate()));
            return movement;
        }).filter(x -> x.getDate().compareTo(startDate) >= 0 && x.getDate().compareTo(endDate) <= 0);

        Map<String, Double> comissionMap = movementsAll.toStream().peek(item -> item.setComission(Math.abs(item.getComission()))).collect(Collectors.groupingBy(MovementsReportDTO::getProductId, Collectors.summingDouble(MovementsReportDTO::getComission)));

        List<ComissionReportDTO> comissionList = new ArrayList<>(List.of());
        comissionMap.keySet().forEach(key -> {
            comissionList.add(new ComissionReportDTO(key, Constants.getNameProduct(key), comissionMap.get(key)));
        });
        return Mono.just(comissionList).flatMapMany(Flux::fromIterable);
    }

    private Mono<TransactionAtmDTO> takeTransference(String sourceAccountNumber, String destinationAccountNumber, double amount, CodesEnum codesEnum) {
        Mono<AccountDTO> sourceMono = integration.getByAccountNumber(sourceAccountNumber);
        Mono<AccountDTO> destinationMono = integration.getByAccountNumber(destinationAccountNumber);
        String productIdSource = Objects.requireNonNull(sourceMono.block()).getProductId();
        String productIdDestination = Objects.requireNonNull(destinationMono.block()).getProductId();
        logger.debug("productIdSource => " + productIdSource);
        logger.debug("productIdDestination => " + productIdDestination);
        validationLimitAmount(sourceAccountNumber, destinationAccountNumber, codesEnum, amount);
        Flux<MovementsReportDTO> movements = integration.getAllMovementsByNumberAccount(codesEnum == CodesEnum.TYPE_TRANSFER ? sourceAccountNumber : destinationAccountNumber);
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

        Mono<AccountDTO> sourceAccountMono;
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
    public Mono<DebitCardDTO> createDebitCard(DebitCardDTO debitCardDTO) {
        if (Objects.isNull(debitCardDTO)) {
            throw new InvaliteInputException("Error, el cuerpo de solicitud es invalido");
        }
        if (Objects.isNull(debitCardDTO.getCustomerId())) {
            throw new InvaliteInputException("Error, el codigo de cliente  es invalido");
        }
        integration.getAllCreditByCustomer(debitCardDTO.getCustomerId());
        return integration.createDebitCard(debitCardDTO);
    }

    @Override
    public Mono<DebitCardNumberDTO> generateNumberDebitCard() {
        return integration.generateNumberDebitCard();
    }

    @Override
    public Mono<AccountDTO> getMainAccountByDebitCardNumber(String debitCardNumber) {
        Supplier<Flux<AccountDTO>> associatedAccountsSupplier = () -> integration.getAllAccountByDebitCardNumber(debitCardNumber);
        if (Objects.isNull(associatedAccountsSupplier.get().blockFirst())) {
            throw new InvaliteInputException("Error, la tarjeta de debito con nro: " + debitCardNumber + " no esta asociada a ninguna cuenta bancaria");
        }
        return Mono.just(associatedAccountsSupplier.get().toStream().sorted(Comparator.comparing(AccountDTO::getDebitCardCreationDate)).collect(Collectors.toList()).get(0));
    }

    @Override
    public Mono<DebitCardPaymentDTO> paymentWithDebitCard(DebitCardPaymentDTO debitCardPaymnetDTO) {
        validatePaymentDebitCard(debitCardPaymnetDTO);
        Supplier<Flux<AccountDTO>> associatedAccountsSupplier = () -> integration.getAllAccountByDebitCardNumber(debitCardPaymnetDTO.getDebitCartNumber());
        if (Objects.isNull(associatedAccountsSupplier.get().blockFirst())) {
            throw new InvaliteInputException("Error, la tarjeta de debito con nro: " + debitCardPaymnetDTO.getDebitCartNumber() + " no esta asociada a ninguna cuenta bancaria");
        }
        int index = 0;
        List<AccountDTO> associatedAccountsList = associatedAccountsSupplier.get().toStream().sorted(Comparator.comparing(AccountDTO::getDebitCardCreationDate)).collect(Collectors.toList());
        for (AccountDTO account : associatedAccountsList) {
            index++;
            String sourceAccount = account.getAccount();
            TransactionTransferDTO transferenceDTO = new TransactionTransferDTO();
            transferenceDTO.setSourceAccount(sourceAccount);
            transferenceDTO.setDestinationAccount(ACCOUNT_NUMBER_OF_ATM);
            transferenceDTO.setAmount(debitCardPaymnetDTO.getAmount());
            try {
                makeTransferAccount(transferenceDTO);
                debitCardPaymnetDTO.setChargeAccountNumber(account.getAccount());
                break;
            } catch (RuntimeException ex) {
                if (index == associatedAccountsList.size()) {
                    if (NotFoundException.class.equals(ex.getClass())) {
                        throw new NotFoundException(ex.getMessage());
                    } else if (BadRequestException.class.equals(ex.getClass())) {
                        throw new BadRequestException(ex.getMessage());
                    } else {
                        throw new InvaliteInputException(ex.getMessage());
                    }
                }
            }
        }
        return Mono.just(debitCardPaymnetDTO);
    }

    @Override
    public Mono<AccountDTO> getAccountByNumber(String accountNumber) {
        integration.getByAccountNumber(accountNumber);
        return integration.getByAccountNumber(accountNumber);
    }

    @Override
    public Mono<AccountDTO> createAccount(AccountDTO dto) {
        integration.getCustomerById(dto.getCustomerId());
        validateCreateAccount(dto);
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

    // Credit

    @Override
    public Mono<CreditDTO> createCredit(CreditDTO dto) {
        integration.getCustomerById(dto.getCustomerId());
        return integration.createCredit(dto);
    }

    @Override
    public Flux<CreditDTO> getAllCreditByCustomer(String customerId) {
        return integration.getAllCreditByCustomer(customerId);
    }

    @Override
    public Flux<CreditDTO> getAllCreditCardByCustomer(String customerId) {
        return integration.getAllCreditCardByCustomer(customerId);
    }

    @Override
    public Mono<CreditDTO> getByCreditNumber(String creditNumber) {
        return integration.getByCreditNumber(creditNumber);
    }

    @Override
    public Mono<CreditDTO> makePaymentCredit(double amount, String creditNumber, String payingCustomerId) {
    	CreditDTO dto = integration.getByCreditNumber(creditNumber).block();
    	List<MovementsReportDTO> list  = integration.getAllMovementsByNumberAccount(creditNumber).collectList().block();
    	double addedAmount=0.0;
    	for (MovementsReportDTO movementsReportDTO : list) {
    		addedAmount = addedAmount + movementsReportDTO.getAmount();
		}
    	validatePaymentCredit(dto, amount, creditNumber,addedAmount);
    	MovementsDTO movementsDTO = new MovementsDTO();
    	movementsDTO.setAccount(creditNumber);
    	movementsDTO.setTransferAccount(creditNumber);
    	movementsDTO.setProductId(dto.getProductId());
    	movementsDTO.setAmount(amount);
    	movementsDTO.setDate(Calendar.getInstance().getTime());  
    	if(!ObjectUtils.isEmpty(payingCustomerId)) {
    		CustomerDto customerDto = integration.getCustomerById(payingCustomerId).block();
    		if(!ObjectUtils.isEmpty(customerDto)) {
    			movementsDTO.setPayingCustomer(payingCustomerId);
    		}
    	}
    	integration.saveCreditMovement(movementsDTO);    	
        return Mono.just(dto);
    }
    
    @Override
    public Mono<CreditDTO> makePaymentCreditCard(double amount, String creditCardNumber) {
    	CreditDTO dto = integration.getCreditCardByNumber(creditCardNumber).block();
    	List<MovementsReportDTO> list  = integration.getAllMovementsByNumberAccount(dto.getCreditNumber()).collectList().block();
    	double addedAmount=0.0;
    	for (MovementsReportDTO movementsReportDTO : list) {
    		addedAmount = addedAmount + movementsReportDTO.getAmount();
		}
    	validatePaymentCreditCard(dto, amount, creditCardNumber,addedAmount);
    	MovementsDTO movementsDTO = new MovementsDTO();
    	movementsDTO.setAccount(dto.getCreditNumber());
    	movementsDTO.setTransferAccount(dto.getCreditNumber());
    	movementsDTO.setProductId(dto.getProductId());
    	movementsDTO.setAmount(amount);
    	movementsDTO.setDate(Calendar.getInstance().getTime());  
    	integration.savePaymentCreditCardMovement(movementsDTO);	
        return Mono.just(dto);
    }
    
    
    @Override
    public Mono<CreditDTO> makeChargeCredit(double amount, String creditCardNumber) {
    	CreditDTO dto = integration.getCreditCardByNumber(creditCardNumber).block();
    	List<MovementsReportDTO> list  = integration.getAllMovementsByNumberAccount(dto.getCreditNumber()).collectList().block();
    	double addedAmount=0.0;
    	for (MovementsReportDTO movementsReportDTO : list) {
    		addedAmount = addedAmount + movementsReportDTO.getAmount();
		}
    	validatePaymentCreditCard(dto, amount, creditCardNumber,addedAmount);
    	MovementsDTO movementsDTO = new MovementsDTO();
    	movementsDTO.setAccount(dto.getCreditNumber());
    	movementsDTO.setTransferAccount(dto.getCreditNumber());
    	movementsDTO.setProductId(dto.getProductId());
    	movementsDTO.setAmount(amount);
    	movementsDTO.setDate(Calendar.getInstance().getTime());  
    	integration.saveChargeCreditCardMovement(movementsDTO);	
    	movementsDTO.setAvailableBalance(0.0);
        return Mono.just(dto);
    }

    @Override
    public Mono<Void> deleteCredit(String creditNumber) {
        return integration.deleteCredit(creditNumber);
    }

    // Product
    @Override
    public Mono<ProductDTO> getProductByCode(String productId) {
        return integration.getProductByCode(productId);
    }

    @Override
    public Flux<ProductDTO> getAllProducts() {
        return integration.getAllProducts();
    }

    @Override
    public Mono<ProductDTO> createProduct(ProductDTO dto) {
        return integration.createProduct(dto);
    }

    @Override
    public Mono<ProductDTO> updateProduct(ProductDTO dto) {
        return integration.updateProduct(dto);
    }

    @Override
    public Mono<Void> deleteProductByCode(String code) {
        return integration.deleteProductByCode(code);
    }

    private void validatePaymentDebitCard(DebitCardPaymentDTO debitCardPaymnetDTO) {
        if (Objects.isNull(debitCardPaymnetDTO)) {
            throw new InvaliteInputException("Error, el cuerpo de la solicitud es invalido");
        }
        if (Objects.isNull(debitCardPaymnetDTO.getDebitCartNumber())) {
            throw new InvaliteInputException("Error, el numero de tarjeta de debito es invalido");
        }
        if (debitCardPaymnetDTO.getAmount() <= 0.0) {
            throw new InvaliteInputException("Error, el monto debe de ser mayor a cero");
        }
    }

    private void validationLimitAmount(String sourceAccount, String destinationAccount, CodesEnum codesEnum, double amount) {

        switch (codesEnum) {
            case TYPE_DEPOSIT:
                if (amount < MIN_DEPOSIT_AMOUNT || amount > MAX_DEPOSIT_AMOUNT) {
                    logger.debug("Error, limites de deposita hacia la cuenta nro:" + destinationAccount + " ,amount: " + amount);
                    throw new NotFoundException(String.format(Locale.getDefault(), "Error, limites de monto de operacion , cuenta nro: " + destinationAccount + " (min: %d PEN y max: %d PEN) ", MIN_DEPOSIT_AMOUNT, MAX_DEPOSIT_AMOUNT));
                }
                break;
            case TYPE_WITHDRAWL:
                if (amount < MIN_WITHDRAWAL_AMOUNT || amount > MAX_WITHDRAWAL_AMOUNT) {
                    logger.debug("Error,  limites de retiro  en cuenta nro: " + destinationAccount + " ,monto: " + amount);
                    throw new NotFoundException(String.format(Locale.getDefault(), "Error, limites de monto de operacion , cuenta nro:" + destinationAccount + " (min: %d PEN y max: %d PEN)", MIN_WITHDRAWAL_AMOUNT, MAX_WITHDRAWAL_AMOUNT));
                }
                break;
            case TYPE_TRANSFER:
                if (amount < MIN_TRANSFERENCE_AMOUNT || amount > MAX_TRANSFERENCE_AMOUNT) {
                    logger.debug("Error,  limites de transferencia ,  amount: " + amount);
                    throw new NotFoundException(String.format(Locale.getDefault(), "Error, limites de monto de operacion , cuenta nro: " + sourceAccount + " (min: %d PEN y max: %d PEN)", MIN_TRANSFERENCE_AMOUNT, MAX_TRANSFERENCE_AMOUNT));
                }
                break;
        }
    }

    private void validateCreateAccount(AccountDTO dto) {
        if (dto.getProductId().equals(CODE_PRODUCT_PERSONAL_VIP_AHORRO)) {
            Flux<CreditDTO> listCreditCard = integration.getAllCreditCardByCustomer(dto.getCustomerId());
            if (ObjectUtils.isEmpty(listCreditCard.blockFirst())) {
                throw new NotFoundException("Error, para crear una cuenta VIP debe tener una tarjeta de crédito");
            }
        }

        if (dto.getProductId().equals(CODE_PRODUCT_EMPRESA_PYME_CORRIENTE)) {
            Flux<CreditDTO> listCreditCard = integration.getAllCreditCardByCustomer(dto.getCustomerId());
            if (ObjectUtils.isEmpty(listCreditCard.blockFirst())) {
                throw new NotFoundException("Error, para crear una cuenta PYME debe tener una tarjeta de crédito");
            }

        }
    }
    
    private void validatePaymentCredit(CreditDTO dto, double amount,String creditNumber, double addedAmount) {
        if (ObjectUtils.isEmpty(dto)) {
            throw new NotFoundException("Error, no existe el credito con Nro: " + creditNumber);
        }
        if (addedAmount == dto.getCreditAmount()) {
            throw new InvaliteInputException("No tiene pagos pendientes");
        }

        if (amount < MIN_PAYMENT_CREDIT_AMOUNT || amount > dto.getCreditAmount()) {
            throw new InvaliteInputException(
                    String.format(Locale.getDefault(), "Error, los limites de Pago son min: %d sol y max: %f sol",
                            MIN_PAYMENT_CREDIT_AMOUNT, dto.getCreditAmount()));
        }
        
        if ((addedAmount+amount) > dto.getCreditAmount()) {
            double suggestedAmount = dto.getCreditAmount() - addedAmount;
            throw new InvaliteInputException(
                    "El monto que intenta pagar excede el monto Total que debe pagar. Monto a pagar: " + suggestedAmount);
        }
    }
    
    
    private void validatePaymentCreditCard(CreditDTO dto, double amount, String creditCardNumber, double addedAmount) {
        if (ObjectUtils.isEmpty(dto)) {
            throw new NotFoundException("Error, no existe la tarjeta de credito con Nro: " + creditCardNumber);
        }
        if (addedAmount == dto.getCreditAmount()) {
            throw new InvaliteInputException("No tiene pagos pendientes");
        }

        if (amount < MIN_PAYMENT_CREDIT_CARD_AMOUNT || amount > dto.getCreditAmount()) {
            throw new InvaliteInputException(
                    String.format(Locale.getDefault(), "Error, los limites de Pago son min: %d sol y max: %f sol",
                            MIN_PAYMENT_CREDIT_CARD_AMOUNT, dto.getCreditAmount()));
        }

        if ((dto.getCreditAmount() +addedAmount+amount) > dto.getCreditAmount()) {
            throw new InvaliteInputException(
                    "El monto que intenta pagar excede el monto Total que debe pagar." );
        }
    }

	@Override
	public Mono<DebitCardReportDTO> getLastTenDebitCardMovements(String debitCardNumber) {
		AccountDTO acouMono = getMainAccountByDebitCardNumber(debitCardNumber).block();

        Flux<MovementsReportDTO> movementsAll = integration.getAllMovementsByNumberAccount(acouMono.getAccount()).sort(Comparator.comparing(MovementsReportDTO::getDate).reversed());              
        List<MovementsReportDTO> listMovementsReportDTO = movementsAll.toStream().collect(Collectors.toList());
        DebitCardReportDTO debitCardReportDTO = new DebitCardReportDTO();
        debitCardReportDTO.setDebitCardNumber(debitCardNumber);
        debitCardReportDTO.setMovements(listMovementsReportDTO);
        return Mono.just(debitCardReportDTO);
	}
	
	@Override
	public Mono<CreditCardReportDTO> getLastTenCreditCardMovements(String creditCardNumber) {	
		CreditDTO creditDTO = integration.getCreditCardByNumber(creditCardNumber).block();

        Flux<MovementsReportDTO> movementsAll = integration.getAllMovementsByNumberAccount(creditDTO.getCreditNumber()).sort(Comparator.comparing(MovementsReportDTO::getDate).reversed());              
        List<MovementsReportDTO> listMovementsReportDTO = movementsAll.toStream().collect(Collectors.toList());
        CreditCardReportDTO dto = new CreditCardReportDTO();
        dto.setCreditCardNumber(creditCardNumber);
        dto.setMovements(listMovementsReportDTO);
        return Mono.just(dto);
	}
	
	@Override
	public Mono<ConsolidatedSummaryDTO> getConsolidatedSummary(String customerId){
		ConsolidatedSummaryDTO consolidatedSummaryDTO = new ConsolidatedSummaryDTO();
		CustomerDto customerDto = integration.getCustomerById(customerId).block();
		List<CreditDTO> creditDTOs = integration.getAllCreditByCustomer(customerId).toStream().collect(Collectors.toList());
		List<AccountDTO> accountDTOs = integration.getAllAccountByCustomer(customerId).toStream().collect(Collectors.toList());
		
		consolidatedSummaryDTO.setCustomer(customerDto);
		consolidatedSummaryDTO.setAccounts(accountDTOs);
		consolidatedSummaryDTO.setCredits(creditDTOs);
		return Mono.just(consolidatedSummaryDTO);
	}
	
    @Override
    public Mono<GeneralReportDTO> getReportGeneral(String fechStart, String fechEnd, String productId) {
        if (Objects.isNull(fechStart) || Objects.isNull(fechEnd)) {
            throw new InvaliteInputException("Error, las fechas ingresadas son invalidad , formato requerido : dd/mm/yyyy");
        }
        Date startDate = DateUtils.convertStringToDate(fechStart);
        Date endDate = DateUtils.convertStringToDate(fechEnd);
        if (Objects.isNull(startDate) || Objects.isNull(endDate)) {
            throw new InvaliteInputException("Error, las fechas ingresadas son invalidad , formato requerido : dd/mm/yyyy");
        }
        
        ProductDTO productDTO = integration.getProductByCode(productId).block();
        Flux<MovementsReportDTO> movementsAll = integration.getAllMovements().filter(x -> x.getDate().compareTo(startDate) >= 0 && x.getDate().compareTo(endDate) <= 0 && x.getProductId().equals(productId));
        List<MovementsReportDTO> listMovementsReportDTO = movementsAll.toStream().collect(Collectors.toList());
        
        GeneralReportDTO generalReportDTO = new GeneralReportDTO();
        generalReportDTO.setProduct(productDTO);
        generalReportDTO.setMovements(listMovementsReportDTO);
        return Mono.just(generalReportDTO);
    }

}
