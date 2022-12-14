package com.nttdata.bc39.grupo04.api.customer;

import java.util.List;

import com.nttdata.bc39.grupo04.api.account.AccountDTO;
import com.nttdata.bc39.grupo04.api.credit.CreditDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsolidatedSummaryDTO {
	
	private CustomerDto customer;
	private List<CreditDTO> credits;
	private List<AccountDTO> accounts;
	

}
