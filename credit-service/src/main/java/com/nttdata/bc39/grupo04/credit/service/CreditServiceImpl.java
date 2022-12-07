package com.nttdata.bc39.grupo04.credit.service;

import static com.nttdata.bc39.grupo04.api.utils.Constants.CODE_PRODUCT_CREDITO_EMPRESARIAL;
import static com.nttdata.bc39.grupo04.api.utils.Constants.CODE_PRODUCT_CREDITO_PERSONAL;
import static com.nttdata.bc39.grupo04.api.utils.Constants.CODE_PRODUCT_TARJETA_CREDITO;
import static com.nttdata.bc39.grupo04.api.utils.Constants.MIN_CHARGE_CREDIT_CARD_AMOUNT;
import static com.nttdata.bc39.grupo04.api.utils.Constants.MIN_PAYMENT_CREDIT_AMOUNT;
import static com.nttdata.bc39.grupo04.api.utils.Constants.MIN_PAYMENT_CREDIT_CARD_AMOUNT;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

import com.nttdata.bc39.grupo04.credit.dto.CreditDTO;
import com.nttdata.bc39.grupo04.credit.exception.ServiceException;
import com.nttdata.bc39.grupo04.credit.persistence.CreditEntity;
import com.nttdata.bc39.grupo04.credit.persistence.CreditRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
		// TODO Auto-generated method stub
		validateCreateCredit(dto);
		CreditEntity entity = mapper.dtoToEntity(dto);
		entity.setCreditNumber(generateCreditNumber());
		entity.setAvailableBalance(entity.getCreditAmount());
		entity.setCreateDate(Calendar.getInstance().getTime());
		return repository.save(entity).onErrorMap(DuplicateKeyException.class,
				ex -> new ServiceException("Error , ya existe una credito con el Nro: " + dto.getCreditNumber(),
						HttpStatus.NOT_FOUND.toString(), LocalDate.now().toString(), HttpStatus.NOT_FOUND.value()))
				.map(mapper::entityToDto);

	}

	@Override
	public Mono<CreditDTO> getByCreditNumber(String creditNumber) {
		// TODO Auto-generated method stub
		if (ObjectUtils.isEmpty(creditNumber)) {
			throw new ServiceException("Error, numero de credito invalido", HttpStatus.NOT_FOUND.toString(),
					LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
		}
		Mono<CreditEntity> entityMono = repository.findByCreditNumber(creditNumber);
		if (Objects.isNull(entityMono.block())) {
			throw new ServiceException("Error, no existe el crédito con Nro: " + creditNumber,
					HttpStatus.NOT_FOUND.toString(), LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
		}
		return entityMono.map(mapper::entityToDto);
	}

	@Override
	public Flux<CreditDTO> getAllCreditByCustomer(String customerId) {
		// TODO Auto-generated method stub
		if (ObjectUtils.isEmpty(customerId)) {
			throw new ServiceException("Error, codigo de cliente invalido", HttpStatus.NOT_FOUND.toString(),
					LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
		}
		return repository.findAll().filter(x -> x.getCustomerId().equals(customerId)).map(mapper::entityToDto);
	}

	@Override
	public Mono<CreditDTO> makePaymentCredit(double amount, String creditNumber) {
		// TODO Auto-generated method stub
		CreditEntity entity = repository.findByCreditNumber(creditNumber).block();
		if (ObjectUtils.isEmpty(entity)) {
			throw new ServiceException("Error, no existe el credito con Nro: " + creditNumber,
					HttpStatus.NOT_FOUND.toString(), LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
		}
		if (entity.getAvailableBalance() == entity.getCreditAmount()) {
			throw new ServiceException("No tiene pagos pendientes", HttpStatus.NOT_FOUND.toString(),
					LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
		}

		if (amount < MIN_PAYMENT_CREDIT_AMOUNT || amount > entity.getCreditAmount()) {
			throw new ServiceException(
					String.format(Locale.getDefault(), "Error, los limites de Pago son min: %d sol y max: %f sol",
							MIN_PAYMENT_CREDIT_AMOUNT, entity.getCreditAmount()),
					HttpStatus.NOT_FOUND.toString(), LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
		}

		double newAvailableBalance = entity.getAvailableBalance() + amount;
		if (newAvailableBalance > entity.getCreditAmount()) {
			double suggestedAmount = entity.getCreditAmount() - entity.getAvailableBalance();
			throw new ServiceException(
					"El monto que intenta pagar excede el monto que debe pagar. Monto a pagar: " + suggestedAmount,
					HttpStatus.NOT_FOUND.toString(), LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
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
			throw new ServiceException("Error, no existe la tarjeta de credito con Nro: " + creditCardNumber,
					HttpStatus.NOT_FOUND.toString(), LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
		}
		if (entity.getAvailableBalance() == entity.getCreditAmount()) {
			throw new ServiceException("No tiene pagos pendientes", HttpStatus.NOT_FOUND.toString(),
					LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
		}

		if (amount < MIN_PAYMENT_CREDIT_CARD_AMOUNT || amount > entity.getCreditAmount()) {
			throw new ServiceException(
					String.format(Locale.getDefault(), "Error, los limites de Pago son min: %d sol y max: %f sol",
							MIN_PAYMENT_CREDIT_CARD_AMOUNT, entity.getCreditAmount()),
					HttpStatus.NOT_FOUND.toString(), LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
		}

		double newAvailableBalance = entity.getAvailableBalance() + amount;
		if (newAvailableBalance > entity.getCreditAmount()) {
			double suggestedAmount = entity.getCreditAmount() - entity.getAvailableBalance();
			throw new ServiceException(
					"El monto que intenta pagar excede el monto que debe pagar. Monto a pagar: " + suggestedAmount,
					HttpStatus.NOT_FOUND.toString(), LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
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
			throw new ServiceException("Error, no existe la tarjeta de credito con Nro: " + creditCardNumber,
					HttpStatus.NOT_FOUND.toString(), LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
		}
		if (amount < MIN_CHARGE_CREDIT_CARD_AMOUNT || amount > entity.getCreditAmount()) {
			throw new ServiceException(
					String.format(Locale.getDefault(), "Error, los limites de Cargo son min: %d sol y max: %f sol",
							MIN_CHARGE_CREDIT_CARD_AMOUNT, entity.getCreditAmount()),
					HttpStatus.NOT_FOUND.toString(), LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
		}
		double availableBalance = entity.getAvailableBalance();
		if (amount > availableBalance) {
			throw new ServiceException("Excede Limite disponible", HttpStatus.NOT_FOUND.toString(),
					LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
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
			throw new ServiceException(
					"Error, el codigo tipo de crédito debe ser Personal, Empresa o Tarjeta de Crédito",
					HttpStatus.NOT_FOUND.toString(), LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
		}

		if (dto.getProductId().equals(CODE_PRODUCT_CREDITO_PERSONAL)) {
			CreditEntity credit = repository.findAll()
					.filter(x -> x.getProductId().equals(CODE_PRODUCT_CREDITO_PERSONAL)
							&& x.getCustomerId().equals(dto.getCustomerId()))
					.blockFirst();
			if (credit != null) {
				throw new ServiceException("Error, una persona solo puede tener un máximo de un credito personal",
						HttpStatus.NOT_FOUND.toString(), LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
			}
		}

		if (dto.getProductId().equals(CODE_PRODUCT_TARJETA_CREDITO)) {
			if (ObjectUtils.isEmpty(dto.getCardNumber())) {
				throw new ServiceException("Error, el producto de tarjeta de crédito debe indicar el numero de tarjeta",
						HttpStatus.NOT_FOUND.toString(), LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
			}
			Mono<CreditEntity> entityMono = repository.findByCardNumber(dto.getCardNumber());
			if (!Objects.isNull(entityMono.block())) {
				throw new ServiceException("Error, Ya existe una tarjeta asignada con Nro: " + dto.getCardNumber(),
						HttpStatus.NOT_FOUND.toString(), LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
			}
		} else {
			if (!ObjectUtils.isEmpty(dto.getCardNumber())) {
				throw new ServiceException(
						"Error, el numero de tarjeta solo debe indicarse para producto de tarjeta de crédito",
						HttpStatus.NOT_FOUND.toString(), LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
			}
		}
	}

	@Override
	public Flux<CreditDTO> getAllCreditCardByCustomer(String customerId) {
		// TODO Auto-generated method stub
		return repository.findAll().filter(
				x -> x.getProductId().equals(CODE_PRODUCT_TARJETA_CREDITO) && x.getCustomerId().equals(customerId))
				.map(mapper::entityToDto);
	}

}
