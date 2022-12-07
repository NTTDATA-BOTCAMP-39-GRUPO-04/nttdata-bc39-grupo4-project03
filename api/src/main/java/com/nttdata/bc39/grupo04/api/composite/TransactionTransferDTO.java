package com.nttdata.bc39.grupo04.api.composite;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransactionTransferDTO {
    private String sourceAccount;
    private String destinationAccount;
    private double amount;
    private double comission;
    private double totalAmount;
    private Date date;
}
