package com.nttdata.bc39.grupo04.api.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DebitCardDTO {
    private String customerId;
    private String numberDebitCard;
    private List<String> associedAccounts;
    private Date date;
}
