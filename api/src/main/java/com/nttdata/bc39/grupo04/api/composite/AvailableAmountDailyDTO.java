package com.nttdata.bc39.grupo04.api.composite;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvailableAmountDailyDTO {
    private Date date;
    private double availableAmount;


}
