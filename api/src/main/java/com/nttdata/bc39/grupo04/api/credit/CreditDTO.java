package com.nttdata.bc39.grupo04.api.credit;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

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

}
