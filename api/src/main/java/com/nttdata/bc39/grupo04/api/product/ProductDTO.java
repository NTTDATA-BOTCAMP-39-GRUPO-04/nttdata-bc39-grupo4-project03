package com.nttdata.bc39.grupo04.api.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nttdata.bc39.grupo04.api.product.enumerator.Enum;
import com.nttdata.bc39.grupo04.api.product.enumerator.TypeProductEnum;
import com.nttdata.bc39.grupo04.api.product.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDTO implements Serializable{


	private static final long serialVersionUID = 8242254604761455533L;

	@JsonProperty("code")
	@NotBlank(message = "Error, código de producto inválido")
	private String code;
	
	@JsonProperty("name")
	@NotBlank(message = "Error, nombre de producto inválido")
	private String name;
	
	@JsonProperty("typeProduct")
	@NotBlank(message = "Error, tipo de producto inválido")
	@Enum(enumClass= TypeProductEnum.class, ignoreCase=true)
	private String typeProduct;

	public static ProductDTO of(Object object) throws ServiceException {
		ObjectMapper mapper = new ObjectMapper();
		ProductDTO productDTO = mapper.convertValue(object, ProductDTO.class);
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		
		Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);
		if(!CollectionUtils.isEmpty(violations)) {
			throw new ServiceException(violations.iterator().next().getMessage(),HttpStatus.NOT_FOUND.toString(), LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
		}
		return productDTO;
	}
}
