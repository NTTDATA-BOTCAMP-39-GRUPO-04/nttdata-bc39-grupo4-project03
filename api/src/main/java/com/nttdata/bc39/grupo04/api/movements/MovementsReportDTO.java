package com.nttdata.bc39.grupo04.api.movements;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovementsReportDTO {
    private String account;
    private String transferAccount;
    private String productId;
    private double amount;
    private double comission;
    private double totalAmount;
    private double availableBalance;
    private Date date;
}
