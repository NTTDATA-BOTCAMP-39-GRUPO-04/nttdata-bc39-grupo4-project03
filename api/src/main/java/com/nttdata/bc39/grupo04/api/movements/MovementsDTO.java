package com.nttdata.bc39.grupo04.api.movements;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovementsDTO {
    private String account;
    private String productId;
    private String transferAccount;
    private double amount;
    private double comission;
    private double availableBalance;
    private Date date;
}
