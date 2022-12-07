package com.nttdata.bc39.grupo04.api.credit;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditCustomerDTO implements Serializable {

	private static final long serialVersionUID = 3129127191471858620L;

	private String creditNumber;

	private String productId;

	private String customerId;

	private double availableBalance;

	private double creditAmount;

	private String cardNumber;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createDate;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date modifyDate; 

}
