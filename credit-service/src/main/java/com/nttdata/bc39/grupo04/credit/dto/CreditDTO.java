package com.nttdata.bc39.grupo04.credit.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nttdata.bc39.grupo04.credit.exception.ServiceException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditDTO implements Serializable {
	private static final long serialVersionUID = -7836207118669985116L;

	private String creditNumber;

	@JsonProperty("productId")
	@NotBlank(message = "Error, codigo de producto inválido")
	private String productId;

	@JsonProperty("customerId")
	@NotBlank(message = "Error, codigo de cliente inválido")
	private String customerId;

	private double availableBalance;

	@JsonProperty("creditAmount")
	@NotNull(message = "Error, Monto de credito inválido")
	@DecimalMin(value = "0.0", inclusive = false, message = "El valor del credito debe ser mayo a 0")
	private double creditAmount;

	private String cardNumber;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createDate;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date modifyDate;

	public static CreditDTO of(Object object) throws ServiceException {
		ObjectMapper mapper = new ObjectMapper();
		CreditDTO creditDTO = mapper.convertValue(object, CreditDTO.class);
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();

		Set<ConstraintViolation<CreditDTO>> violations = validator.validate(creditDTO);
		if (!CollectionUtils.isEmpty(violations)) {
			throw new ServiceException(violations.iterator().next().getMessage(), HttpStatus.NOT_FOUND.toString(),
					LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
		}
		return creditDTO;
	}

}
