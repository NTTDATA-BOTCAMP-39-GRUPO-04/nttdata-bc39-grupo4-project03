package com.nttdata.bc39.grupo04.api.account;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DebitCardPaymentDTO {
    private String debitCartNumber;
    private double amount;
    private String chargeAccountNumber;
}
