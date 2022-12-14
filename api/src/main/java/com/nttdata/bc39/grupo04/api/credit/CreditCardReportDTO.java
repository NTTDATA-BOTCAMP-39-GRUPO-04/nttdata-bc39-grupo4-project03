package com.nttdata.bc39.grupo04.api.credit;

import java.util.List;

import com.nttdata.bc39.grupo04.api.movements.MovementsReportDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditCardReportDTO {
	
    private String creditCardNumber;
    private List<MovementsReportDTO> movements;

}
