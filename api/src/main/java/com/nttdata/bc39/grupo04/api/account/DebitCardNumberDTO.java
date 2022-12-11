package com.nttdata.bc39.grupo04.api.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DebitCardNumberDTO {
    private String numberDebitCard;
    private Date date;
}
